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

trait Taekwondoist extends Person {
  var passportNumber: Option[Int]
  var grade: Option[String]
}

object Taekwondoist extends Taekwondoist {
  var _id: Option[String] = Some(null)
  var firstName: Option[String] = Some(null)
  var lastName: Option[String] = Some(null)
  var birthDate: Option[Date] = Some(null)
  var addresses: Option[List[Address]] = Some(null)
  var passportNumber: Option[Int] = Some(0)
  var grade: Option[String] = Some(null)

  final val PASSEPORT_NUMBER: String = "passportNumber"
  final val GRADE: String = "grade"

  implicit object TaekwondoistWrites extends Writes[Taekwondoist] {
    def writes(taekwondoist: Taekwondoist): JsObject = {
      var json = PersonWrites.writes(taekwondoist)
      if (taekwondoist.passportNumber.isDefined)
        json += (PASSEPORT_NUMBER -> JsNumber(taekwondoist.passportNumber.get))
      if (taekwondoist.grade.isDefined)
        json += (GRADE -> JsString(taekwondoist.grade.get))
      json
    }
  }

  implicit object TaekwondoistReads extends Reads[Taekwondoist] {
    def reads(json: JsValue): JsResult[Taekwondoist] = json match {
      case obj: JsValue => try {
        val person = PersonReads.reads(obj).get
        _id = person._id
        firstName = person.firstName
        lastName = person.lastName
        birthDate = person.birthDate
        addresses = person.addresses
        passportNumber = (obj \ PASSEPORT_NUMBER).asOpt[Int]
        grade = (obj \ GRADE).asOpt[String]
        JsSuccess(Taekwondoist)

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
      _id = person._id
      firstName = person.firstName
      lastName = person.lastName
      birthDate = person.birthDate
      addresses = person.addresses
      passportNumber = bson.getAs[Int](PASSEPORT_NUMBER)
      grade = bson.getAs[String](GRADE)
      Taekwondoist
    }
  }
}