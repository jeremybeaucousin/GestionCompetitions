package v1.bo

import play.api.Logger
import play.api.libs.json._
import reactivemongo.bson
import scala.util.parsing.json.JSONObject
import reactivemongo.bson.BSONHandler
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.Macros
import play.api.mvc.Call

case class Operation(
  // TODO add body and header for response and request  
  var call: Option[Call],  
  var description: Option[String],
  var parameters: Option[Map[String,String]],
  var body: Option[JsValue],
  var errors: Option[Map[String,String]],
  var response: Option[String])