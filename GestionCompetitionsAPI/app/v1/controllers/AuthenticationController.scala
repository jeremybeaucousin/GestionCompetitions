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

class AuthenticationController @Inject() (
  val messagesApi: MessagesApi)
    extends Controller with I18nSupport with Secured {

  def login = Action.async { implicit request =>

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

  def logout = withToken { authToken =>
    implicit request =>
      Future(Ok(authToken.expirationTime.toString()))
//      ApiToken.delete(authToken)
//      NoContent
  }
}