package controllers

import bo.Person
import bo.Taekwondoist
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
    val rootUrl:String = routes.PersonController.index().url
    val title:String = messagesApi(MessageConstant.getDocumentationTitleMessageKey, rootUrl)
    
    val availableOperations:Map[String, String] = Map[String, String]()
    
    // TODO Create method to create all occurrences
    val addPerson = routes.PersonController.addPerson()
    availableOperations += (addPerson.method -> addPerson.url)
    
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