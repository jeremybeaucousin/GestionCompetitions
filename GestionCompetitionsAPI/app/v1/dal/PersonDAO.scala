package v1.dal

import v1.dal.repos.AbstractRepoImpl
import java.io.{ StringWriter, PrintWriter }
import javax.inject.{ Inject, Singleton }
import play.api.Logger
import play.api.mvc.Results
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.ReadPreference
import scala.concurrent.Future
import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.util.{ Failure, Success }
import v1.constantes.MessageConstants
import scala.concurrent.duration.Duration
import play.api.i18n.MessagesApi
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter
import v1.utils.MongoDbUtil
import scala.concurrent.Await
import play.modules.reactivemongo.ReactiveMongoApi
import v1.model.Person
import reactivemongo.api.collections.bson.BSONCollection

class PersonDAO @Inject() (val reactiveMongoApi: ReactiveMongoApi)(
    implicit ec: ExecutionContext) {

  val collection = reactiveMongoApi.database.map(_.collection[BSONCollection](MongoDbUtil.PERSONS_COLLECTION))
  val personRepo: AbstractRepoImpl[Person] = new AbstractRepoImpl[Person](collection)

  def getTotalCount(personOption: Option[Person], searchInValues: Option[Boolean]): Future[Int] = {
    personRepo.getTotalCount(personOption, searchInValues)
  }

  def searchPersons(
    personOption: Option[Person],
    searchInValues: Option[Boolean],
    sortOption: Option[Seq[String]],
    fieldsOption: Option[Seq[String]],
    offsetOption: Option[Int],
    limitOption: Option[Int]): Future[List[Person]] = {
    personRepo.find(personOption, searchInValues: Option[Boolean], sortOption, fieldsOption, offsetOption, limitOption)
  }

  def getPerson(id: String, fieldsOption: Option[Seq[String]])(
    implicit bSONDocumentReader: BSONDocumentReader[Person],
    bSONDocumentWriter: BSONDocumentWriter[Person]): Future[Option[Person]] = {
    personRepo.select(id, fieldsOption)
  }

  def addPerson(document: Person): Future[Option[Person]] = {
    val _id = MongoDbUtil.generateId().stringify
    val futureResult = personRepo.save(_id, document)
    val hasNoError = Await.result(futureResult, Duration.Inf)
    if (hasNoError) {
      val futurePerson = personRepo.select(_id, None)
      futurePerson.map(personInserted => {
        personInserted
      })
    } else {
      Future(None)
    }
  }

  def deleteFields(id: String, fields: List[String]): Future[Boolean] = {
    personRepo.deleteFields(id, fields)
  }

  def editPerson(id: String, person: Person): Future[Boolean] = {
    person._id = None
    personRepo.update(id, person)
  }

  def deletePerson(id: String): Future[Boolean] = {
    personRepo.remove(id)
  }

  import v1.model.Address

  object Address {
    def getAddresses(
      userId: String,
      fieldsOption: Option[Seq[String]]): Future[Option[List[Address]]] = {
      val personSearch = Person()
      personSearch._id = Some(userId)
      var fieldsWithAddressAndId = Seq[String]()
      fieldsWithAddressAndId = fieldsWithAddressAndId :+ Person._ID
      fieldsWithAddressAndId = fieldsWithAddressAndId :+ Person.ADDRESSES

      if (fieldsOption.isDefined) {
        fieldsOption.get.foreach(field => {
          if (v1.model.Address.isAddressField(field)) {
            fieldsWithAddressAndId = fieldsWithAddressAndId :+ s"${Person.ADDRESSES}.$field"
          }
        })
      }
      personRepo.find(Some(personSearch), Some(false), None, Some(fieldsWithAddressAndId), None, None).map {
        case (persons) => {
          if (!persons.isEmpty) {
            val personOption = persons.find(personFound => {
              personFound._id.isDefined && (personFound._id.get.equals(personSearch._id.get))
            })
            if (personOption.isDefined) {
              personOption.get.addresses
            } else {
              None
            }
          } else {
            None
          }
        }
      }
    }

    def addAddress(userId: String, address: Address): Future[Option[Int]] = {
      val futureResult = personRepo.addDocumentToSubArray(userId, Person.ADDRESSES, address, v1.model.Address.NAME)
      val hasNoError = Await.result(futureResult, Duration.Inf)
      if (hasNoError) {
        val futurePerson = personRepo.select(userId, None)
        futurePerson.map(personModified => {
          if(personModified.isDefined && personModified.get.addresses.isDefined) {
            Some(personModified.get.addresses.get.indexOf(address))
          } else {
            None
          }
        })
      } else {
        Future(None)
      }
    }

    def getAddress(userId: String, index: Int, fields: Option[Seq[String]]): Future[Option[Address]] = {
      Future(null)
    }

    def editAddress(userId: String, index: Int, address: Address): Future[Boolean] = {
      Future(false)
    }

    def deleteAddress(userId: String, index: Int): Future[Boolean] = {
      Future(false)
    }
  }

  final val address = Address
}
