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

@Singleton
class AuthenticationServices @Inject() (
    implicit val ec: ExecutionContext,
    personServices: PersonServices) {

  def createAccount(person: Person)(implicit messages: Messages): Future[(Option[Person], Boolean)] = {
    if (person.email.isDefined && person.password.isDefined) {
      person.encryptedPassword = Some(SecurityUtil.createPassword(person.password.get))
      val futurePersonWithFlagForNew = personServices.addPerson(person)
      val personWithFlagForNew = Await.ready(futurePersonWithFlagForNew, Duration.Inf).value.get.get
      val personInsertedOption = personWithFlagForNew._1
      val isNew = personWithFlagForNew._2
      if(personInsertedOption.isDefined && isNew) {
        val uuid = UUID.randomUUID().toString
        val personInserted = personInsertedOption.get
        personInserted.emailToken = Some(uuid)
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
        if (personWithSameEmail.encryptedPassword.isDefined && SecurityUtil.checkPassword(person.password.get, personWithSameEmail.encryptedPassword.get)) {
          return Future(Some(personWithSameEmail))
        }
      }
    }
    Future(None)
  }
}