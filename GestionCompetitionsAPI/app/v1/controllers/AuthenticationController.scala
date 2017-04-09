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

class AuthenticationController @Inject() (
  val documentationServices: DocumentationServices,
  val authenticationServices: AuthenticationServices,
  val mailServices: MailServices,
  val messagesApi: MessagesApi)
    extends Controller with I18nSupport with Secured {

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
      val person = request.body.as[Person]
      val futurePerson = authenticationServices.authenticate(person)
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
        Future(null)
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
      if(ResultOk) {
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
        Logger.info(authToken.userId)
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