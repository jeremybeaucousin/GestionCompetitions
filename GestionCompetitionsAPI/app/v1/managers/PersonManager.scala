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

  def getTotalCount(personOption: Option[Person], searchInValues: Option[Boolean]): Future[Int] = {
    personDAO.getTotalCount(personOption, searchInValues)
  }

  def searchPersons(personOption: Option[Person], searchInValues: Option[Boolean], sortOption: Option[Seq[String]], fieldsOption: Option[Seq[String]], offsetOption: Option[Int], limitOption: Option[Int])(implicit messages: Messages): Future[List[Person]] = {
    personDAO.searchPersons(personOption, searchInValues, sortOption, fieldsOption, offsetOption, limitOption)
  }

  def getPerson(id: String, fieldsOption: Option[Seq[String]])(implicit messages: Messages): Future[Option[Person]] = {
    personDAO.getPerson(id, fieldsOption)
  }

  def addPerson(person: Person)(implicit messages: Messages): Future[String] = {
    personDAO.addPerson(person)
  }

  def editPerson(id: String, person: Person)(implicit messages: Messages): Future[Boolean] = {
    personDAO.editPerson(id, person)
  }

  def deletePerson(id: String)(implicit messages: Messages): Future[Boolean] = {
    personDAO.deletePerson(id)
  }
}
