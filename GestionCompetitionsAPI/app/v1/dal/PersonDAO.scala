package v1.dal

import v1.bo.Person
import v1.dal.repos.PersonRepoImpl
import java.io.{StringWriter, PrintWriter}
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

  def listPersons(sort: Option[Seq[String]], fields: Option[Seq[String]], offset: Option[Int], limit: Option[Int])(implicit messages: Messages): Future[List[Person]] = {
    personRepo.find(sort, fields, offset, limit)
  }
  
  def getPerson(id: String)(implicit messages: Messages): Future[Option[Person]] = {
    personRepo.select(id)
  }

  def addPerson(person: Person)(implicit messages: Messages): Future[String] = {
    val writeResult: Future[WriteResult] = personRepo.save(person)
    
    handleWriteResul(writeResult)

    writeResult.map(writeRes =>
      person._id.get)
  }

  def editPerson(id: String, person: Person)(implicit messages: Messages) = {
    val writeResult: Future[WriteResult] = personRepo.update(id, person)

    handleWriteResul(writeResult)
  }

  def deletePerson(id: String)(implicit messages: Messages) = {
    handleWriteResul(personRepo.remove(id))
  }

  private def handleWriteResul(writeResult: Future[WriteResult])(implicit messages: Messages) {
    writeResult.onComplete {
      case Failure(exception) => {
        val sw = new StringWriter
        exception.printStackTrace(new PrintWriter(sw))
        Logger.error(sw.toString)
      }
      case Success(writeResult) => {
        Logger.info(messages(MessageConstants.database.inserted, writeResult))
      }
    }
  }
}