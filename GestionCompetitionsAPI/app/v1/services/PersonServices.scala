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
import errors.LoginCannotBeSetException

class PersonServices @Inject() (val personDAO: PersonDAO[Person])(implicit val ec: ExecutionContext) {

  def getTotalCount(personOption: Option[Person], searchInValues: Option[Boolean]): Future[Int] = {
    personDAO.getTotalCount(personOption, searchInValues)
  }

  def searchPersons(personOption: Option[Person], searchInValues: Option[Boolean], sortOption: Option[Seq[String]], fieldsOption: Option[Seq[String]], offsetOption: Option[Int], limitOption: Option[Int]): Future[List[Person]] = {
    personDAO.searchPersons(personOption, searchInValues, sortOption, fieldsOption, offsetOption, limitOption)
  }

  def searchPersonWithEmail(person: Person): Future[Option[Person]] = {
    val personWithEmainOnly = Person()
    personWithEmainOnly.email = person.email
    var futurePersons = personDAO.searchPersons(Some(personWithEmainOnly), None, None, None, None, None)
    futurePersons.map(personsWithSameEmail => {
      if (!personsWithSameEmail.isEmpty) {
        personsWithSameEmail.find(personWithSameEmail => personWithSameEmail.email.get.equals(person.email.get))
      } else {
        None
      }
    })
  }
  
  def searchPersonWithLogin(person: Person): Future[Option[Person]] = {
    val personWithLoginOnly = Person()
    personWithLoginOnly.login = person.login
    var futurePersons = personDAO.searchPersons(Some(personWithLoginOnly), None, None, None, None, None)
    futurePersons.map(personsWithSameLogin => {
      if (!personsWithSameLogin.isEmpty) {
        personsWithSameLogin.find(personWithSameLogin => personWithSameLogin.login.get.equals(person.login.get))
      } else {
        None
      }
    })
  }

  def getPerson(id: String, fieldsOption: Option[Seq[String]]): Future[Option[Person]] = {
    personDAO.getPerson(id, fieldsOption)
  }

  /**
   * Add the person only if :
   * - Both first name and last name are filled
   * - The email note exists and is not registered (with encrypted password)
   * - There is no homonym in the data base (firstName, lastName, BirthDate)
   *
   * If there is an existing email which is no active (without encrypted password) send back this user else
   * add the save the new Person
   * @param person
   * @param messages
   * @return
   */
  def addPerson(person: Person)(implicit messages: Messages): Future[(Option[Person], Boolean)] = {
    def searchHomonyme(personRequest: Person): Future[Boolean] = {
      val futurePersonResult = personDAO.searchPersons(Some(personRequest), None, None, None, None, None)
      futurePersonResult.map(personResult => {
        !personResult.isEmpty
      })
    }

    // Set only for account creation (when the encryptedEmailToken is set)
    if (person.login.isDefined && !person.encryptedEmailToken.isDefined) {
      throw new LoginCannotBeSetException
    }
    
    if (!person.firstName.isDefined || !person.lastName.isDefined) {
      throw new FirstNameAndLastNameRequiredException
    }

    if (person.email.isDefined) {
      val futurePerson = searchPersonWithEmail(person)
      val personWithSameEmail = Await.ready(futurePerson, Duration.Inf).value.get.get
      if (personWithSameEmail.isDefined) {
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
      val futureHomonymFound = searchHomonyme(personSearch)
      val homonymFound = Await.ready(futureHomonymFound, Duration.Inf).value.get.get
      if (homonymFound) {
        throw new HomonymNamesAndBirthDateException
      }
    }
    // Reset the birthDate to search only for first and last name
    personSearch.birthDate = None
    val futureHomonymFound = searchHomonyme(personSearch)
    val homonymFound = Await.ready(futureHomonymFound, Duration.Inf).value.get.get
    if (homonymFound) {
      throw new HomonymNamesException
    }

    val personOption = Await.ready(personDAO.addPerson(person), Duration.Inf).value.get.get
    Future(personOption, true)
  }

  /**
   * Can be used in two case.
   *
   * The first when the person as no active account yet (to update the email if it were wrong).
   * In that case we can update all the fields except when the email is already used by another person.
   *
   * The second is when the account is active. in that case the email set in input is not saved.
   *
   * @param id
   * @param person
   * @return
   */
  def editPerson(id: String, person: Person)(implicit messages: Messages): Future[Boolean] = {
    if (person.login.isDefined) {
      throw new LoginCannotBeSetException
    }
    val futurePerson = personDAO.getPerson(id, None)
    val existingPersonOption = Await.ready(futurePerson, Duration.Inf).value.get.get
    if (existingPersonOption.isDefined) {
      val existingPerson = existingPersonOption.get
      // If there is no registered account we can modify the email
      if (person.email.isDefined && existingPerson.email.isDefined && !person.email.get.equals(existingPerson.email.get)) {
        if (!existingPerson.encryptedPassword.isDefined) {
          val futurePersonWithSameEmail = searchPersonWithEmail(person)
          val personWithSameEmail = Await.ready(futurePersonWithSameEmail, Duration.Inf).value.get.get
          if (personWithSameEmail.isDefined) {
            if (personWithSameEmail.isDefined) {
              throw new EmailAlreadyRegisterdException
            }
          }
        } else {
          throw new EmailAlreadyRegisterdException
        }
      }

      return personDAO.editPerson(id, person)
    }
    Future(false)
  }

  def deletePerson(id: String): Future[Boolean] = {
    personDAO.deletePerson(id)
  }

}
