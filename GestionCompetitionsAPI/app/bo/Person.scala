package bo

import play.api.Logger
import play.api.libs.json._
import reactivemongo.bson
import reactivemongo.bson.BSONObjectID
import java.util.Date
import java.util.Formatter.DateTime
import java.text.DateFormat
import play.api.data.format.Formats
import org.joda.time.DateTime
import scala.concurrent.ExecutionContext
import java.util.ArrayList
import org.apache.commons.lang3.StringUtils

trait Person {
  var _id: Option[String]
  var firstName: Option[String]
  var lastName: Option[String]
  var birthDate: Option[Date]
  var addresses: Option[List[Address]]
}

object Person extends Person {

  var _id: Option[String] = Some(StringUtils.EMPTY)
  var firstName: Option[String] = Some(StringUtils.EMPTY)
  var lastName: Option[String] = Some(StringUtils.EMPTY)
  var birthDate: Option[Date] = Some(new Date)
  var addresses: Option[List[Address]] = Some(List[Address](new Address, new Address))

  def apply(
    _id: Option[String],
    firstName: Option[String],
    lastName: Option[String],
    birthDate: Option[Date],
    addresses: Option[List[Address]]): Person = {
    this._id = _id
    this.firstName = firstName
    this.lastName = lastName
    this.birthDate = birthDate
    this.addresses = addresses
    this
  }

  implicit val jodaDateReads = Reads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss'Z'")
  implicit val jodaDateWrites = Writes.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss'Z'")

  final val _ID: String = "_id"
  final val FIRST_NAME: String = "firstName"
  final val LAST_NAME: String = "lastName"
  final val BIRTH_DATE: String = "birthDate"
  final val ADDRESSES: String = "addresses"

  implicit object PersonWrites extends Writes[Person] {
    def writes(person: Person): JsObject = {
      var json = Json.obj()
      if (person._id.isDefined && !person._id.get.isEmpty())
        json += (_ID -> JsString(person._id.get))
      if (person.firstName.isDefined)
        json += (FIRST_NAME -> JsString(person.firstName.get))
      if (person.lastName.isDefined)
        json += (LAST_NAME -> JsString(person.lastName.get))
      if (person.birthDate.isDefined)
        json += (BIRTH_DATE -> JsString(new DateTime(person.birthDate.get).toString()))
      if (person.addresses.isDefined)
        json += (ADDRESSES -> Json.toJson(person.addresses.get))
      json
    }
  }

  implicit object PersonReads extends Reads[Person] {
    def reads(json: JsValue): JsResult[Person] = json match {
      case obj: JsValue => try {
        _id = (obj \ _ID).asOpt[String]
        firstName = (obj \ FIRST_NAME).asOpt[String]
        lastName = (obj \ LAST_NAME).asOpt[String]
        birthDate = (obj \ BIRTH_DATE).asOpt[Date]
        addresses = (obj \ ADDRESSES).asOpt[List[Address]]
        JsSuccess(Person)

      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }

      case _ => JsError("expected.jsobject")
    }
  }

  import reactivemongo.bson._

  implicit object PersonWriter extends BSONDocumentWriter[Person] {
    def write(person: Person): BSONDocument = {
      var bson = BSONDocument()
      if (person._id.isDefined && !person._id.get.isEmpty())
        bson ++= (_ID -> person._id.get)
      if (person.firstName.isDefined)
        bson ++= (FIRST_NAME -> person.firstName.get)
      if (person.lastName.isDefined)
        bson ++= (LAST_NAME -> person.lastName.get)
      if (person.birthDate.isDefined)
        bson ++= (BIRTH_DATE -> person.birthDate.get)
      if (person.addresses.isDefined)
        bson ++= (ADDRESSES -> person.addresses.get)
      bson
    }
  }

  implicit object PersonReader extends BSONDocumentReader[Person] {
    def read(bson: BSONDocument): Person = {
      _id = bson.getAs[String](_ID)
      firstName = bson.getAs[String](FIRST_NAME)
      lastName = bson.getAs[String](LAST_NAME)
      birthDate = bson.getAs[Date](BIRTH_DATE)
      addresses = bson.getAs[List[Address]](ADDRESSES)
      Person
    }
  }
}