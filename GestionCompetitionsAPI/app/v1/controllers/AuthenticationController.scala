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

class AuthenticationController @Inject() (
  val messagesApi: MessagesApi)
    extends Controller with I18nSupport with Secured {

  def createPassword(clearString: String): String = {
    if (clearString == null) {
      throw new Exception("empty.password");
    }
    BCrypt.hashpw(clearString, BCrypt.gensalt());
  }

  def checkPassword(candidate: String, encryptedPassword: String): Boolean = {
    if (candidate == null) {
      false
    }
    if (encryptedPassword == null) {
      false
    }
    BCrypt.checkpw(candidate, encryptedPassword);
  }

  def signup = Action.async { implicit request =>
    val ecryptedTest = createPassword("test")
    val ecryptedTest2 = createPassword("test2")
    Logger.info(ecryptedTest)
    Logger.info(checkPassword("test", ecryptedTest).toString())
    Logger.info(checkPassword("test2", ecryptedTest).toString())
    Logger.info(checkPassword("test1", ecryptedTest2).toString())
    Logger.info(checkPassword("test2", ecryptedTest2).toString())
    
    Future(Ok)
  }

  def signin = Action.async { implicit request =>

    // TODO replace fake ID by real user

    val apiKeyOpt = request.headers.get(HttpConstants.headerFields.HEADER_API_KEY)
    if (apiKeyOpt.isDefined && ApiToken.apiKeysExists(apiKeyOpt.get)) {
      ApiToken.create(apiKeyOpt.get, "Fake_ID").flatMap { token =>
        Future(Ok(
          Json.obj(
            "token" -> token,
            "minutes" -> ApiToken.TOKEN_DURATION)))
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
}