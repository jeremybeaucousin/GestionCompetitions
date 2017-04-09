package v1.dal

import v1.dal.repos.AbstractRepoImpl
import java.io.{ StringWriter, PrintWriter }
import javax.inject.{ Inject, Singleton }
import play.api.Logger
import play.api.mvc.Results
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.ReadPreference
import scala.concurrent.Future
import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.util.{ Failure, Success }
import v1.constantes.MessageConstants
import scala.concurrent.duration.Duration
import play.api.i18n.MessagesApi
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter
import v1.utils.MongoDbUtil
import scala.concurrent.Await
import play.modules.reactivemongo.ReactiveMongoApi
import v1.model.Person
import reactivemongo.api.collections.bson.BSONCollection

  
class PersonDAO @Inject() (val reactiveMongoApi: ReactiveMongoApi)(
    implicit ec: ExecutionContext) {

  val collection = reactiveMongoApi.database.map(_.collection[BSONCollection](MongoDbUtil.PERSONS_COLLECTION))
  val personRepo: AbstractRepoImpl[Person] = new AbstractRepoImpl[Person](collection)
  
  def getTotalCount(personOption: Option[Person], searchInValues: Option[Boolean]): Future[Int] = {
    personRepo.getTotalCount(personOption, searchInValues)
  }

  def searchPersons(
    personOption: Option[Person],
    searchInValues: Option[Boolean],
    sortOption: Option[Seq[String]],
    fieldsOption: Option[Seq[String]],
    offsetOption: Option[Int],
    limitOption: Option[Int]): Future[List[Person]] = {
    personRepo.find(personOption, searchInValues: Option[Boolean], sortOption, fieldsOption, offsetOption, limitOption)
  }

  def getPerson(id: String, fieldsOption: Option[Seq[String]])(
    implicit bSONDocumentReader: BSONDocumentReader[Person],
    bSONDocumentWriter: BSONDocumentWriter[Person]): Future[Option[Person]] = {
    personRepo.select(id, fieldsOption)
  }

  def addPerson(document: Person)(
    implicit bSONDocumentReader: BSONDocumentReader[Person],
    bSONDocumentWriter: BSONDocumentWriter[Person]): Future[Option[Person]] = {
    val _id = MongoDbUtil.generateId().stringify
    val futureResult = personRepo.save(_id, document)
    val hasNoError = Await.result(futureResult, Duration.Inf)
    if (hasNoError) {
      val futurePerson = personRepo.select(_id, None)
      futurePerson.map(personInserted => {
        personInserted
      })
    } else {
      Future(None)
    }
  }

  def deleteFields(id: String, fields: List[String]): Future[Boolean] = {
    personRepo.deleteFields(id, fields)
  }

  def editPerson(id: String, document: Person): Future[Boolean] = {
    personRepo.update(id, document)
  }

  def deletePerson(id: String): Future[Boolean] = {
    personRepo.remove(id)
  }
}
