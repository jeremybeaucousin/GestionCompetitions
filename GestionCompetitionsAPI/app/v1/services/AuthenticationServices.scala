package v1.services

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import javax.inject.Singleton
import v1.bo.Person
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import v1.utils.SecurityUtil
import play.api.i18n.Messages
import play.Logger
import java.util.UUID
import org.joda.time.DateTime
import java.util.Date
import errors.EmailPasswordLoginRequired
import errors.LoginAlreadyRegisterdException

@Singleton
class AuthenticationServices @Inject() (
    implicit val ec: ExecutionContext,
    personServices: PersonServices,
    grantsServices: GrantsServices) {

  final val EMAIL_TOKEN_DURATION = 10

  private def getExpirationTime = (new DateTime()) plusDays EMAIL_TOKEN_DURATION

  def createAccount(person: Person)(implicit messages: Messages): Future[(Option[Person], Boolean)] = {
    if (person.email.isDefined && person.password.isDefined && person.login.isDefined) {
      val futurePersonWithSameLogin = personServices.searchPersonWithLogin(person)
      futurePersonWithSameLogin.flatMap(personWithSameLogin => {
        if (personWithSameLogin.isDefined) {
          throw new LoginAlreadyRegisterdException
        } else {
          // TODO SEND MAIL
          person.encryptedPassword = Some(SecurityUtil.encryptString(person.password.get))
          val encryptedEmailToken = SecurityUtil.encryptString(SecurityUtil.generateString(10))
          person.encryptedEmailToken = Some(encryptedEmailToken)
          person.emailTokenExpirationTime = Some(getExpirationTime.toDate())
          person.role = Some(grantsServices.USER)
          personServices.addPerson(person)
        }
      })
    } else {
      throw new EmailPasswordLoginRequired
    }
  }
  // TODO  Resend verification email
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
}