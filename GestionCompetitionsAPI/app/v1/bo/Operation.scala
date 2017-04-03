package v1.bo

import play.api.Logger
import play.api.libs.json._
import reactivemongo.bson
import scala.util.parsing.json.JSONObject
import reactivemongo.bson.BSONHandler
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.Macros
import play.api.mvc.Call

case class RequestContents (
  var parameters: Option[Map[String,String]],
  var headers: Option[Map[String,String]],
  var body: Option[JsValue]
)

object RequestContents {
   def apply() = new RequestContents(None, None, None)
}

case class Operation(
  var call: Option[Call],  
  var description: Option[String],
  var request: Option[RequestContents],
  var response: Option[RequestContents],
  var codes: Option[Map[String,String]]
  )
  
// TODO Add Exceptions
object Operation {
  def apply() = new Operation(None, None, None, None, None)
}