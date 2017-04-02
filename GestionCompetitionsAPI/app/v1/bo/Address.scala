package v1.bo

import play.api.Logger
import org.apache.commons.lang3.StringUtils
import v1.constantes.ValidationConstants
import v1.constantes.MessageConstants

case class Address(
  var name: Option[String] = Some(StringUtils.EMPTY),
  var number: Option[Int] = Some(0),
  var streetName: Option[String] = Some(StringUtils.EMPTY),
  var postalCode: Option[String] = Some(StringUtils.EMPTY),
  var city: Option[String] = Some(StringUtils.EMPTY))

object Address {
  import play.api.libs.json._
  import play.api.libs.json.Reads._
  import play.api.libs.functional.syntax._

  final val NAME: String = "name"
  final val NUMBER: String = "number"
  final val STREET_NAME: String = "streetName"
  final val POSTAL_CODE: String = "postalCode"
  final val CITY: String = "city"

  //    val personReads: Reads[Person] = (
  //    (JsPath \ _ID).readNullable[String](minLength[String](24) keepAnd maxLength[String](24)) and
  //    (JsPath \ FIRST_NAME).readNullable[String](minLength[String](2)) and
  //    (JsPath \ LAST_NAME).readNullable[String](minLength[String](2)) and
  //    (JsPath \ BIRTH_DATE).readNullable[Date] and
  //    (JsPath \ EMAIL).readNullable[String](email) and
  //    (JsPath \ PASSWORD).readNullable[String](pattern("""[a-zA-Z0-9@*#]{8,15}""".r, MessageConstants.error.password)) and
  //    // Extract empty to not receive an encrypted password by clients (only for database)
  //    (JsPath \ StringUtils.EMPTY).readNullable[String] and
  //    (JsPath \ ADDRESSES).readNullable[List[Address]])(Person.apply _)

  /**
   * Convert a json into an Address
   * @return
   */
  // TODO Implements validation
  val addressReads: Reads[Address] = (
    (JsPath \ NAME).readNullable[String] and
    (JsPath \ NUMBER).readNullable[Int] and
    (JsPath \ STREET_NAME).readNullable[String] and
    (JsPath \ POSTAL_CODE).readNullable[String](pattern(ValidationConstants.regex.POSTAL_CODE, MessageConstants.error.postalCode)) and
    (JsPath \ CITY).readNullable[String])(Address.apply _)

  object AddressWrites extends Writes[Address] {
    def writes(address: Address): JsObject = {
      var json = Json.obj()
      if (address.name.isDefined)
        json += (NAME -> JsString(address.name.get))
      if (address.number.isDefined)
        json += (NUMBER -> JsNumber(address.number.get))
      if (address.streetName.isDefined)
        json += (STREET_NAME -> JsString(address.streetName.get))
      if (address.postalCode.isDefined)
        json += (POSTAL_CODE -> JsString(address.postalCode.get))
      if (address.city.isDefined)
        json += (CITY -> JsString(address.city.get))
      json
    }
  }

  implicit object addressFormat extends Format[Address] {
    def reads(json: JsValue) = addressReads.reads(json)
    def writes(address: Address) = AddressWrites.writes(address)
  }

  import reactivemongo.bson._

  implicit object AddressWriter extends BSONDocumentWriter[Address] {
    def write(address: Address): BSONDocument = {
      var bson = BSONDocument()
      if (address.name.isDefined)
        bson ++= (NAME -> address.name.get)
      if (address.number.isDefined)
        bson ++= (NUMBER -> address.number.get)
      if (address.streetName.isDefined)
        bson ++= (STREET_NAME -> address.streetName.get)
      if (address.postalCode.isDefined)
        bson ++= (POSTAL_CODE -> address.postalCode.get)
      if (address.city.isDefined)
        bson ++= (CITY -> address.city.get)
      bson
    }
  }

  implicit object AddressReader extends BSONDocumentReader[Address] {
    def read(bson: BSONDocument): Address = {
      new Address(
        bson.getAs[String](NAME),
        bson.getAs[Int](NUMBER),
        bson.getAs[String](STREET_NAME),
        bson.getAs[String](POSTAL_CODE),
        bson.getAs[String](CITY))
    }
  }
}