package v1.controllers

import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.mvc.Action
import play.api.mvc.Controller
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class AuthenticationController @Inject() (
  val messagesApi: MessagesApi)
    extends Controller with I18nSupport {

  def signIn() = Action.async { implicit request =>
    Future(Ok("test"))
  }

  def signOut() = Action.async { implicit request =>
    Future(Ok("test"))
  }
}