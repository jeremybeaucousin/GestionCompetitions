package bo

import play.api.Logger
import play.api.libs.json._
import reactivemongo.bson

case class Address(
  var name: Option[String],  
  var number: Option[Int],
  var streetName: Option[String],
  var postalCode: Option[String])

object Address {
  final val NAME: String = "name"
  final val NUMBER: String = "number"
  final val STREET_NAME: String = "streetName"
  final val POSTAL_CODE: String = "postalCode"

  implicit object AddressWrites extends Writes[Address] {
    def writes(address: Address): JsObject = {
      var json = Json.obj()
      if (address.name.isDefined)
        json += (NAME -> JsString.apply(address.name.get))
      if (address.number.isDefined)
        json += (NUMBER -> JsNumber.apply(address.number.get))
      if (address.streetName.isDefined)
        json += (STREET_NAME -> JsString.apply(address.streetName.get))
      if (address.postalCode.isDefined)
        json += (POSTAL_CODE -> JsString.apply(address.postalCode.get))
      json
    }
  }

  implicit object AddressReads extends Reads[Address] {
    def reads(json: JsValue): JsResult[Address] = json match {
      case obj: JsValue => try {
        val name = (obj \ NAME).asOpt[String]
        val number = (obj \ NUMBER).asOpt[Int]
        val streetName = (obj \ STREET_NAME).asOpt[String]
        val postalCode = (obj \ POSTAL_CODE).asOpt[String]
        JsSuccess(Address(
          name,
          number,
          streetName,
          postalCode))

      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }
      case _ => JsError("expected.jsobject")
    }
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
      bson
    }
  }

  implicit object AddressReader extends BSONDocumentReader[Address] {
    def read(bson: BSONDocument): Address = {
      new Address(
        bson.getAs[String](NAME),
        bson.getAs[Int](NUMBER),
        bson.getAs[String](STREET_NAME),
        bson.getAs[String](POSTAL_CODE))
    }
  }
}