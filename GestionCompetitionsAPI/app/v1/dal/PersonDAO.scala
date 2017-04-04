package v1.dal

import v1.bo.Person
import v1.dal.repos.PersonRepoImpl
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

class PersonDAO[T] @Inject() (val personRepo: PersonRepoImpl[T])(
    implicit ec: ExecutionContext) {

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
    implicit bSONDocumentReader: BSONDocumentReader[T],
    bSONDocumentWriter: BSONDocumentWriter[T]): Future[Option[T]] = {
    personRepo.select(id, fieldsOption)
  }

  def addPerson(person: Person)(
    implicit bSONDocumentReader: BSONDocumentReader[T],
    bSONDocumentWriter: BSONDocumentWriter[T]): Future[Option[T]] = {
    val _id = MongoDbUtil.generateId().stringify
    person._id = Some(_id)
    val futureWriteResult = personRepo.save(person)
    var futureResult = handleWriteResult(futureWriteResult)
    val hasNoError = Await.ready(futureResult, Duration.Inf).value.get.get
    if (hasNoError) {
        personRepo.select(_id, None)
      } else {
        Future(None)
      }
  }

  def editPerson(id: String, person: Person): Future[Boolean] = {
    personRepo.update(id, person)
  }

  def deletePerson(id: String): Future[Boolean] = {
    personRepo.remove(id)
  }

  private def handleWriteResult(futureWriteResult: Future[WriteResult]): Future[Boolean] = {
    futureWriteResult.map(writeResult => {
      !writeResult.hasErrors && writeResult.n > 0
    })
  }
}
