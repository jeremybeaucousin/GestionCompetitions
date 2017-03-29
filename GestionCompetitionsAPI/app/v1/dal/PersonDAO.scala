package v1.dal

import v1.bo.Person
import v1.dal.repos.PersonRepoImpl
import java.io.{ StringWriter, PrintWriter }
import javax.inject.{ Inject, Singleton }
import play.api.Logger
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.ReadPreference
import scala.concurrent.Future
import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.util.{ Failure, Success }
import play.api.i18n.Messages
import v1.constantes.MessageConstants

@Singleton
class PersonDAO @Inject() (val personRepo: PersonRepoImpl)(implicit ec: ExecutionContext) {

  def getTotalCount(personOption: Option[Person]): Future[Int] = {
    personRepo.getTotalCount(personOption)
  }

  def listPersons(sort: Option[Seq[String]], fields: Option[Seq[String]], offset: Option[Int], limit: Option[Int])(implicit messages: Messages): Future[List[Person]] = {
    personRepo.find(None, sort, fields, offset, limit)
  }

  def searchPersons(person: Person, sortOption: Option[Seq[String]], fieldsOption: Option[Seq[String]], offsetOption: Option[Int], limitOption: Option[Int])(implicit messages: Messages): Future[List[Person]] = {
    personRepo.find(Some(person), sortOption, fieldsOption, offsetOption, limitOption)
  }

  def getPerson(id: String)(implicit messages: Messages): Future[Option[Person]] = {
    personRepo.select(id)
  }

  def addPerson(person: Person)(implicit messages: Messages): Future[String] = {
    val writeResult: Future[WriteResult] = personRepo.save(person)

    handleWriteResult(writeResult)

    writeResult.map(writeRes =>
      person._id.get)
  }

  def editPerson(id: String, person: Person)(implicit messages: Messages) = {
    val writeResult: Future[WriteResult] = personRepo.update(id, person)

    handleWriteResult(writeResult)
  }

  def deletePerson(id: String)(implicit messages: Messages) = {
    handleWriteResult(personRepo.remove(id))
  }

  private def handleWriteResult(writeResult: Future[WriteResult])(implicit messages: Messages) {
    writeResult.onComplete {
      case Failure(exception) => {
        val sw = new StringWriter
        exception.printStackTrace(new PrintWriter(sw))
        Logger.error(sw.toString)
      }
      case Success(writeResult) => {
        if(writeResult.hasErrors && writeResult.n == 0) {
          
        }
        Logger.info(messages(MessageConstants.database.inserted, writeResult))
      }
    }
  }
}
