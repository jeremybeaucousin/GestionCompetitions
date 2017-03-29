package v1.managers

import v1.bo.Person
import v1.dal.PersonDAO
import javax.inject.{ Inject, Singleton }
import play.api.inject.ApplicationLifecycle
import play.api.Logger
import reactivemongo.api.commands.WriteResult
import scala.concurrent.Future
import scala.concurrent.{ ExecutionContext, Future }
import play.api.i18n.Messages

@Singleton
class PersonManager @Inject() (val personDAO: PersonDAO)(implicit val ec: ExecutionContext) {

  def getTotalCount(personOption: Option[Person]): Future[Int] = {
    personDAO.getTotalCount(personOption)
  }

  def listPersons(sortOption: Option[Seq[String]], fieldsOption: Option[Seq[String]], offsetOption: Option[Int], limitOption: Option[Int])(implicit messages: Messages): Future[List[Person]] = {
    personDAO.listPersons(sortOption, fieldsOption, offsetOption, limitOption)
  }

  def searchPersons(person: Person, sortOption: Option[Seq[String]], fieldsOption: Option[Seq[String]], offsetOption: Option[Int], limitOption: Option[Int])(implicit messages: Messages): Future[List[Person]] = {
    personDAO.searchPersons(person, sortOption, fieldsOption, offsetOption, limitOption)
  }

  def getPerson(id: String)(implicit messages: Messages): Future[Option[Person]] = {
    personDAO.getPerson(id)
  }

  def addPerson(person: Person)(implicit messages: Messages): Future[String] = {
    personDAO.addPerson(person)
  }

  def editPerson(id: String, person: Person)(implicit messages: Messages) = {
    personDAO.editPerson(id, person)
  }

  def deletePerson(id: String)(implicit messages: Messages) = {
    personDAO.deletePerson(id)
  }
}
