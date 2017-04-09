package v1.services

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.Duration

import org.joda.time.DateTime

import errors.EmailPasswordLoginRequired
import errors.LoginAlreadyRegisterdException
import javax.inject.Inject
import javax.inject.Singleton
import play.Logger
import play.api.i18n.Messages
import v1.dal.PersonDAO
import v1.model.Person
import v1.utils.SecurityUtil
import v1.model.PasswordChange
import errors.PasswordNotRecognizedException
import errors.PasswordsNotMatchException

@Singleton
class AuthenticationServices @Inject() (
    implicit val ec: ExecutionContext,
    personServices: PersonServices,
    personDao: PersonDAO,
    grantsServices: GrantsServices) {

  final val EMAIL_TOKEN_DURATION = 10

  private def getExpirationTime = (new DateTime()) plusDays EMAIL_TOKEN_DURATION

  /**
   * @param id
   * @param person
   * @param messages
   * @return the boolean is used in two case :
   * 	- For complete creation to inform if is is new or not
   * 	-	For editing from existing person to know if is correctly saved
   */
  def createAccount(id: Option[String], person: Person)(implicit messages: Messages): Future[(Option[Person], Boolean)] = {
    if (person.email.isDefined && person.password.isDefined && person.login.isDefined) {
      val futurePersonWithSameLogin = personServices.searchPersonWithLogin(person)
      val personWithSameLogin = Await.result(futurePersonWithSameLogin, Duration.Inf)
      if (personWithSameLogin.isDefined) {
        throw new LoginAlreadyRegisterdException
      } else {
        person.encryptedPassword = Some(SecurityUtil.encryptString(person.password.get))
        val encryptedEmailToken = SecurityUtil.encryptString(SecurityUtil.generateString(10)).replaceAll("/", "")
        person.encryptedEmailToken = Some(encryptedEmailToken)
        person.emailTokenExpirationTime = Some(getExpirationTime.toDate())
        person.role = Some(grantsServices.USER)
        if (id.isDefined) {
          val futureExistingPerson = personServices.getPerson(id.get, None)
          val existingPerson = Await.result(futureExistingPerson, Duration.Inf)
          // If the person do not exists or already has an active account we do not continue
          if (existingPerson.isDefined && existingPerson.get.hasActiveAccount()) {
            throw new LoginAlreadyRegisterdException
          } else {
            val futurePerson = personServices.getPerson(id.get, None)
            val futureResult = personServices.editPerson(id.get, person)
            val resultOk = Await.result(futureResult, Duration.Inf)
            futurePerson.map(personOption => {
              (personOption, resultOk)
            })
          }
        } else {
          personServices.addPerson(person)
        }
      }
    } else {
      throw new EmailPasswordLoginRequired
    }
  }

  def authenticate(person: Person)(implicit messages: Messages): Future[Option[Person]] = {
    def handlePersonReturn(futurePerson: Future[Option[Person]]): Future[Option[Person]] = {
      futurePerson.map(personFoundOption => {
        if (personFoundOption.isDefined) {
          val personFound = personFoundOption.get
          if (personFound.encryptedPassword.isDefined && SecurityUtil.checkString(person.password.get, personFound.encryptedPassword.get)) {
            Some(personFound)
          } else {
            throw new PasswordNotRecognizedException
          }
        } else {
          throw new PasswordNotRecognizedException
        }
      })
    }

    if (person.login.isDefined) {
      handlePersonReturn(personServices.searchPersonWithLogin(person))
    } else if (person.email.isDefined) {
      handlePersonReturn(personServices.searchPersonWithEmail(person))
    } else {
      Future(None)
    }
  }

  def resetPassword(person: Person)(implicit messages: Messages): Future[Boolean] = {
    val futurePersonWithSameEmail = personServices.searchPersonWithEmail(person)
    val personWithSameEmailOption = Await.result(futurePersonWithSameEmail, Duration.Inf)
    if (personWithSameEmailOption.isDefined) {
      val personWithSameEmail = personWithSameEmailOption.get
      val newPassword = SecurityUtil.generateString(15)
      val newEncryptedPassword = SecurityUtil.encryptString(newPassword)
      Logger.info(newPassword.toString())
      val personUpdate = Person()
      personUpdate.password = Some(newPassword)
      personUpdate.encryptedPassword = Some(newEncryptedPassword)
      //          TODO See why it does not work
      //          mailServices.createAndSendEmail()
      //personWithSameEmail.email
      personDao.editPerson(personWithSameEmail._id.get, personUpdate)
    } else {
      throw new PasswordNotRecognizedException
    }
  }

  def validateAccount(encryptedEmailToken: String)(implicit messages: Messages): Future[Boolean] = {
    val personWithEmailTokenSearch = Person()
    personWithEmailTokenSearch.encryptedEmailToken = Some(encryptedEmailToken)
    val futurePersonWithToken = personDao.searchPersons(Some(personWithEmailTokenSearch), None, None, None, None, None)
    val personsWithToken = Await.result(futurePersonWithToken, Duration.Inf)
    if (!personsWithToken.isEmpty) {
      val personWithTokenOption = personsWithToken.find(personWithSameToken => personWithSameToken.encryptedEmailToken.get.equals(personWithEmailTokenSearch.encryptedEmailToken.get))
      if (personWithTokenOption.isDefined && !personWithTokenOption.get.emailTokenIsExpired) {
        val personWithToken = personWithTokenOption.get
        var fields = List[String]()
        fields = Person.ENCRYPTED_EMAIL_TOKEN :: fields
        fields = Person.EMAIL_TOKEN_EXPIRATION_TIME :: fields
        return personDao.deleteFields(personWithToken._id.get, fields)
      }
    }
    Future(false)
  }

  def changePassword(userId: String, passwordChange: PasswordChange)(implicit messages: Messages): Future[Boolean] = {
    Logger.info(userId)
    val futurePerson = personServices.getPerson(userId, None)
    val personOption = Await.result(futurePerson, Duration.Inf)
    if (personOption.isDefined) {
      val person = personOption.get
      val oldPasswordOk = SecurityUtil.checkString(passwordChange.oldPassword, person.encryptedPassword.get)
      if (!oldPasswordOk) {
        throw new PasswordNotRecognizedException
      }
      val newPasswordsMatch = passwordChange.newPasswordFirst.equals(passwordChange.newPasswordSecond)
      if (!newPasswordsMatch) {
        throw new PasswordsNotMatchException
      }
      val personWithNewEncryptedPassword = Person()
      personWithNewEncryptedPassword.encryptedPassword = Some(SecurityUtil.encryptString(passwordChange.newPasswordFirst))
      personDao.editPerson(person._id.get, personWithNewEncryptedPassword)
    } else {
      throw new PasswordNotRecognizedException
    }
  }
}