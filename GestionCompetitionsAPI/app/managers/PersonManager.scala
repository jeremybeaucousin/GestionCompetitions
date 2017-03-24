package managers

import bo.Person
import dal.PersonDAO
import javax.inject.{Inject, Singleton}
import play.api.inject.ApplicationLifecycle
import play.api.Logger
import reactivemongo.api.commands.WriteResult
import scala.concurrent.Future
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PersonManager @Inject() (personDAO: PersonDAO) (implicit ec: ExecutionContext) {

  def listPersons: Future[List[Person]] = {
  	personDAO.listPersons
  }
  
  def getPerson(id: String): Future[Option[Person]] = {
  	personDAO.getPerson(id)
  }
  
  def addPerson(person: Person): Future[String] = {
  	personDAO.addPerson(person)
  }
  
  def editPerson(id: String, person: Person) = {
  	personDAO.editPerson(id, person)
  }
  
  def deletePerson(id: String) = {
  	personDAO.deletePerson(id)
  }
}
