package v1.utils

import play.api.libs.json.{ JsObject, Json }
import play.api.Logger
import play.modules.reactivemongo.json._
import reactivemongo.api.collections.bson
import reactivemongo.api.collections.bson.BSONCollection

import reactivemongo.bson.{ BSON, BSONDocument, BSONObjectID }
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

object RequestUtil {

  def managePagination(result: Result, offset: Option[Int], limit: Option[Int], totalCount: Future[Int])(implicit ec: ExecutionContext, messages: Messages): Result = {
    val offsetValue = offset.getOrElse(0)
    val limitValue = limit.getOrElse(0)
    var returnedResult = result

    if (offsetValue > 0 || limitValue > 0) {
//      totalCount.onComplete {
//        case Failure(exception) => {
//          val sw = new StringWriter
//          exception.printStackTrace(new PrintWriter(sw))
//          Logger.error(sw.toString)
//        }
//        case Success(totalCount) => {
//          // TODO CREATE Pagination if necessary
//          //    Link: <https://blog.mwaysolutions.com/sample/api/v1/cars?offset=15&limit=5>; rel="next",
//          //   <https://blog.mwaysolutions.com/sample/api/v1/cars?offset=50&limit=3>; rel="last",
//          //   <https://blog.mwaysolutions.com/sample/api/v1/cars?offset=0&limit=5>; rel="first",
//          //   <https://blog.mwaysolutions.com/sample/api/v1/cars?offset=5&limit=5>; rel="prev",
//        }
//      }
      
      Logger.info(totalCount.value.get.get.toString())
      Logger.info("result : " + returnedResult.header.toString())
      // TODO Manage future properly
      val totalCountHeaders = HttpConstants.headerFields.xTotalCount -> (totalCount.value.get.get.toString())
      val linkHeaders = HttpConstants.headerFields.link -> (totalCount.toString())
      result.withHeaders(totalCountHeaders, linkHeaders)
    } else {
      result
    }
  }
}