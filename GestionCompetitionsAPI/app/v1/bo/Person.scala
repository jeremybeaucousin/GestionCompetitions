package v1.bo

import java.util.Date

import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime

abstract trait User {
  def _id: Option[String]
  def firstName: Option[String]
  def lastName: Option[String]
  def birthDate: Option[Date]
  def addresses: Option[List[Address]]
}

case class Person(
    var _id: Option[String] = Some(StringUtils.EMPTY),
    var firstName: Option[String] = Some(StringUtils.EMPTY),
    var lastName: Option[String] = Some(StringUtils.EMPTY),
    var birthDate: Option[Date] = Some(new Date),
    var addresses: Option[List[Address]] = Some(List[Address](new Address, new Address))) extends User {

  def toTaekwondoist(): Taekwondoist = {
    Taekwondoist(
      _id,
      firstName,
      lastName,
      birthDate,
      addresses)
  }

  @Override
  override def toString(): String = {
    "id : " + _id + "; firstName : " + firstName + "; lastName : " + lastName + "; birthDate : " + birthDate + "; addresses: " + addresses + ";"
  }
}

object Person {
  import play.api.libs.json._

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
      if (person._id.isDefined)
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
        JsSuccess(Person(
          (obj \ _ID).asOpt[String],
          (obj \ FIRST_NAME).asOpt[String],
          (obj \ LAST_NAME).asOpt[String],
          (obj \ BIRTH_DATE).asOpt[Date],
          (obj \ ADDRESSES).asOpt[List[Address]]))
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
      if (person._id.isDefined)
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
      Person(
        bson.getAs[String](_ID),
        bson.getAs[String](FIRST_NAME),
        bson.getAs[String](LAST_NAME),
        bson.getAs[Date](BIRTH_DATE),
        bson.getAs[List[Address]](ADDRESSES))
    }
  }
}