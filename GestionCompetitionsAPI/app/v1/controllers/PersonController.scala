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
import v1.bo.Operation
import v1.bo.Person
import v1.constantes.HttpConstants
import v1.constantes.MessageConstants
import v1.managers.DocumentationServices
import v1.services.PersonServices
import v1.utils.RequestUtil
import play.Logger
import reactivemongo.bson.BSONDocumentReader
import v1.bo.Person.PersonReader
import reactivemongo.bson.BSONDocumentWriter

class PersonController @Inject() (
  val documentationServices: DocumentationServices,
  val personServices: PersonServices,
  val messagesApi: MessagesApi)
    extends Controller with I18nSupport with Secured {

  def index(sort: Option[Seq[String]], fields: Option[Seq[String]], offset: Option[Int], limit: Option[Int]) = Action.async { implicit request =>
    val rootUrl: String = routes.PersonController.index(None, None, None, None).url
    val title: String = messagesApi(MessageConstants.title.documentation, rootUrl)
    val availableOperations: Seq[Operation] = documentationServices.getPersonOperations
    render.async {
      case Accepts.Html() => Future.successful(Ok(v1.views.html.documentation(title, availableOperations)))
      case Accepts.Json() => listPersons(sort, fields, offset, limit).apply(request)
    }
  }

  def listPersons(sortOption: Option[Seq[String]], fieldsOption: Option[Seq[String]], offsetOption: Option[Int], limitOption: Option[Int]) = Action.async { implicit request =>
    val futurePersons = personServices.searchPersons(None, None, sortOption, fieldsOption, offsetOption, limitOption)
    val futurTotalCount = personServices.getTotalCount(None, None)
    RequestUtil.handleFutureListAndTotal(futurePersons, futurTotalCount).map({
      case (persons, totalCount) => {
        if (persons.isEmpty) {
          NoContent
        } else {
          var result = Ok(Json.toJson(persons))
          RequestUtil.handlePagination(result, offsetOption, limitOption, totalCount)
        }
      }
    })
  }

  def searchPersons(sortOption: Option[Seq[String]], fieldsOption: Option[Seq[String]], offsetOption: Option[Int], limitOption: Option[Int]) = Action.async(BodyParsers.parse.json) { implicit request =>
    val person = request.body.as[Person]
    val futurePersons = personServices.searchPersons(Some(person), Some(true), sortOption, fieldsOption, offsetOption, limitOption)
    val futurTotalCount = personServices.getTotalCount(Some(person), Some(true))
    RequestUtil.handleFutureListAndTotal(futurePersons, futurTotalCount).map({
      case (persons, totalCount) => {
        if (persons.isEmpty) {
          NoContent
        } else {
          var result = Ok(Json.toJson(persons))
          RequestUtil.handlePagination(result, offsetOption, limitOption, totalCount)
        }
      }
    })
  }

  // TODO def getPerson(id: String, fieldsOption: Option[Seq[String]]) = withToken { authToken =>
  def getPerson(id: String, fieldsOption: Option[Seq[String]]) = Action.async {
    { implicit request =>
      val futurePerson: Future[Option[Person]] = personServices.getPerson(id, fieldsOption)
      futurePerson.map { person =>
        Logger.info(person.get.encryptedEmailToken.toString())
        if (person.isDefined) {
          Ok(Json.toJson(person))
        } else {
          NotFound
        }
      }
    }
  }

  def addPerson = Action.async(BodyParsers.parse.json) { implicit request =>
    val futurePerson = personServices.addPerson(request.body.as[Person])
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

  def editPerson(id: String) = Action.async(BodyParsers.parse.json) { implicit request =>
    val futurBoolean: Future[Boolean] = personServices.editPerson(id, request.body.as[Person])
    futurBoolean.map { resultsOk =>
      if (resultsOk) {
        Ok
      } else {
        NotFound
      }
    }
  }

  def deletePerson(id: String) = Action.async { implicit request =>
    val futurBoolean: Future[Boolean] = personServices.deletePerson(id)
    futurBoolean.map { resultsOk =>
      if (resultsOk) {
        Ok
      } else {
        NotFound
      }
    }
  }
}