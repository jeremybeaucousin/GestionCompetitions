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
import v1.managers.DocumentationManager
import v1.services.PersonManager
import v1.constantes.MessageConstants
import v1.bo.Operation

class AuthenticationController @Inject() (
  val documentationManager: DocumentationManager,
  val personManager: PersonManager,
  val messagesApi: MessagesApi)
    extends Controller with I18nSupport with Secured {

  def index = Action.async { implicit request =>
    val rootUrl: String = routes.AuthenticationController.index.url
    val title: String = messagesApi(MessageConstants.title.documentation, rootUrl)
    val availableOperations: Seq[Operation] = documentationManager.getAuthenticationOperations
    render.async {
      case Accepts.Html() => Future.successful(Ok(v1.views.html.documentation(title, availableOperations)))
    }
  }

  def signup = Action.async { implicit request =>
    val ecryptedTest = createPassword("test")
    val ecryptedTest2 = createPassword("test2")
    //    Logger.info(ecryptedTest)
    //    Logger.info(checkPassword("test", ecryptedTest).toString())
    //    Logger.info(checkPassword("test2", ecryptedTest).toString())
    //    Logger.info(checkPassword("test1", ecryptedTest2).toString())
    //    Logger.info(checkPassword("test2", ecryptedTest2).toString())
    ApiToken.cleanTokenStore

    Future(Ok)
  }

  def signin = Action.async { implicit request =>

    // TODO replace fake ID by real user

    val apiKeyOpt = request.headers.get(HttpConstants.headerFields.apiKey)
    if (apiKeyOpt.isDefined && ApiToken.apiKeysExists(apiKeyOpt.get)) {
      ApiToken.create(apiKeyOpt.get, "Fake_ID").flatMap { token =>
        Future(Ok(
          Json.obj(
            ApiToken.TOKEN_FIELD  -> token,
            ApiToken.DURATION_FIELD -> ApiToken.TOKEN_DURATION)))
      }
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