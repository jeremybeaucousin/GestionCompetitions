package dal

import bo.Person
import dal.repos.PersonRepoImpl
import java.io.{StringWriter, PrintWriter}
import javax.inject.{ Inject, Singleton }
import play.api.Logger
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.ReadPreference
import scala.concurrent.Future
import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.util.{ Failure, Success }
import bo.Taekwondoist

@Singleton
class PersonDAO @Inject() (val personRepo: PersonRepoImpl)(implicit ec: ExecutionContext) {
  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  def getPerson(id: String)(implicit ec: ExecutionContext): Future[Option[Person]] = {
    personRepo.select(id)
  }

  def addPerson(person: Person)(implicit ec: ExecutionContext): Future[String] = {
    val writeResult: Future[WriteResult] = personRepo.save(person)
    
    handleWriteResul(writeResult)

    writeResult.map(writeRes =>
      person._id.get)
  }

  def editPerson(id: String, person: Person)(implicit ec: ExecutionContext) = {
    val writeResult: Future[WriteResult] = personRepo.update(id, person)

    handleWriteResul(writeResult)
  }

  def deletePerson(id: String)(implicit ec: ExecutionContext) = {
    handleWriteResul(personRepo.remove(id))
  }

  private def handleWriteResul(writeResult: Future[WriteResult]) {
    writeResult.onComplete {
      case Failure(exception) => {
        val sw = new StringWriter
        exception.printStackTrace(new PrintWriter(sw))
        logger.error(sw.toString)
      }
      case Success(writeResult) => {
        logger.info(s"successfully inserted document with result: $writeResult")
      }
    }
  }
}
