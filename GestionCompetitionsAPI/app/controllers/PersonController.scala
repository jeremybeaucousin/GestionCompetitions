package controllers

import bo.Address
import bo.Person
import bo.Route
import constantes.MessageConstant
import java.util.Date
import java.util.Locale
import javax.inject._
import managers._
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

class PersonController @Inject() (val personManager: PersonManager, val messagesApi: MessagesApi)
    extends Controller with I18nSupport {

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  def index = Action.async { implicit request =>
    val rootUrl: String = routes.PersonController.index().url
    val title: String = messagesApi(MessageConstant.getDocumentationTitleMessageKey, rootUrl)

    var availableOperations: List[Route] = List[Route]()

    // TODO Create method to create all occurrences
    val operation: Call = routes.PersonController.addPerson()
    val parameters: Map[String, String] = Map[String, String]("parameter1" -> "parameterValue")
    val errors: Map[String, String] = Map[String, String]("error1" -> "errorValue")
    // TODO Create exemple generator
    val address: Address = Address(
        Some("name"),
        Some (0),
        Some ("streetName"),
        Some ("postalCode"))

    val personExemple = Person(
      Some("1"),
      Some("firstName"),
      Some("lastName"),
      Some(new Date),
      Some(List[Address](address)))
    val route = Route(
      Some(operation.method),
      Some(operation.url),
      Some(parameters.toMap),
      Some((Json.toJson(personExemple)).toString),
      Some(errors.toMap))
    availableOperations = route :: availableOperations

    render.async {
      case Accepts.Html() => Future.successful(Ok(views.html.personsDoc(title, availableOperations)))
      case Accepts.Json() => listPersons.apply(request)
    }
  }

  def listPersons = Action.async { implicit request =>
    val futurePersons = personManager.listPersons
    futurePersons.map { persons =>
      Ok(Json.toJson(persons))
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
      Created.withHeaders("Location" -> (request.host + "/persons/" + id))
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