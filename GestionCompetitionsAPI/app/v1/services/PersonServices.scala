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
import v1.utils.SecurityUtil
import errors.EmailAlreadyRegisterdException

class PersonManager @Inject() (val personDAO: PersonDAO[Person])(implicit val ec: ExecutionContext) {

  def getTotalCount(personOption: Option[Person], searchInValues: Option[Boolean]): Future[Int] = {
    personDAO.getTotalCount(personOption, searchInValues)
  }

  def searchPersons(personOption: Option[Person], searchInValues: Option[Boolean], sortOption: Option[Seq[String]], fieldsOption: Option[Seq[String]], offsetOption: Option[Int], limitOption: Option[Int]): Future[List[Person]] = {
    personDAO.searchPersons(personOption, searchInValues, sortOption, fieldsOption, offsetOption, limitOption)
  }

  // send back the personn such as Add person
  def createAccount(person: Person)(implicit messages: Messages) = {
    if (person.email.isDefined && person.password.isDefined) {
      person.encryptedPassword = Some(SecurityUtil.createPassword(person.password.get))
      addPerson(person)
    }
  }

  def searchPersonWithEmail(person: Person): Future[List[Person]] = {
    val personWithEmainOnly = Person()
    personWithEmainOnly.email = person.email
    personDAO.searchPersons(Some(personWithEmainOnly), None, None, None, None, None)
  }
  def authenticate(person: Person): Future[Option[Person]] = {
    if (person.email.isDefined && person.email.isDefined) {
      val futurePersons = searchPersonWithEmail(person)
      val persons = Await.ready(futurePersons, Duration.Inf).value.get.get
      if (!persons.isEmpty) {
        val firstPerson = persons(0)
        if (firstPerson.encryptedPassword.isDefined && SecurityUtil.checkPassword(person.password.get, firstPerson.encryptedPassword.get)) {
          Future(Some(firstPerson))
        } else {
          Future(None)
        }
      } else {
        Future(None)
      }
    } else {
      Future(None)
    }

  }

  def getPerson(id: String, fieldsOption: Option[Seq[String]]): Future[Option[Person]] = {
    personDAO.getPerson(id, fieldsOption)
  }

  // TODO Send back a person instead of just the ID
  def addPerson(person: Person)(implicit messages: Messages): Future[String] = {
    def searchHomonyme(personRequest: Person): Boolean = {
      val futurePersonResult = personDAO.searchPersons(Some(personRequest), None, None, None, None, None)
      val personResult = Await.ready(futurePersonResult, Duration.Inf).value.get.get
      !personResult.isEmpty
    }

    if (person.email.isDefined) {
      Logger.info(person.email.get)
      val futurePersons = searchPersonWithEmail(person)
      val personResult = Await.ready(futurePersons, Duration.Inf).value.get.get
      Logger.info(personResult.toString())
      if (!personResult.isEmpty) {
        val personWithActiveAccount = personResult.find(person => person.encryptedPassword.isDefined)
        if (personWithActiveAccount.isDefined) {
          Logger.info(personWithActiveAccount.get.email.get)
          throw new EmailAlreadyRegisterdException
        }
      }
    }
    val personSearch = Person()
    personSearch.firstName = person.firstName
    personSearch.lastName = person.lastName
    personSearch.birthDate = person.birthDate
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
