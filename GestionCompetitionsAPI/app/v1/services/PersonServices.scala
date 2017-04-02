package v1.services

import v1.bo.Person
import v1.dal.PersonDAO
import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import errors.HomonymNamesException
import errors.HomonymNamesAndBirthDateException
import play.api.i18n.Messages
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter
import play.Logger

class PersonManager @Inject() (val personDAO: PersonDAO[Person])(implicit val ec: ExecutionContext) {

  def getTotalCount(personOption: Option[Person], searchInValues: Option[Boolean]): Future[Int] = {
    personDAO.getTotalCount(personOption, searchInValues)
  }

  def searchPersons(personOption: Option[Person], searchInValues: Option[Boolean], sortOption: Option[Seq[String]], fieldsOption: Option[Seq[String]], offsetOption: Option[Int], limitOption: Option[Int]): Future[List[Person]] = {
    personDAO.searchPersons(personOption, searchInValues, sortOption, fieldsOption, offsetOption, limitOption)
  }

  def getPerson(id: String, fieldsOption: Option[Seq[String]]): Future[Option[Person]] = {
    personDAO.getPerson(id, fieldsOption)
  }

  def addPerson(person: Person)(implicit messages: Messages): Future[String] = {
    def searchHomonyme(personRequest: Person): Boolean = {
      val futurePersonResult = personDAO.searchPersons(Some(personRequest), None, None, None, None, None)
      val personResult = Await.ready(futurePersonResult, Duration.Inf).value.get.get
      !personResult.isEmpty
    }

    val personSearch = new Person(None, person.firstName, person.lastName, person.birthDate, None)
    if (person.birthDate.isDefined) {
      if (searchHomonyme(personSearch)) {
        throw new HomonymNamesAndBirthDateException
      }
    } else {
      personSearch.birthDate = None
      if (searchHomonyme(personSearch)) {
        throw new HomonymNamesException
      }
    }
    personDAO.addPerson(person)
  }

  def editPerson(id: String, person: Person): Future[Boolean] = {
    personDAO.editPerson(id, person)
  }

  def deletePerson(id: String): Future[Boolean] = {
    personDAO.deletePerson(id)
  }
}
