package v1.controllers

import v1.bo.Address
import v1.bo.Person
import v1.bo.Operation
import v1.constantes.MessageConstants
import java.util.Date
import java.util.Locale
import javax.inject._
import v1.managers._
import play.api._
import play.api.http.ContentTypes
import play.api.i18n.I18nSupport
import play.api.i18n.Lang
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc._
import play.modules.reactivemongo.{ MongoController, ReactiveMongoApi, ReactiveMongoComponents }
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{ BSONDocument, BSONObjectID }
import scala.concurrent.{ Future, ExecutionContext }
import scala.util.{ Failure, Success }
import scala.collection.mutable.Map
import v1.bo.Taekwondoist
import reactivemongo.bson.BSON
import v1.bo.Taekwondoist.TaekwondoistReader
import v1.utils.RequestUtil
import v1.constantes.HttpConstants

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
    val futurePersons = personManager.listPersons(sortOption, fieldsOption, offsetOption, limitOption)
    val totalCount = personManager.getTotalCount(None)
    futurePersons.map { persons =>
      var result = Ok(Json.toJson(persons))
      RequestUtil.managePagination(result, offsetOption, limitOption, totalCount)
    }
  }

  def searchPersons(sortOption: Option[Seq[String]], fieldsOption: Option[Seq[String]], offsetOption: Option[Int], limitOption: Option[Int]) = Action.async(BodyParsers.parse.json) { implicit request =>
    val person = request.body.as[Person]
    val futurePersons = personManager.searchPersons(person, sortOption, fieldsOption, offsetOption, limitOption)
    val totalCount = personManager.getTotalCount(Some(person))
    futurePersons.map { persons =>
      var result = Ok(Json.toJson(persons))
      RequestUtil.managePagination(result, offsetOption, limitOption, totalCount)
    }
  }

  def getPerson(id: String) = Action.async { implicit request =>
    val futurePerson = personManager.getPerson(id)
    futurePerson.map { person =>
      Ok(Json.toJson(person))
    }
  }

  def addPerson = Action.async(BodyParsers.parse.json) { implicit request =>
    val futureId = personManager.addPerson(request.body.as[Person])
    futureId.map { id =>
      Created.withHeaders(HttpConstants.headerFields.location -> (routes.PersonController.getPerson(id).absoluteURL()))
    }
  }

  def editPerson(id: String) = Action.async(BodyParsers.parse.json) { implicit request =>
    personManager.editPerson(id, request.body.as[Person])
    Future(Ok)
  }

  def deletePerson(id: String) = Action.async { implicit request =>
    personManager.deletePerson(id)
    Future(Ok)
  }
}