package v1.model;

import java.util.Date
import play.api.Logger
import play.api.libs.json._
import reactivemongo.bson
import reactivemongo.bson.BSONObjectID
import v1.model.Person.PersonWrites
import v1.model.Person.PersonWriter
import v1.model.Person.PersonReader
import org.apache.commons.lang3.StringUtils
import v1.model.Person.PersonFormat

case class Taekwondoist(
    var _id: Option[String] = None,
    var firstName: Option[String] = None,
    var lastName: Option[String] = None,
    var birthDate: Option[Date] = None,
    var login: Option[String] = None,
    var email: Option[String] = None,
    var role: Option[String] = None,
    var encryptedEmailToken: Option[String] = None,
    var emailTokenExpirationTime: Option[Date],
    var password: Option[String] = None,
    var encryptedPassword: Option[String] = None,
    var addresses: Option[List[Address]] = None,
    var passportNumber: Option[Int] = None,
    var grade: Option[String] = None) {

  def toPerson(): Person = {
    Person(
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
}

object Taekwondoist {
  final val PASSEPORT_NUMBER: String = "passportNumber"
  final val GRADE: String = "grade"

  //  def eval(e: Expr, env: Env): Value = e match {
  //  case Var (x) =>
  //    env(x)
  //  case Apply(f, g) =>
  //    val Value(Lambda (x, e1), env1) = eval(f, env)
  //    val v = eval(g, env)
  //    eval (e1, (y => if (y == x) v else env1(y)))
  //  case Lambda(_, _) =>
  //    Value(e, env)
  //}

  implicit object TaekwondoistWrites extends Writes[Taekwondoist] {
    def writes(taekwondoist: Taekwondoist): JsObject = {
      var json = PersonWrites.writes(taekwondoist.toPerson())
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
        val person = PersonFormat.reads(obj).get
        var taekwondoist = person.toTaekwondoist
        taekwondoist.passportNumber = (obj \ PASSEPORT_NUMBER).asOpt[Int]
        taekwondoist.grade = (obj \ GRADE).asOpt[String]
        JsSuccess(taekwondoist)

      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }

      case _ => JsError("expected.jsobject")
    }
  }

  import reactivemongo.bson._

  implicit object TaekwondoistWriter extends BSONDocumentWriter[Taekwondoist] {
    def write(taekwondoist: Taekwondoist): BSONDocument = {
      var bson = PersonWriter.write(taekwondoist.toPerson())
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
      var taekwondoist = person.toTaekwondoist()
      taekwondoist.passportNumber = bson.getAs[Int](PASSEPORT_NUMBER)
      taekwondoist.grade = bson.getAs[String](GRADE)
      taekwondoist
    }
  }
}