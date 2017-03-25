package controllers

import bo.Person
import managers._
import javax.inject._
import play.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc._
import play.modules.reactivemongo.{ MongoController, ReactiveMongoApi, ReactiveMongoComponents }
import reactivemongo.api.commands.WriteResult
import scala.concurrent.{ Future, ExecutionContext }
import scala.util.{ Failure, Success }

import reactivemongo.bson.{ BSONDocument, BSONObjectID }
import java.util.Date
import bo.Taekwondoist
import play.api.http.ContentTypes
import play.api.i18n.MessagesApi
import play.api.i18n.I18nSupport
import play.api.i18n.Lang
import java.util.Locale

class PersonController @Inject() (val personManager: PersonManager, val messagesApi: MessagesApi)
    extends Controller with I18nSupport {

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  def index = Action.async { implicit request =>
    render.async {
      case Accepts.Html() => Future.successful(Ok(views.html.personsDoc("Documentation")))
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