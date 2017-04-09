package v1.model

import java.util.Date

import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import v1.constantes.MessageConstants
import v1.constantes.ValidationConstants
import play.Logger

case class Person(
    var _id: Option[String] = None,
    var firstName: Option[String] = None,
    var lastName: Option[String] = None,
    var birthDate: Option[Date] = None,
    var login: Option[String] = None,
    var email: Option[String] = None,
    var role: Option[String] = None,
    var encryptedEmailToken: Option[String] = None,
    var emailTokenExpirationTime: Option[Date] = None,
    var password: Option[String] = None,
    var encryptedPassword: Option[String] = None,
    var addresses: Option[List[Address]] = None) {

  /**
   * The person can have been saved by a admin or by himself by creating an account
   * @return
   */
  def hasAnAccount(): Boolean = {
    encryptedPassword.isDefined
  }

  /**
   * The person has an account account when he validate it (wich delete the encryptedEmailToken stored)
   * @return
   */
  def hasActiveAccount(): Boolean = {
    encryptedPassword.isDefined && !encryptedEmailToken.isDefined
  }
  
  def emailTokenIsExpired = if (emailTokenExpirationTime.isDefined) emailTokenExpirationTime.get.before(new Date) else false

  def toTaekwondoist(): Taekwondoist = {
    Taekwondoist(
      _id,
      firstName,
      lastName,
      birthDate,
      login,
      email,
      role,
      encryptedEmailToken,
      emailTokenExpirationTime,
      password,
      encryptedPassword,
      addresses)
  }

  @Override
  override def toString(): String = {
    "" + _id + firstName + lastName + birthDate + email + password + encryptedPassword + addresses
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
  final val LOGIN = "login"
  final val EMAIL = "email"
  final val ROLE = "role"
  final val ENCRYPTED_EMAIL_TOKEN = "encryptedEmailToken"
  final val EMAIL_TOKEN_EXPIRATION_TIME = "emailTokenExpirationTime"
  final val PASSWORD = "password"
  final val ENCRYPTED_PASSWORD = "encryptedPassword"
  final val ADDRESSES: String = "addresses"

  /**
   * Convert a json into a Person, extract empty values when we this is informations that clients do not have to have
   * @return
   */
  val personReads: Reads[Person] = (
    (JsPath \ _ID).readNullable[String](minLength[String](24) keepAnd maxLength[String](24)) and
    (JsPath \ FIRST_NAME).readNullable[String](minLength[String](2)) and
    (JsPath \ LAST_NAME).readNullable[String](minLength[String](2)) and
    (JsPath \ BIRTH_DATE).readNullable[Date] and
    (JsPath \ LOGIN).readNullable[String] and
    (JsPath \ EMAIL).readNullable[String](email) and
    (JsPath \ StringUtils.EMPTY).readNullable[String](email) and // role
    (JsPath \ StringUtils.EMPTY).readNullable[String] and // ENCRYPTED_EMAIL_TOKEN
    (JsPath \ StringUtils.EMPTY).readNullable[Date] and // EMAIL_TOKEN_EXPIRATION_TIME
    (JsPath \ PASSWORD).readNullable[String](pattern(ValidationConstants.regex.PASSWORD, MessageConstants.error.password)) and
    (JsPath \ StringUtils.EMPTY).readNullable[String] and // ENCRYPTED_PASSWORD
    (JsPath \ ADDRESSES).readNullable[List[Address]])(Person.apply _)

  /**
   * Convert a Person into a json, some informations are not send back to the clients
   * @return
   */
    // TODO had confidenciality information and test before send back information
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
      if (person.login.isDefined)
        json += (LOGIN -> JsString(person.login.get))
      if (person.email.isDefined)
        json += (EMAIL -> JsString(person.email.get))
      if (person.addresses.isDefined)
        json += (ADDRESSES -> Json.toJson(person.addresses.get))
      json
    }
  }

  implicit object PersonFormat extends Format[Person] {
    def reads(json: JsValue) = {
      val personRead = personReads.reads(json)
      personRead.map(person => {
        if (person.email.isDefined) {
          person.email = Some(person.email.get.toLowerCase())
        }
        person
      })
    }
    def writes(person: Person) = PersonWrites.writes(person)
  }

  import reactivemongo.bson._
  
  case class PersonWriter() extends BSONDocumentWriter[Person] {
     def write(person: Person): BSONDocument = {
       PersonWriter.write(person)
     }
  }

  case class PersonReader() extends BSONDocumentReader[Person] {
    def read(bson: BSONDocument): Person = {
      PersonReader.read(bson)
    }
  }

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
      if (person.login.isDefined)
        bson ++= (LOGIN -> person.login.get)
      if (person.email.isDefined)
        bson ++= (EMAIL -> person.email.get)
      if (person.role.isDefined)
        bson ++= (ROLE -> person.role.get)
      if (person.encryptedEmailToken.isDefined)
        bson ++= (ENCRYPTED_EMAIL_TOKEN -> person.encryptedEmailToken.get)
      if (person.emailTokenExpirationTime.isDefined)
        bson ++= (EMAIL_TOKEN_EXPIRATION_TIME -> person.emailTokenExpirationTime.get)
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
      val person = Person()
      person._id = bson.getAs[String](_ID)
      person.firstName = bson.getAs[String](FIRST_NAME)
      person.lastName = bson.getAs[String](LAST_NAME)
      person.birthDate = bson.getAs[Date](BIRTH_DATE)
      person.login = bson.getAs[String](LOGIN)
      person.email = bson.getAs[String](EMAIL)
      person.role = bson.getAs[String](ROLE)
      person.encryptedEmailToken = bson.getAs[String](ENCRYPTED_EMAIL_TOKEN)
      person.emailTokenExpirationTime = bson.getAs[Date](EMAIL_TOKEN_EXPIRATION_TIME)
      person.encryptedPassword = bson.getAs[String](ENCRYPTED_PASSWORD)
      person.addresses = bson.getAs[List[Address]](ADDRESSES)
      person
    }
  }
}