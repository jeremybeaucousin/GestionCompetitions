package bo

import play.api.Logger
import play.api.libs.json._
import reactivemongo.bson
import scala.util.parsing.json.JSONObject
import reactivemongo.bson.BSONHandler
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.Macros

case class Route(
  var method: Option[String],  
  var url: Option[String],
  var parameters: Option[Map[String,String]],
  var body: Option[String],
  var errors: Option[Map[String,String]])
    
object Route {
  final val METHOD: String = "method"
  final val URL: String = "url"
  final val PARAMETERS: String = "parameters"
  final val BODY: String = "body"
  final val ERRORS: String = "errors"
  
  implicit def mapHandler: BSONHandler[BSONDocument, Map[String, String]] = ???

  implicit object RouteWrites extends Writes[Route] {
    def writes(route: Route): JsObject = {
      var json = Json.obj()
      if (route.method.isDefined)
        json += (METHOD -> JsString(route.method.get))
      if (route.url.isDefined)
        json += (URL -> JsString(route.url.get))
      if (route.parameters.isDefined)
        json += (PARAMETERS -> Json.toJson(route.parameters.get))
      if (route.body.isDefined)
        json += (BODY -> JsString(route.body.get))
      if (route.errors.isDefined)
        json += (ERRORS -> Json.toJson(route.errors.get))
      json
    }
  }

  implicit object RouteReads extends Reads[Route] {
    def reads(json: JsValue): JsResult[Route] = json match {
      case obj: JsValue => try {
        val method = (obj \ METHOD).asOpt[String]
        val url = (obj \ URL).asOpt[String]
        val parameters = (obj \ PARAMETERS).asOpt[Map[String,String]]
        val body = (obj \ BODY).asOpt[String]
        val errors = (obj \ ERRORS).asOpt[Map[String,String]]
        JsSuccess(Route(
          method,
          url,
          parameters,
          body,
          errors))

      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }
      case _ => JsError("expected.jsobject")
    }
  }

  import reactivemongo.bson._

  implicit object RouteWriter extends BSONDocumentWriter[Route] {
    def write(route: Route): BSONDocument = {
      var bson = BSONDocument()
      if (route.method.isDefined)
        bson ++= (METHOD -> route.method.get)
      if (route.url.isDefined)
        bson ++= (URL -> route.url.get)
      if (route.parameters.isDefined)
        bson ++= (PARAMETERS -> BSON.writeDocument(route.parameters.get))
      if (route.body.isDefined)
        bson ++= (BODY -> route.body.get)
      if (route.errors.isDefined)
        bson ++= (ERRORS -> BSON.writeDocument(route.errors.get))
      bson
    }
  }

  implicit object RouteReader extends BSONDocumentReader[Route] {
    def read(bson: BSONDocument): Route = {
      new Route(
        bson.getAs[String](METHOD),
        bson.getAs[String](URL),
        bson.getAs[Map[String,String]](PARAMETERS),
        bson.getAs[String](BODY),
        bson.getAs[Map[String,String]](ERRORS))
    }
  }
}