package v1.bo

import java.util.Date

import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import v1.constantes.MessageConstants
import v1.constantes.ValidationConstants

abstract trait User {
  def _id: Option[String]
  def firstName: Option[String]
  def lastName: Option[String]
  def birthDate: Option[Date]
  def email: Option[String]
  def password: Option[String]
  def encryptedPassword: Option[String]
  def addresses: Option[List[Address]]
}

case class Person(
    var _id: Option[String] = Some(StringUtils.EMPTY),
    var firstName: Option[String] = Some(StringUtils.EMPTY),
    var lastName: Option[String] = Some(StringUtils.EMPTY),
    var birthDate: Option[Date] = Some(new Date),
    var email: Option[String] = Some(StringUtils.EMPTY),
    var password: Option[String] = Some(StringUtils.EMPTY),
    var encryptedPassword: Option[String] = Some(StringUtils.EMPTY),
    var addresses: Option[List[Address]] = Some(List[Address](new Address, new Address))) extends User {

  def toTaekwondoist(): Taekwondoist = {
    Taekwondoist(
      _id,
      firstName,
      lastName,
      birthDate,
      email,
      password,
      encryptedPassword,
      addresses)
  }

  @Override
  override def toString(): String = {
    "id : " + _id + "; firstName : " + firstName + "; lastName : " + lastName + "; birthDate : " + birthDate + "; addresses: " + addresses + ";"
  }
}

object Person {
  import play.api.libs.json._
  import play.api.libs.json.Reads._
  import play.api.libs.functional.syntax._

  implicit val jodaDateReads = Reads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss'Z'")
  implicit val jodaDateWrites = Writes.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss'Z'")

  final val _ID: String = "_id"
  final val FIRST_NAME: String = "firstName"
  final val LAST_NAME: String = "lastName"
  final val BIRTH_DATE: String = "birthDate"
  final val EMAIL = "email"
  final val PASSWORD = "password"
  final val ENCRYPTED_PASSWORD = "encryptedPassword"
  final val ADDRESSES: String = "addresses"

  /**
   * Convert a json into a Person
   * @return
   */
  val personReads: Reads[Person] = (
    (JsPath \ _ID).readNullable[String](minLength[String](24) keepAnd maxLength[String](24)) and
    (JsPath \ FIRST_NAME).readNullable[String](minLength[String](2)) and
    (JsPath \ LAST_NAME).readNullable[String](minLength[String](2)) and
    (JsPath \ BIRTH_DATE).readNullable[Date] and
    (JsPath \ EMAIL).readNullable[String](email) and
    (JsPath \ PASSWORD).readNullable[String](pattern(ValidationConstants.regex.PASSWORD, MessageConstants.error.password)) and
    // Extract empty to not receive an encrypted password by clients (only for database)
    (JsPath \ StringUtils.EMPTY).readNullable[String] and
    (JsPath \ ADDRESSES).readNullable[List[Address]])(Person.apply _)

  /**
   * Convert a Person into a json, passwords are not sent to the clients
   * @return
   */
  object PersonWrites extends Writes[Person] {
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
      if (person.email.isDefined)
        json += (EMAIL -> JsString(person.email.get))
      if (person.addresses.isDefined)
        json += (ADDRESSES -> Json.toJson(person.addresses.get))
      json
    }
  }

  implicit object personFormat extends Format[Person] {
    def reads(json: JsValue) = personReads.reads(json)
    def writes(person: Person) = PersonWrites.writes(person)
  }

  import reactivemongo.bson._

  /**
   * Convert a Person into a Bson for MongoDb, only the encrypted password is stored
   * @return
   */
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
      if (person.email.isDefined)
        bson ++= (EMAIL -> person.email.get)
      if (person.encryptedPassword.isDefined)
        bson ++= (ENCRYPTED_PASSWORD -> person.encryptedPassword.get)
      if (person.addresses.isDefined)
        bson ++= (ADDRESSES -> person.addresses.get)
      bson
    }
  }

  /**
   * Convert a Bson from MongoDb into a Person, , only the encrypted password is read
   * @return
   */
  implicit object PersonReader extends BSONDocumentReader[Person] {
    def read(bson: BSONDocument): Person = {
      Person(
        bson.getAs[String](_ID),
        bson.getAs[String](FIRST_NAME),
        bson.getAs[String](LAST_NAME),
        bson.getAs[Date](BIRTH_DATE),
        bson.getAs[String](EMAIL),
        None,
        bson.getAs[String](ENCRYPTED_PASSWORD),
        bson.getAs[List[Address]](ADDRESSES))
    }
  }
}