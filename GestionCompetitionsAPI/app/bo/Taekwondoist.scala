package bo

import java.util.Date
import play.api.Logger
import play.api.libs.json._
import reactivemongo.bson
import reactivemongo.bson.BSONObjectID
import bo.Person.PersonWrites
import bo.Person.PersonReads
import bo.Person.PersonWriter
import bo.Person.PersonReader

class Taekwondoist(
  _id: Option[String],
  firstName: Option[String],
  lastName: Option[String],
  birthDate: Option[Date],
  address: Option[Adress],
  val passportNumber: Option[Int],
  val grade: Option[String])
    extends Person(_id, firstName, lastName, birthDate, address) {
  
  final val PASSEPORT_NUMBER: String = "passportNumber"
  final val GRADE: String = "grade"

  implicit object TaekwondoistWrites extends Writes[Taekwondoist] {
    def writes(taekwondoist: Taekwondoist): JsObject = {
      var json = PersonWrites.writes(taekwondoist)
      if (taekwondoist.passportNumber.isDefined)
        json += (PASSEPORT_NUMBER -> JsNumber.apply(taekwondoist.passportNumber.get))
      if (taekwondoist.grade.isDefined)
        json += (GRADE -> JsString.apply(taekwondoist.grade.get))
      json
    }
  }

  implicit object TaekwondoistReads extends Reads[Taekwondoist] {
    def reads(json: JsValue): JsResult[Taekwondoist] = json match {
      case obj: JsValue => try {
        val person = PersonReads.reads(obj).get
        val number = (obj \ PASSEPORT_NUMBER).asOpt[Int]
        val streetName = (obj \ GRADE).asOpt[String]
        JsSuccess(new Taekwondoist(
          person._id,
          person.firstName,
          person.lastName,
          person.birthDate,
          person.address,
          number,
          streetName))

      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }

      case _ => JsError("expected.jsobject")
    }
  }

  import reactivemongo.bson._

  implicit object TaekwondoistWriter extends BSONDocumentWriter[Taekwondoist] {
    def write(taekwondoist: Taekwondoist): BSONDocument = {
      var bson = PersonWriter.write(taekwondoist)
      if (taekwondoist.passportNumber.isDefined)
        bson ++= (PASSEPORT_NUMBER -> taekwondoist.passportNumber.get)
      if (taekwondoist.grade.isDefined)
        bson ++= (GRADE -> taekwondoist.grade.get)
      bson
    }
  }

  implicit object TaekwondoistReader extends BSONDocumentReader[Taekwondoist] {
    def read(bson: BSONDocument): Taekwondoist = {
      val person = PersonReader.read(bson)
      new Taekwondoist(
        person._id,
        person.firstName,
        person.lastName,
        person.birthDate,
        person.address,
        bson.getAs[Int](PASSEPORT_NUMBER),
        bson.getAs[String](GRADE))
    }
  }
}