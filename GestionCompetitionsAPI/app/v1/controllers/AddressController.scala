package v1.controllers

import scala.concurrent.Future

import javax.inject.Inject
import javax.inject.Singleton
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.BodyParsers
import play.api.mvc.Controller
import v1.model.Operation
import v1.model.Person
import v1.constantes.HttpConstants
import v1.constantes.MessageConstants
import v1.services.DocumentationServices
import v1.services.PersonServices
import v1.utils.RequestUtil
import play.Logger
import reactivemongo.bson.BSONDocumentReader
import v1.model.Person.PersonReader
import reactivemongo.bson.BSONDocumentWriter

class AddressController @Inject() (
  val documentationServices: DocumentationServices,
  val personServices: PersonServices,
  val messagesApi: MessagesApi)
    extends Controller with I18nSupport with Secured {

  def adresses = Action.async { implicit request =>
    Future(Ok)
  }

  def addAddress = Action.async { implicit request =>
    Future(Ok)
  }

  def getAddress(index: String) = Action.async { implicit request =>
    Future(Ok)
  }

  def editAddress(index: String) = Action.async { implicit request =>
    Future(Ok)
  }

  def deleteAddress(index: String) = Action.async { implicit request =>
    Future(Ok)
  }

}