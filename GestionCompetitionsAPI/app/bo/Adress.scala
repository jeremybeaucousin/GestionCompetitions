package bo

import play.api.Logger
import play.api.libs.json._
import reactivemongo.bson
import reactivemongo.bson.BSONObjectID

case class Adress(
  var name: Option[String],  
  var number: Option[Int],
  var streetName: Option[String],
  var postalCode: Option[String])

object Adress {
  final val NAME: String = "name"
  final val NUMBER: String = "number"
  final val STREET_NAME: String = "streetName"
  final val POSTAL_CODE: String = "postalCode"

  implicit object AdressWrites extends Writes[Adress] {
    def writes(adress: Adress): JsObject = {
      var json = Json.obj()
      if (adress.name.isDefined)
        json += (NAME -> JsString.apply(adress.name.get))
      if (adress.number.isDefined)
        json += (NUMBER -> JsNumber.apply(adress.number.get))
      if (adress.streetName.isDefined)
        json += (STREET_NAME -> JsString.apply(adress.streetName.get))
      if (adress.postalCode.isDefined)
        json += (POSTAL_CODE -> JsString.apply(adress.postalCode.get))
      json
    }
  }

  implicit object AdressReads extends Reads[Adress] {
    def reads(json: JsValue): JsResult[Adress] = json match {
      case obj: JsValue => try {
        val name = (obj \ NAME).asOpt[String]
        val number = (obj \ NUMBER).asOpt[Int]
        val streetName = (obj \ STREET_NAME).asOpt[String]
        val postalCode = (obj \ POSTAL_CODE).asOpt[String]
        JsSuccess(Adress(
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

  implicit object AdressWriter extends BSONDocumentWriter[Adress] {
    def write(adress: Adress): BSONDocument = {
      var bson = BSONDocument()
      if (adress.name.isDefined)
        bson ++= (NAME -> adress.name.get)
      if (adress.number.isDefined)
        bson ++= (NUMBER -> adress.number.get)
      if (adress.streetName.isDefined)
        bson ++= (STREET_NAME -> adress.streetName.get)
      if (adress.postalCode.isDefined)
        bson ++= (POSTAL_CODE -> adress.postalCode.get)
      bson
    }
  }

  implicit object AdressReader extends BSONDocumentReader[Adress] {
    def read(bson: BSONDocument): Adress = {
      new Adress(
        bson.getAs[String](NAME),
        bson.getAs[Int](NUMBER),
        bson.getAs[String](STREET_NAME),
        bson.getAs[String](POSTAL_CODE))
    }
  }
}