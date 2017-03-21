package bo

import play.api.Logger
import play.api.libs.json._
import reactivemongo.bson
import reactivemongo.bson.BSONObjectID

class Licenced(
  val passportNumber: Option[Int],
  val grade: Option[String])

object Licenced {
  final val PASSEPORT_NUMBER: String = "passportNumber"
  final val GRADE: String = "grade"

  implicit object LicencedWrites extends Writes[Licenced] {
    def writes(licenced: Licenced): JsObject = {
      var json = Json.obj()
      if (licenced.passportNumber.isDefined)
        json += (PASSEPORT_NUMBER -> JsNumber.apply(licenced.passportNumber.get))
      if (licenced.grade.isDefined)
        json += (GRADE -> JsString.apply(licenced.grade.get))
      json
    }
  }

  implicit object LicencedReads extends Reads[Licenced] {
    def reads(json: JsValue): JsResult[Licenced] = json match {
      case obj: JsValue => try {
        val number = (obj \ PASSEPORT_NUMBER).asOpt[Int]
        val streetName = (obj \ GRADE).asOpt[String]
        JsSuccess(new Licenced(
          number,
          streetName))

      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }

      case _ => JsError("expected.jsobject")
    }
  }

  import reactivemongo.bson._

  implicit object LicencedWriter extends BSONDocumentWriter[Licenced] {
    def write(licenced: Licenced): BSONDocument = {
      var bson = BSONDocument()
      if (licenced.passportNumber.isDefined)
        bson ++= (PASSEPORT_NUMBER -> licenced.passportNumber.get)
      if (licenced.grade.isDefined)
        bson ++= (GRADE -> licenced.grade.get)
      bson
    }
  }

  implicit object LicencedReader extends BSONDocumentReader[Licenced] {
    def read(bson: BSONDocument): Licenced = {
      new Licenced(
        bson.getAs[Int](PASSEPORT_NUMBER),
        bson.getAs[String](GRADE))
    }
  }
}