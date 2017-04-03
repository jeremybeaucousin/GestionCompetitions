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

@Singleton
class AuthenticationServices @Inject() (
    implicit val ec: ExecutionContext,
    personServices: PersonServices) {

  // TODO send error for email and password
  def createAccount(person: Person)(implicit messages: Messages): Future[(Option[Person], Boolean)] = {
    if (person.email.isDefined && person.password.isDefined) {
      person.encryptedPassword = Some(SecurityUtil.createPassword(person.password.get))
      personServices.addPerson(person)
    } else {
      Future(None, false)
    }
  }

  def authenticate(person: Person): Future[Option[Person]] = {
    if (person.email.isDefined && person.email.isDefined) {
      val futurePersons = personServices.searchPersonWithEmail(person)
      val persons = Await.ready(futurePersons, Duration.Inf).value.get.get
      if (!persons.isEmpty) {
        val firstPerson = persons(0)
        if (firstPerson.encryptedPassword.isDefined && SecurityUtil.checkPassword(person.password.get, firstPerson.encryptedPassword.get)) {
          return Future(Some(firstPerson))
        }
      }
    }
    Future(None)
  }
}