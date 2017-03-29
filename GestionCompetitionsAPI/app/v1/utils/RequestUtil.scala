package v1.utils

import play.api.libs.json.{ JsObject, Json }
import play.api.Logger
import play.modules.reactivemongo.json._
import reactivemongo.api.collections.bson
import reactivemongo.api.collections.bson.BSONCollection

import reactivemongo.bson.BSONArray
import reactivemongo.bson.BSONString
import play.api.mvc.Result
import javax.inject.Inject
import v1.managers.PersonManager
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import java.io.StringWriter
import java.io.PrintWriter
import play.api.i18n.Messages
import play.api.mvc.Headers
import v1.constantes.HttpConstants
import play.api.mvc.Request
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import play.api.mvc.Call
import scala.util.matching.Regex
import scala.util.matching.Regex.Match

object RequestUtil {

  def managePagination(result: Result, offset: Option[Int], limit: Option[Int], totalCount: Future[Int])
  (implicit request:Request[Any], ec: ExecutionContext, messages: Messages): Result = {
    val offsetValue = offset.getOrElse(0)
    val limitValue = limit.getOrElse(0)
    if (offsetValue > 0 || limitValue > 0) {
      val totalCountValue:Int = Await.ready(totalCount, Duration.Inf).value.get.getOrElse(0)
      val linkStringBuilder = new StringBuilder
      var callUrl = new Call(request.method, request.uri, "").absoluteURL()
      
      val offsetString = HttpConstants.queryFields.offset
      val limitString = HttpConstants.queryFields.limit
      val offsetRegex = (offsetString + """=\d+""")r
      val limitRegex = (limitString + """=\d+""")r
      
      var newOffsetValue: Int = offsetValue
      var newLimitValue: Int = limitValue
      var firstOffset = 0
      var lastOffset = (totalCountValue / limitValue) * limitValue
      var lastLimit = totalCountValue % limitValue
      
      // TODO Rewrite in a function
      // set the offset with last possible offset when offset is above total count and the next value in normal case 
      newOffsetValue = if((offsetValue + limitValue) < totalCountValue) (offsetValue + limitValue) else (lastOffset)
      // set the limit with the remaining elements when offset is above total count and the normal limit in normal case 
      newLimitValue = if((offsetValue + limitValue) < totalCountValue) (limitValue) else (lastLimit)
      callUrl = offsetRegex.replaceAllIn(callUrl, s"$offsetString=$newOffsetValue")
      callUrl = limitRegex.replaceAllIn(callUrl, s"$limitString=$newLimitValue")
      val nextLigne = s"""<$callUrl>; rel="next","""
      callUrl = offsetRegex.replaceAllIn(callUrl, s"$offsetString=$lastOffset")
      callUrl = limitRegex.replaceAllIn(callUrl, s"$limitString=$lastLimit")
      val lastLigne = s"""<$callUrl>; rel="last","""
      callUrl = offsetRegex.replaceAllIn(callUrl, s"$offsetString=$firstOffset")
      callUrl = limitRegex.replaceAllIn(callUrl, s"$limitString=$limitValue")
      val firstLigne = s"""<$callUrl>; rel="first","""
      // set the offset with the first possible offset when offset is negative and the previous value in normal case 
      newOffsetValue = if((offsetValue - limitValue) > 0) offsetValue - limitValue else firstOffset
      callUrl = offsetRegex.replaceAllIn(callUrl, s"$offsetString=$newOffsetValue")
      callUrl = limitRegex.replaceAllIn(callUrl, s"$limitString=$limitValue")
      val prevLigne = s"""<$callUrl>; rel="prev","""
      
      linkStringBuilder ++= nextLigne
      linkStringBuilder ++= lastLigne
      linkStringBuilder ++= firstLigne
      linkStringBuilder ++= prevLigne
      val totalCountHeaders = HttpConstants.headerFields.xTotalCount -> (totalCountValue.toString())
      val linkHeaders = HttpConstants.headerFields.link -> (linkStringBuilder.toString())
      result.withHeaders(totalCountHeaders, linkHeaders)
    } else {
      result
    }
  }
}