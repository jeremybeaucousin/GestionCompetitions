package v1.controllers

import scala.concurrent.Future

import javax.inject.Inject
import javax.inject.Singleton
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Action
import play.api.mvc.Controller
import v1.services.DocumentationServices
import v1.services.PersonServices
import v1.constantes.MessageConstants
import v1.model.Operation
import play.Logger
import org.apache.commons.lang3.StringUtils
import v1.utils.MongoDbUtil

class PhoneController @Inject() (
  val documentationServices: DocumentationServices,
  val personServices: PersonServices,
  val messagesApi: MessagesApi)
    extends Controller with I18nSupport with Secured {

  def index(userId: String) = Action.async { implicit request =>
    val rootUrl: String = routes.PhoneController.index(MongoDbUtil._ID).url
    val title: String = messagesApi(MessageConstants.title.documentation, rootUrl)
    val availableOperations: Seq[Operation] = documentationServices.getPersonPhonesOperations
    
    
    Future.successful(Ok(v1.views.html.documentation(title, availableOperations)))
  }

  def listPhones(userId: String, sort: Option[Seq[String]], fields: Option[Seq[String]]) = Action.async { implicit request =>
    Future(Ok)
  }

  def addPhone(userId: String) = Action.async { implicit request =>
    Future(Ok)
  }

  def getPhone(userId: String, key: String) = Action.async { implicit request =>
    Future(Ok)
  }

  def editPhone(userId: String, key: String) = Action.async { implicit request =>
    Future(Ok)
  }

  def deletePhone(userId: String, key: String) = Action.async { implicit request =>
    Future(Ok)
  }

}