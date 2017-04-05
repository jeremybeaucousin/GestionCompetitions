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

@Singleton
class AuthenticationServices @Inject() (
    implicit val ec: ExecutionContext,
    personServices: PersonServices) {
  
  final val EMAIL_TOKEN_DURATION = 10

  private def getExpirationTime = (new DateTime()) plusDays EMAIL_TOKEN_DURATION
  // TODO  Resend verification email
  // TODO Treat login (required)
  def createAccount(person: Person)(implicit messages: Messages): Future[(Option[Person], Boolean)] = {
    if (person.email.isDefined && person.password.isDefined) {
      person.encryptedPassword = Some(SecurityUtil.encryptString(person.password.get))
      val futurePersonWithFlagForNew = personServices.addPerson(person)
      val personWithFlagForNew = Await.ready(futurePersonWithFlagForNew, Duration.Inf).value.get.get
      val personInsertedOption = personWithFlagForNew._1
      val isNew = personWithFlagForNew._2
      if(personInsertedOption.isDefined && isNew) {
        // TODO Send by email
        val emailToken = SecurityUtil.generateString(10)
        val encryptedEmailToken = SecurityUtil.encryptString(SecurityUtil.generateString(10))
        val personInserted = personInsertedOption.get
        personInserted.encryptedEmailToken = Some(encryptedEmailToken)
        personInserted.emailTokenExpirationTime = Some(getExpirationTime.toDate())
        personServices.editPerson(personInserted._id.get, personInserted)
        // TODO Send and email to activate the accound and think about test about inserted token
        // TODO Create business rules about this token
      }
      Future(personInsertedOption, isNew)
    } else {
      Future(None, false)
    }
  }

  def authenticate(person: Person): Future[Option[Person]] = {
    if (person.email.isDefined && person.email.isDefined) {
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