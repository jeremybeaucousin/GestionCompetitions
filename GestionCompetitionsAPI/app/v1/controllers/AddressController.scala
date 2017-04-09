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

class AddressController @Inject() (
  val documentationServices: DocumentationServices,
  val personServices: PersonServices,
  val messagesApi: MessagesApi)
    extends Controller with I18nSupport with Secured {

  def index(id: String) = Action.async { implicit request =>
    val rootUrl: String = routes.AddressController.index(MongoDbUtil._ID).url
    val title: String = messagesApi(MessageConstants.title.documentation, rootUrl)
    val availableOperations: Seq[Operation] = documentationServices.getPersonAddressesOperations
    Future.successful(Ok(v1.views.html.documentation(title, availableOperations)))
  }

  def listAddresses(id: String) = Action.async { implicit request =>
    Future(Ok)
  }

  def addAddress(id: String) = Action.async { implicit request =>
    Future(Ok)
  }

  def getAddress(id: String, index: String) = Action.async { implicit request =>
    Future(Ok)
  }

  def editAddress(id: String, index: String) = Action.async { implicit request =>
    Future(Ok)
  }

  def deleteAddress(id: String, index: String) = Action.async { implicit request =>
    Future(Ok)
  }

}