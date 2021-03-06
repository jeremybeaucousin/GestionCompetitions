package v1.controllers

import scala.concurrent.Future

import javax.inject.Inject
import play.Logger
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.BodyParsers
import play.api.mvc.Controller
import v1.constantes.HttpConstants
import v1.constantes.MessageConstants
import v1.http.ApiToken
import v1.model.Operation
import v1.model.PasswordChange
import v1.model.Person
import v1.services.AuthenticationServices
import v1.services.DocumentationServices
import v1.utils.RequestUtil
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import v1.services.MailServices
import v1.services.PersonServices
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import v1.errors.EmailPasswordLoginRequired
import v1.errors.LoginOrEmailAndPasswordRequiredException
import play.api.libs.oauth.OAuthCalculator
import play.api.libs.ws.WSClient

class AuthenticationController @Inject() (
  val documentationServices: DocumentationServices,
  val authenticationServices: AuthenticationServices,
  val mailServices: MailServices,
  val wsClient: WSClient,
  val messagesApi: MessagesApi)
    extends Controller with I18nSupport with Secured with SecuredOAuth {

  // TODO exemple
  def timeline = Action.async { implicit request =>
    sessionTokenPair match {
      case Some(credentials) => {
        wsClient.url("https://api.twitter.com/1.1/statuses/home_timeline.json")
          .sign(OAuthCalculator(KEY, credentials))
          .get
          .map(result => Ok(result.json))
      }
      case _ => Future.successful(Redirect(routes.AuthenticationController.signin()))
    }
  }

  // TODO To implement
  def signin2 = Action.async(BodyParsers.parse.json) { implicit request =>

    sessionTokenPair match {
      case Some(credentials) => {
        wsClient.url("https://api.twitter.com/1.1/statuses/home_timeline.json")
          .sign(OAuthCalculator(KEY, credentials))
          .get
          .map(result => Ok(result.json))
      }
      case _ => Future.successful(Redirect(routes.AuthenticationController.signin()))
    }
    
    val apiKeyOpt = request.headers.get(HttpConstants.headerFields.xApiKey)
    if (apiKeyOpt.isDefined && ApiToken.apiKeysExists(apiKeyOpt.get)) {
      val signInReads: Reads[Tuple2[String, String]] = (
        (JsPath \ Person.LOGIN).read[String] and
        (JsPath \ Person.PASSWORD).read[String]).apply((login, password) => (login, password))
      val signInInputs = signInReads.reads(request.body).asOpt
      if (signInInputs.isDefined) {
        val futurePerson = authenticationServices.authenticate(signInInputs.get._1, signInInputs.get._2)
        val personFound = Await.result(futurePerson, Duration.Inf)
        if (personFound.isDefined && personFound.get._id.isDefined) {
          val futureApiToken = ApiToken.create(apiKeyOpt.get, personFound.get._id.get)
          futureApiToken.map(apiToken => {
            if (apiToken != null) {
              Ok(
                Json.obj(
                  ApiToken.TOKEN_FIELD -> apiToken.token,
                  ApiToken.DURATION_FIELD -> ApiToken.API_TOKEN_DURATION))
            } else {
              Unauthorized
            }
          })
        } else {
          Future(Unauthorized)
        }

      } else {
        throw new LoginOrEmailAndPasswordRequiredException
      }

    } else {
      Future(Unauthorized)
    }
  }
  def index = Action.async { implicit request =>
    val rootUrl: String = routes.AuthenticationController.index.url
    val title: String = messagesApi(MessageConstants.title.documentation, rootUrl)
    val availableOperations: Seq[Operation] = documentationServices.getAuthenticationOperations
    Future.successful(Ok(v1.views.html.documentation(title, availableOperations)))
  }

  def signup = Action.async(BodyParsers.parse.json) { implicit request =>
    val futurePerson = authenticationServices.createAccount(None, request.body.as[Person])
    futurePerson.map {
      case (personOption, isNew) =>
        if (personOption.isDefined && personOption.get._id.isDefined) {
          var returnedLocation = HttpConstants.headerFields.location -> (routes.PersonController.getPerson(personOption.get._id.get, None).absoluteURL())
          if (isNew) {
            // TODO Send email
            mailServices.createAndSendEmail()
            Created.withHeaders(returnedLocation)
          } else {
            Conflict(Json.toJson(personOption.get)).withHeaders(returnedLocation)
          }
        } else {
          UnprocessableEntity
        }
    }
  }

  def signupWithExistingPerson(id: String) = Action.async(BodyParsers.parse.json) { implicit request =>
    val futurePerson = authenticationServices.createAccount(Some(id), request.body.as[Person])
    futurePerson.map {
      case (personOption, resultOk) =>
        if (personOption.isDefined && personOption.get._id.isDefined) {
          var returnedLocation = HttpConstants.headerFields.location -> (routes.PersonController.getPerson(personOption.get._id.get, None).absoluteURL())
          if (resultOk) {
            // TODO Send email
            Ok.withHeaders(returnedLocation)
          } else {
            Conflict(Json.toJson(personOption.get)).withHeaders(returnedLocation)
          }
        } else {
          UnprocessableEntity
        }
    }
  }

  def signin = Action.async(BodyParsers.parse.json) { implicit request =>
    val apiKeyOpt = request.headers.get(HttpConstants.headerFields.xApiKey)
    if (apiKeyOpt.isDefined && ApiToken.apiKeysExists(apiKeyOpt.get)) {
      val signInReads: Reads[Tuple2[String, String]] = (
        (JsPath \ Person.LOGIN).read[String] and
        (JsPath \ Person.PASSWORD).read[String]).apply((login, password) => (login, password))
      val signInInputs = signInReads.reads(request.body).asOpt
      if (signInInputs.isDefined) {
        val futurePerson = authenticationServices.authenticate(signInInputs.get._1, signInInputs.get._2)
        val personFound = Await.result(futurePerson, Duration.Inf)
        if (personFound.isDefined && personFound.get._id.isDefined) {
          val futureApiToken = ApiToken.create(apiKeyOpt.get, personFound.get._id.get)
          futureApiToken.map(apiToken => {
            if (apiToken != null) {
              Ok(
                Json.obj(
                  ApiToken.TOKEN_FIELD -> apiToken.token,
                  ApiToken.DURATION_FIELD -> ApiToken.API_TOKEN_DURATION))
            } else {
              Unauthorized
            }
          })
        } else {
          Future(Unauthorized)
        }

      } else {
        throw new LoginOrEmailAndPasswordRequiredException
      }

    } else {
      Future(Unauthorized)
    }
  }

  def signout = withToken { authToken =>
    implicit request =>
      ApiToken.delete(authToken)
      Future(NoContent)
  }

  def resetPassword = Action.async(BodyParsers.parse.json) { implicit request =>
    val person = request.body.as[Person]
    if (person.email.isDefined) {
      val futureResult = authenticationServices.resetPassword(person)
      futureResult.map(resultOk => {
        if (resultOk) {
          //          TODO See why it does not work
          //          mailServices.createAndSendEmail()
          //personWithSameEmail.email
          Ok
        } else {
          UnprocessableEntity
        }
      })
    } else {
      Future(UnprocessableEntity)
    }
  }

  def validateAccount(encryptedEmailToken: String) = Action.async { implicit request =>
    val futureBoolean = authenticationServices.validateAccount(encryptedEmailToken)
    futureBoolean.map(resultOk => {
      if (resultOk) {
        Ok
      } else {
        UnprocessableEntity
      }
    })
  }

  def sendEmailValidation(email: String) = Action.async { implicit request =>
    val futureResult = authenticationServices.sendEmailValidation(email)
    futureResult.map(ResultOk => {
      if (ResultOk) {
        // TODO Send email
        Ok
      } else {
        NotFound
      }
    })
  }

  def changePassword = withToken(BodyParsers.parse.json) { authToken =>
    implicit request =>
      {
        val futureBoolean = authenticationServices.changePassword(authToken.userId, request.body.as[PasswordChange])
        futureBoolean.map(resultOk => {
          if (resultOk) {
            Ok.withHeaders(RequestUtil.getApiTokenHeader(authToken))
          } else {
            UnprocessableEntity
          }
        })
      }
  }
}