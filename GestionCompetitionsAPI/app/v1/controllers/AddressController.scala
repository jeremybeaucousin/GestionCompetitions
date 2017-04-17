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
import play.api.libs.json.Json
import v1.constantes.HttpConstants

class AddressController @Inject() (
  val documentationServices: DocumentationServices,
  val personServices: PersonServices,
  val messagesApi: MessagesApi)
    extends Controller with I18nSupport with Secured {

  def index(userId: String, sort: Option[Seq[String]], fields: Option[Seq[String]]) = Action.async { implicit request =>
    val rootUrl: String = routes.AddressController.index(MongoDbUtil._ID, None, None).url
    val title: String = messagesApi(MessageConstants.title.documentation, rootUrl)
    val availableOperations: Seq[Operation] = documentationServices.getPersonAddressesOperations
    render.async {
      case Accepts.Html() => Future.successful(Ok(v1.views.html.documentation(title, availableOperations)))
      case Accepts.Json() => listAddresses(userId, sort, fields).apply(request)
    }
  }

  def listAddresses(userId: String, sort: Option[Seq[String]], fields: Option[Seq[String]]) = Action.async { implicit request =>
    val futureAddresses = personServices.addresses.getAddresses(userId, sort, fields)
    futureAddresses.map(addresses => {
      if (!addresses.isDefined || addresses.isEmpty) {
        NoContent
      } else {
        Ok(Json.toJson(addresses))
      }
    })
  }

  def getAddress(userId: String, index: Int, fields: Option[Seq[String]]) = Action.async { implicit request =>
    val futureAddress = personServices.addresses.getAddress(userId, index, fields)
    futureAddress.map(addressOption => {
      if (addressOption.isDefined) {
        Ok(Json.toJson(addressOption))
      } else {
        NotFound
      }
    })
  }

  def addAddress(userId: String) = Action.async { implicit request =>
    val futureAddress = personServices.addresses.addAddress(userId)
    futureAddress.map(indexOption => {
      if (indexOption.isDefined) {
        var returnedLocation = HttpConstants.headerFields.location -> (routes.AddressController.getAddress(userId, indexOption.get, None).absoluteURL())
        Created.withHeaders(returnedLocation)
      } else {
        UnprocessableEntity
      }
    })
  }

  def editAddress(userId: String, index: Int) = Action.async { implicit request =>
    val futurBoolean = personServices.addresses.editAddress(userId, index, null)
    futurBoolean.map { resultsOk =>
      if (resultsOk) {
        Ok
      } else {
        NotFound
      }
    }
  }

  def deleteAddress(userId: String, index: Int) = Action.async { implicit request =>
     val futurBoolean = personServices.addresses.deleteAddress(userId, index)
    futurBoolean.map { resultsOk =>
      if (resultsOk) {
        Ok
      } else {
        NotFound
      }
    }
  }

}