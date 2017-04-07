package v1.controllers

import scala.concurrent.Future

import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import v1.constantes.HttpConstants
import java.util.UUID
import v1.model.Person
import play.mvc.Http.Context
import play.api.libs.json.Json
import play.mvc.Security
import play.api.libs.json.JsValue
import play.Logger
import org.mindrot.jbcrypt.BCrypt
import v1.utils.SecurityUtil._
import v1.services.DocumentationServices
import v1.services.PersonServices
import v1.constantes.MessageConstants
import v1.model.Operation
import v1.services.AuthenticationServices
import v1.http.ApiToken
import org.apache.commons.lang3.StringUtils

class AuthenticationController @Inject() (
  val documentationServices: DocumentationServices,
  val authenticationServices: AuthenticationServices,
  val messagesApi: MessagesApi)
    extends Controller with I18nSupport with Secured {

  def index = Action.async { implicit request =>
    val rootUrl: String = routes.AuthenticationController.index.url
    val title: String = messagesApi(MessageConstants.title.documentation, rootUrl)
    val availableOperations: Seq[Operation] = documentationServices.getAuthenticationOperations
    render.async {
      case Accepts.Html() => Future.successful(Ok(v1.views.html.documentation(title, availableOperations)))
    }
  }

  // TODO check if already logged
  def signup = Action.async(BodyParsers.parse.json) { implicit request =>
    val futurePerson = authenticationServices.createAccount(request.body.as[Person])
    futurePerson.map {
      case (personOption, isNew) =>
        if (personOption.isDefined && personOption.get._id.isDefined) {
          var returnedLocation = HttpConstants.headerFields.location -> (routes.PersonController.getPerson(personOption.get._id.get, None).absoluteURL())
          if (isNew) {
            Created.withHeaders(returnedLocation)
          } else {
            Conflict(Json.toJson(personOption.get)).withHeaders(returnedLocation)
          }
        } else {
          UnprocessableEntity
        }
    }
  }

  def signin = Action.async(BodyParsers.parse.json) { implicit request =>
    val apiKeyOpt = request.headers.get(HttpConstants.headerFields.apiKey)
    if (apiKeyOpt.isDefined && ApiToken.apiKeysExists(apiKeyOpt.get)) {
      val person = request.body.as[Person]
      val futurePerson = authenticationServices.authenticate(person)
      val futureApiToken = futurePerson.flatMap(person => {
        if (person.isDefined && person.get._id.isDefined) {
          ApiToken.create(apiKeyOpt.get, person.get._id.get)
        } else {
          Future(null)
        }
      })
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

  def changePassword = Action.async(BodyParsers.parse.json) { implicit request =>
    val person = request.body.as[Person]
    Future(Ok)
  }
}