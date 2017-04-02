package v1.controllers

import scala.concurrent.Future

import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import v1.constantes.HttpConstants
import java.util.UUID
import v1.bo.Person
import play.mvc.Http.Context
import models.ApiToken
import play.api.libs.json.Json
import play.mvc.Security
import play.api.libs.json.JsValue
import play.Logger
import org.mindrot.jbcrypt.BCrypt
import v1.utils.SecurityUtil._
import v1.managers.DocumentationServices
import v1.services.PersonServices
import v1.constantes.MessageConstants
import v1.bo.Operation

class AuthenticationController @Inject() (
  val documentationServices: DocumentationServices,
  val personServices: PersonServices,
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

  def signup = Action.async(BodyParsers.parse.json) { implicit request =>
    val person = request.body.as[Person]
    personServices.createAccount(person)
    Future(Ok)
  }

  // TODO Externalyse this or opimize to avoir successive else
  def signin = Action.async(BodyParsers.parse.json) { implicit request =>
    val apiKeyOpt = request.headers.get(HttpConstants.headerFields.apiKey)
    if (apiKeyOpt.isDefined && ApiToken.apiKeysExists(apiKeyOpt.get)) {
      val person = request.body.as[Person]
      val futurePerson = personServices.authenticate(person)
      futurePerson.flatMap(person => {
        if (person.isDefined && person.get._id.isDefined) {
          ApiToken.create(apiKeyOpt.get, person.get._id.get).map(token =>
            Ok(
              Json.obj(
                ApiToken.TOKEN_FIELD -> token,
                ApiToken.DURATION_FIELD -> ApiToken.TOKEN_DURATION)))
        } else {
          Future(Forbidden)
        }
      })
    } else {
      Future(Forbidden)
    }
  }

  def signout = withToken { authToken =>
    implicit request =>
      ApiToken.delete(authToken)
      Future(NoContent)
  }

  def reset = Action.async { implicit request =>
    Future(Ok)
  }
}