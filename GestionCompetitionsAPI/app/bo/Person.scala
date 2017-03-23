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

trait Person {
  var _id: Option[String]
  var firstName: Option[String]
  var lastName: Option[String]
  var birthDate: Option[Date]
  var address: Option[Adress]
}

object Person extends Person {

  var _id: Option[String] = Some(null)
  var firstName: Option[String]= Some(null)
  var lastName: Option[String]= Some(null)
  var birthDate: Option[Date]= Some(null)
  var address: Option[Adress]= Some(null)

  implicit val jodaDateReads = Reads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss'Z'")
  implicit val jodaDateWrites = Writes.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss'Z'")

  final val _ID: String = "_id"
  final val FIRST_NAME: String = "firstName"
  final val LAST_NAME: String = "lastName"
  final val BIRTH_DATE: String = "birthDate"
  final val ADDRESS: String = "Address"

  implicit object PersonWrites extends Writes[Person] {
    def writes(person: Person): JsObject = {
      var json = Json.obj()
      if (person._id.isDefined && !person._id.get.isEmpty())
        json += (_ID -> JsString.apply(person._id.get))
      if (person.firstName.isDefined)
        json += (FIRST_NAME -> JsString.apply(person.firstName.get))
      if (person.lastName.isDefined)
        json += (LAST_NAME -> JsString.apply(person.lastName.get))
      if (person.birthDate.isDefined)
        json += (BIRTH_DATE -> JsString.apply(new DateTime(person.birthDate.get).toString()))
      if (person.address.isDefined)
        json += (ADDRESS -> Json.toJson(person.address.get))
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
        address = (obj \ ADDRESS).asOpt[Adress]
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
      if (person.address.isDefined)
        bson ++= (ADDRESS -> BSON.write(person.address.get))
      bson
    }
  }

  implicit object PersonReader extends BSONDocumentReader[Person] {
    def read(bson: BSONDocument): Person = {
      _id = bson.getAs[String](_ID)
      firstName = bson.getAs[String](FIRST_NAME)
      lastName = bson.getAs[String](LAST_NAME)
      birthDate = bson.getAs[Date](BIRTH_DATE)
      address = bson.getAs[Adress](ADDRESS)
      Person
    }
  }
}