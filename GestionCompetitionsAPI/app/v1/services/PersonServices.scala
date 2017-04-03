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
import errors.FirstNameAndLastNameRequiredException

class PersonServices @Inject() (val personDAO: PersonDAO[Person])(implicit val ec: ExecutionContext) {

  def getTotalCount(personOption: Option[Person], searchInValues: Option[Boolean]): Future[Int] = {
    personDAO.getTotalCount(personOption, searchInValues)
  }

  def searchPersons(personOption: Option[Person], searchInValues: Option[Boolean], sortOption: Option[Seq[String]], fieldsOption: Option[Seq[String]], offsetOption: Option[Int], limitOption: Option[Int]): Future[List[Person]] = {
    personDAO.searchPersons(personOption, searchInValues, sortOption, fieldsOption, offsetOption, limitOption)
  }

  def searchPersonWithEmail(person: Person): Future[List[Person]] = {
    val personWithEmainOnly = Person()
    personWithEmainOnly.email = person.email
    personDAO.searchPersons(Some(personWithEmainOnly), None, None, None, None, None)
  }

  def getPerson(id: String, fieldsOption: Option[Seq[String]]): Future[Option[Person]] = {
    personDAO.getPerson(id, fieldsOption)
  }

  /**
   * Add the person only if :
   * - The email note exists and is not registered (with encrypted password)
   * - There is no homonym in the data base (firstName, lastName, BirthDate)
   *
   * If there is an existing email which is no active (without encrypted password) send back this user else
   * add the save the new Person
   * @param person
   * @param messages
   * @return
   */
  // TODO add test one first name and last Name
  def addPerson(person: Person)(implicit messages: Messages): Future[(Option[Person], Boolean)] = {
    def searchHomonyme(personRequest: Person): Boolean = {
      val futurePersonResult = personDAO.searchPersons(Some(personRequest), None, None, None, None, None)
      val personResult = Await.ready(futurePersonResult, Duration.Inf).value.get.get
      !personResult.isEmpty
    }

    if(!person.firstName.isDefined || !person.lastName.isDefined) {
      throw new FirstNameAndLastNameRequiredException
    }
    
    if (person.email.isDefined) {
      val futurePersons = searchPersonWithEmail(person)
      val personsWithSameEmail = Await.ready(futurePersons, Duration.Inf).value.get.get
      if (!personsWithSameEmail.isEmpty) {
        val personWithSameEmail = personsWithSameEmail.find(personWithSameEmail => personWithSameEmail.email.get.equals(person.email.get))
        Logger.info(personWithSameEmail.isDefined.toString())
        if (personWithSameEmail.isDefined && personWithSameEmail.get.encryptedPassword.isDefined) {
          throw new EmailAlreadyRegisterdException
        } else {
          return Future(personWithSameEmail, false)
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
    val personOption =  Await.ready(personDAO.addPerson(person), Duration.Inf).value.get.get
    Future(personOption, true)
  }

  def editPerson(id: String, person: Person): Future[Boolean] = {
    personDAO.editPerson(id, person)
  }

  def deletePerson(id: String): Future[Boolean] = {
    personDAO.deletePerson(id)
  }
}
