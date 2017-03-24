package http

import play.api.Logger
import play.api.libs.json._
import reactivemongo.bson


case class Error(
  var code: Option[Int],
  var userMessage: Option[String],  
  var internalMessage: Option[String],
  var moreInfo: Option[String])
  
object Error {
  final val CODE: String = "code"
  final val USER_MESSAGE: String = "userMessage"
  final val INTERNAL_MESSAGE: String = "internalMessage"
  final val MORE_INFO: String = "moreInfo"

  implicit object ErrorWrites extends Writes[Error] {
    def writes(error: Error): JsObject = {
      var json = Json.obj()
      if (error.code.isDefined)
        json += (CODE -> JsNumber.apply(error.code.get))
      if (error.userMessage.isDefined)
        json += (USER_MESSAGE -> JsString.apply(error.userMessage.get))
      if (error.internalMessage.isDefined)
        json += (INTERNAL_MESSAGE -> JsString.apply(error.internalMessage.get))
      if (error.moreInfo.isDefined)
        json += (MORE_INFO -> JsString.apply(error.moreInfo.get))
      json
    }
  }

  implicit object ErrorReads extends Reads[Error] {
    def reads(json: JsValue): JsResult[Error] = json match {
      case obj: JsValue => try {
        val code = (obj \ CODE).asOpt[Int]
        val userMessage = (obj \ USER_MESSAGE).asOpt[String]
        val internalMessage = (obj \ INTERNAL_MESSAGE).asOpt[String]
        val moreInfo = (obj \ MORE_INFO).asOpt[String]
        JsSuccess(Error(
          code,
          userMessage,
          internalMessage,
          moreInfo))

      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }
      case _ => JsError("expected.jsobject")
    }
  }

  import reactivemongo.bson._

  implicit object ErrorWriter extends BSONDocumentWriter[Error] {
    def write(error: Error): BSONDocument = {
      var bson = BSONDocument()
      if (error.code.isDefined)
        bson ++= (CODE -> error.code.get)
      if (error.userMessage.isDefined)
        bson ++= (USER_MESSAGE -> error.userMessage.get)
      if (error.internalMessage.isDefined)
        bson ++= (INTERNAL_MESSAGE -> error.internalMessage.get)
      if (error.moreInfo.isDefined)
        bson ++= (MORE_INFO -> error.moreInfo.get)
      bson
    }
  }

  implicit object ErrorReader extends BSONDocumentReader[Error] {
    def read(bson: BSONDocument): Error = {
      new Error(
        bson.getAs[Int](CODE),
        bson.getAs[String](USER_MESSAGE),
        bson.getAs[String](INTERNAL_MESSAGE),
        bson.getAs[String](MORE_INFO))
    }
  }
}