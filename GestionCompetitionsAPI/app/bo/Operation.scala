package bo

import play.api.Logger
import play.api.libs.json._
import reactivemongo.bson
import scala.util.parsing.json.JSONObject
import reactivemongo.bson.BSONHandler
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.Macros
import play.api.mvc.Call

case class Operation(
  var call: Option[Call],  
  var parameters: Option[Map[String,String]],
  var body: Option[JsValue],
  var errors: Option[Map[String,String]],
  var response: Option[String])