package v1.bo;
//package bo
//
//import java.util.Date
//import play.api.Logger
//import play.api.libs.json._
//import reactivemongo.bson
//import reactivemongo.bson.BSONObjectID
//import v1.bo.Person.PersonWrites
//import v1.bo.Person.PersonReads
//import v1.bo.Person.PersonWriter
//import v1.bo.Person.PersonReader
//import org.apache.commons.lang3.StringUtils
//
//case class Taekwondoist(
//  var passportNumber: Option[Int] = Some(0),
//  var grade: Option[String] = Some(null)) extends Person
////  (
////  Some(StringUtils.EMPTY),
////  Some(StringUtils.EMPTY),
////  Some(StringUtils.EMPTY),
////  Some(new Date),
////  Some(List[Address](new Address, new Address)))
//
//object Taekwondoist {
//  final val PASSEPORT_NUMBER: String = "passportNumber"
//  final val GRADE: String = "grade"
//
//  implicit object TaekwondoistWrites extends Writes[Taekwondoist] {
//    def writes(taekwondoist: Taekwondoist): JsObject = {
//      var json = PersonWrites.writes(taekwondoist)
//      if (taekwondoist.passportNumber.isDefined)
//        json += (PASSEPORT_NUMBER -> JsNumber(taekwondoist.passportNumber.get))
//      if (taekwondoist.grade.isDefined)
//        json += (GRADE -> JsString(taekwondoist.grade.get))
//      json
//    }
//  }
//
//  implicit object TaekwondoistReads extends Reads[Taekwondoist] {
//    def reads(json: JsValue): JsResult[Taekwondoist] = json match {
//      case obj: JsValue => try {
//        val person = PersonReads.reads(obj).get
//        _id = person._id
//        firstName = person.firstName
//        lastName = person.lastName
//        birthDate = person.birthDate
//        addresses = person.addresses
//        passportNumber = (obj \ PASSEPORT_NUMBER).asOpt[Int]
//        grade = (obj \ GRADE).asOpt[String]
//        JsSuccess(Taekwondoist)
//
//      } catch {
//        case cause: Throwable => JsError(cause.getMessage)
//      }
//
//      case _ => JsError("expected.jsobject")
//    }
//  }
//
//  import reactivemongo.bson._
//
//  implicit object TaekwondoistWriter extends BSONDocumentWriter[Taekwondoist] {
//    def write(taekwondoist: Taekwondoist): BSONDocument = {
//      var bson = PersonWriter.write(taekwondoist)
//      if (taekwondoist.passportNumber.isDefined)
//        bson ++= (PASSEPORT_NUMBER -> taekwondoist.passportNumber.get)
//      if (taekwondoist.grade.isDefined)
//        bson ++= (GRADE -> taekwondoist.grade.get)
//      bson
//    }
//  }
//
//  implicit object TaekwondoistReader extends BSONDocumentReader[Taekwondoist] {
//    def read(bson: BSONDocument): Taekwondoist = {
//      val person = PersonReader.read(bson)
//      _id = person._id
//      firstName = person.firstName
//      lastName = person.lastName
//      birthDate = person.birthDate
//      addresses = person.addresses
//      passportNumber = bson.getAs[Int](PASSEPORT_NUMBER)
//      grade = bson.getAs[String](GRADE)
//      Taekwondoist
//    }
//  }
//}