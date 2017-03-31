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
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import play.api.i18n.MessagesApi
import v1.bo.User
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter

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

  def addPerson(person: Person): Future[String] = {
    val futureWriteResult: Future[WriteResult] = personRepo.save(person)
    val noErrors: Boolean = Await.ready(handleWriteResult(futureWriteResult), Duration.Inf).value.get.getOrElse(false)
    if (noErrors) {
      futureWriteResult.map(writeRes =>
        person._id.get)
    } else {
      null
    }
  }

  def editPerson(id: String, person: Person): Future[Boolean] = {
    val futureWriteResult: Future[WriteResult] = personRepo.update(id, person)
    handleWriteResult(futureWriteResult)
  }

  def deletePerson(id: String): Future[Boolean] = {
    handleWriteResult(personRepo.remove(id))
  }

  def handleWriteResult(FutureWriteResult: Future[WriteResult]): Future[Boolean] = {
    FutureWriteResult.map(writeResult => {
      !writeResult.hasErrors && writeResult.n > 0
    })
  }
}
