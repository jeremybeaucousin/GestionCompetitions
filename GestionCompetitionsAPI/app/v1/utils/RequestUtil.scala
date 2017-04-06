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
import v1.services.PersonServices
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
import scala.concurrent.duration.Duration
import play.api.mvc.Call
import scala.util.matching.Regex
import scala.util.matching.Regex.Match
import org.apache.commons.lang3.StringUtils
import scala.xml.dtd.EMPTY

object RequestUtil {

  def handleFutureListAndTotal[T](futureList: Future[List[T]], futurTotalCount: Future[Int])(implicit ec: ExecutionContext): Future[(List[T], Int)] = {
    futureList.flatMap { elements =>
      futurTotalCount.map(totalCount => {
        (elements, totalCount)
      })
    }
  }

  def handlePagination(result: Result, offset: Option[Int], limit: Option[Int], totalCountValue: Int)(implicit request: Request[Any], ec: ExecutionContext, messages: Messages): Result = {
    val offsetValue = offset.getOrElse(0)
    val limitValue = limit.getOrElse(0)
    if (offsetValue > 0 || limitValue > 0) {
      val linkStringBuilder = new StringBuilder

      if (offsetValue < totalCountValue) {
        val callUrl = new Call(request.method, request.uri, "").absoluteURL()

        var newOffsetValue: Int = offsetValue
        var newLimitValue: Int = limitValue
        var firstOffset = 0
        var lastOffset = (totalCountValue / limitValue) * limitValue
        var lastLimit = totalCountValue % limitValue

        def generateLinkLigne(url: String, offset: Int, limit: Int): String = {
          var returnUrl = url
          val offsetString = HttpConstants.queryFields.offset
          val limitString = HttpConstants.queryFields.limit
          val offsetRegex = (offsetString + """=\d+""")r
          val limitRegex = (limitString + """=\d+""")r

          returnUrl = offsetRegex.replaceAllIn(returnUrl, s"$offsetString=$offset")
          returnUrl = limitRegex.replaceAllIn(returnUrl, s"$limitString=$limit")
          returnUrl
        }

        val startOfLine: String = HttpConstants.HTML_LT
        val middleOfligne: String = HttpConstants.HTML_GT + "; rel=\""
        val EndOfLine: String = "\","

        if (offsetValue != firstOffset) {
          linkStringBuilder ++= startOfLine + generateLinkLigne(callUrl, firstOffset, limitValue) + middleOfligne + HttpConstants.FIRST + EndOfLine
          // set the offset with the first possible offset when offset is negative and the previous value in normal case 
          newOffsetValue = if ((offsetValue - limitValue) > 0) offsetValue - limitValue else firstOffset
          linkStringBuilder ++= startOfLine + generateLinkLigne(callUrl, newOffsetValue, limitValue) + middleOfligne + HttpConstants.PREV
        }

        if (offsetValue != lastOffset) {
          linkStringBuilder ++= EndOfLine
          // set the offset with last possible offset when offset is above total count and the next value in normal case 
          newOffsetValue = if ((offsetValue + limitValue) < totalCountValue) (offsetValue + limitValue) else (lastOffset)
          // set the limit with the remaining elements when offset with next limite is above total count and the normal limit in normal case 
          newLimitValue = if ((offsetValue + (limitValue * 2)) < totalCountValue) (limitValue) else (lastLimit)
          linkStringBuilder ++= startOfLine + generateLinkLigne(callUrl, newOffsetValue, newLimitValue) + middleOfligne + HttpConstants.NEXT + EndOfLine
          linkStringBuilder ++= startOfLine + generateLinkLigne(callUrl, lastOffset, lastLimit) + middleOfligne + HttpConstants.LAST
        }
      }
      val totalCountHeaders = HttpConstants.headerFields.xTotalCount -> (totalCountValue.toString())
      val linkHeaders = HttpConstants.headerFields.link -> (linkStringBuilder.toString())
      result.withHeaders(totalCountHeaders, linkHeaders)
    } else {
      result
    }
  }
}