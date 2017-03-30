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
import v1.managers.DocumentationManager
import v1.managers.PersonManager
import v1.utils.RequestUtil
import play.Logger

class PersonController @Inject() (val documentationManager: DocumentationManager, val personManager: PersonManager, val messagesApi: MessagesApi)
    extends Controller with I18nSupport {

  def index(sort: Option[Seq[String]], fields: Option[Seq[String]], offset: Option[Int], limit: Option[Int]) = Action.async { implicit request =>
    val rootUrl: String = routes.PersonController.index(None, None, None, None).url
    val title: String = messagesApi(MessageConstants.title.documentation, rootUrl)
    val availableOperations: Seq[Operation] = documentationManager.getPersonOperations
    render.async {
      case Accepts.Html() => Future.successful(Ok(v1.views.html.documentation(title, availableOperations)))
      case Accepts.Json() => listPersons(sort, fields, offset, limit).apply(request)
    }
  }

  def listPersons(sortOption: Option[Seq[String]], fieldsOption: Option[Seq[String]], offsetOption: Option[Int], limitOption: Option[Int]) = Action.async { implicit request =>
    val futurePersons = personManager.searchPersons(None, None, sortOption, fieldsOption, offsetOption, limitOption)
    val totalCount = personManager.getTotalCount(None, None)
    futurePersons.map { persons =>
      if (persons.isEmpty) {
        NoContent
      } else {
        var result = Ok(Json.toJson(persons))
        RequestUtil.managePagination(result, offsetOption, limitOption, totalCount)
      }
    }
  }

  def searchPersons(sortOption: Option[Seq[String]], fieldsOption: Option[Seq[String]], offsetOption: Option[Int], limitOption: Option[Int]) = Action.async(BodyParsers.parse.json) { implicit request =>
    val person = request.body.as[Person]
    val futurePersons = personManager.searchPersons(Some(person), Some(true), sortOption, fieldsOption, offsetOption, limitOption)
    val totalCount = personManager.getTotalCount(Some(person), Some(true))
    futurePersons.map { persons =>
      if (persons.isEmpty) {
        NoContent
      } else {
        var result = Ok(Json.toJson(persons))
        RequestUtil.managePagination(result, offsetOption, limitOption, totalCount)
      }
    }
  }

  def getPerson(id: String, fieldsOption: Option[Seq[String]]) = Action.async { implicit request =>
    val futurePerson = personManager.getPerson(id, fieldsOption)
    futurePerson.map { person =>
      if (person.isDefined) {
        Ok(Json.toJson(person))
      } else {
        NotFound
      }

    }
  }

  def addPerson = Action.async(BodyParsers.parse.json) { implicit request =>
    val futureId = personManager.addPerson(request.body.as[Person])
    futureId.map { id =>
      BAD_REQUEST
      if (id != null && !id.isEmpty()) {
        Created.withHeaders(HttpConstants.headerFields.location -> (routes.PersonController.getPerson(id, None).absoluteURL()))
      } else {
        UnprocessableEntity
      }
    }
  }

  def editPerson(id: String) = Action.async(BodyParsers.parse.json) { implicit request =>
    val futurBoolean: Future[Boolean] = personManager.editPerson(id, request.body.as[Person])
    futurBoolean.map { resultsOk =>
      if (resultsOk) {
        Ok
      } else {
        UnprocessableEntity
      }
    }
  }

  def deletePerson(id: String) = Action.async { implicit request =>
    val futurBoolean: Future[Boolean] = personManager.deletePerson(id)
    futurBoolean.map { resultsOk =>
      if (resultsOk) {
        Ok
      } else {
        UnprocessableEntity
      }
    }
  }
}