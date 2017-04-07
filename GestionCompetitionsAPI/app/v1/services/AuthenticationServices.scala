package v1.services

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import javax.inject.Singleton
import v1.model.Person
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import v1.utils.SecurityUtil
import play.api.i18n.Messages
import play.Logger
import java.util.UUID
import org.joda.time.DateTime
import java.util.Date
import errors.EmailPasswordLoginRequired
import errors.LoginAlreadyRegisterdException
import v1.dal.PersonDAO
import v1.dal.PersonDAO
import scala.concurrent.Await

@Singleton
class AuthenticationServices @Inject() (
    implicit val ec: ExecutionContext,
    personServices: PersonServices,
    personDao: PersonDAO[Person],
    grantsServices: GrantsServices,
    mailServices: MailServices) {

  final val EMAIL_TOKEN_DURATION = 10

  private def getExpirationTime = (new DateTime()) plusDays EMAIL_TOKEN_DURATION

  def createAccount(person: Person)(implicit messages: Messages): Future[(Option[Person], Boolean)] = {
    if (person.email.isDefined && person.password.isDefined && person.login.isDefined) {
      val futurePersonWithSameLogin = personServices.searchPersonWithLogin(person)
      futurePersonWithSameLogin.flatMap(personWithSameLogin => {
        if (personWithSameLogin.isDefined) {
          throw new LoginAlreadyRegisterdException
        } else {
          person.encryptedPassword = Some(SecurityUtil.encryptString(person.password.get))
          val encryptedEmailToken = SecurityUtil.encryptString(SecurityUtil.generateString(10))
          person.encryptedEmailToken = Some(encryptedEmailToken)
          person.emailTokenExpirationTime = Some(getExpirationTime.toDate())
          person.role = Some(grantsServices.USER)
          //          TODO See why it does not work
          mailServices.createAndSendEmail()
          personServices.addPerson(person)
        }
      })
    } else {
      throw new EmailPasswordLoginRequired
    }
  }

  def authenticate(person: Person): Future[Option[Person]] = {
    def handlePersonReturn(futurePerson: Future[Option[Person]]): Future[Option[Person]] = {
      futurePerson.map(personFoundOption => {
        if (personFoundOption.isDefined) {
          val personFound = personFoundOption.get
          if (personFound.encryptedPassword.isDefined && SecurityUtil.checkString(person.password.get, personFound.encryptedPassword.get)) {
            Some(personFound)
          } else {
            None
          }
        } else {
          None
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

  def validateAccount(encryptedEmailToken: String)(implicit messages: Messages): Future[Boolean] = {
    val personWithEmailTokenSearch = Person()
    personWithEmailTokenSearch.encryptedEmailToken = Some(encryptedEmailToken)
    val futurePersonWithToken = personDao.searchPersons(Some(personWithEmailTokenSearch), None, None, None, None, None)
    val personsWithToken = Await.result(futurePersonWithToken, Duration.Inf)
    if (!personsWithToken.isEmpty) {
      val personWithTokenOption = personsWithToken.find(personWithSameToken => personWithSameToken.encryptedEmailToken.get.equals(personWithEmailTokenSearch.encryptedEmailToken.get))
      if (personWithTokenOption.isDefined && !personWithTokenOption.get.emailTokenIsExpired) {
        val personWithToken = personWithTokenOption.get
        Logger.info(personWithToken._id.toString())
        Logger.info(personWithToken.emailTokenIsExpired.toString())
        personWithToken.encryptedEmailToken = None
        personWithToken.emailTokenExpirationTime = None
        return personServices.editPerson(personWithToken._id.get, personWithToken)
      }
    }
    Future(false)
  }
}