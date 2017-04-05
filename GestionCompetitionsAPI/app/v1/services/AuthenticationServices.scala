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
  // TODO  Await send dead letter
  def authenticate(person: Person): Future[Option[Person]] = {
//    def handlePersonReturn(futurePerson: Future[Option[Person]]): Option[Person] = {
//      val personFoundOption = Await.ready(futurePerson, Duration.Inf).value.get.get
//      if (personFoundOption.isDefined) {
//        val personFound: Person = personFoundOption.get
//        if (personFound.encryptedPassword.isDefined && SecurityUtil.checkString(person.password.get, personFound.encryptedPassword.get)) {
//          Logger.info(personFound.toString)
//          return Some(personFound)
//        }
//      }
//      None
//    }
    
    if (person.login.isDefined) {
      val futurePerson = personServices.searchPersonWithLogin(person)
      val personWithSameLoginOption = Await.ready(futurePerson, Duration.Inf).value.get.get
      if (personWithSameLoginOption.isDefined) {
        val personWithSameLogin = personWithSameLoginOption.get
        if (personWithSameLogin.encryptedPassword.isDefined && SecurityUtil.checkString(person.password.get, personWithSameLogin.encryptedPassword.get)) {
          return Future(Some(personWithSameLogin))
        }
      }
    } else if (person.email.isDefined) {
      val futurePerson = personServices.searchPersonWithEmail(person)
      val personWithSameEmailOption = Await.ready(futurePerson, Duration.Inf).value.get.get
      if (personWithSameEmailOption.isDefined) {
        val personWithSameEmail = personWithSameEmailOption.get
        if (personWithSameEmail.encryptedPassword.isDefined && SecurityUtil.checkString(person.password.get, personWithSameEmail.encryptedPassword.get)) {
          return Future(Some(personWithSameEmail))
        }
      }
    }
    Future(None)
  }
}