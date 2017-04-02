package v1.http

import play.api.Logger
import play.api.libs.json._
import reactivemongo.bson

case class Error(
  var code: Option[Int],
  var field: Option[String],
  var userMessage: Option[String],
  var internalMessage: Option[String],
  var moreInfo: Option[String],
  var errors: Option[List[Error]])
  

object Error {
  final val CODE: String = "code"
  final val FIELD: String = "field"
  final val USER_MESSAGE: String = "userMessage"
  final val INTERNAL_MESSAGE: String = "internalMessage"
  final val MORE_INFO: String = "moreInfo"
  final val ERRORS: String = "errors"

  def apply() = new Error(None, None, None, None, None, None)

  implicit object ErrorWrites extends Writes[Error] {
    def writes(error: Error): JsObject = {
      var json = Json.obj()
      if (error.code.isDefined)
        json += (CODE -> JsNumber.apply(error.code.get))
      if (error.field.isDefined)
        json += (FIELD -> JsString.apply(error.field.get))
      if (error.userMessage.isDefined)
        json += (USER_MESSAGE -> JsString.apply(error.userMessage.get))
      if (error.internalMessage.isDefined)
        json += (INTERNAL_MESSAGE -> JsString.apply(error.internalMessage.get))
      if (error.moreInfo.isDefined)
        json += (MORE_INFO -> JsString.apply(error.moreInfo.get))
       if (error.errors.isDefined)
        json += (ERRORS -> Json.toJson(error.errors.get))
      json
    }
  }
}