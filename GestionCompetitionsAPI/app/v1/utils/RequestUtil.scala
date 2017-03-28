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

object RequestUtil {

  def managePagination(result: Result, offset: Option[Int], limit: Option[Int]): Result = {
    //    result.withHeaders(headers)
    // TODO CREATE Pagination if necessary
    //    Link: <https://blog.mwaysolutions.com/sample/api/v1/cars?offset=15&limit=5>; rel="next",
    //   <https://blog.mwaysolutions.com/sample/api/v1/cars?offset=50&limit=3>; rel="last",
    //   <https://blog.mwaysolutions.com/sample/api/v1/cars?offset=0&limit=5>; rel="first",
    //   <https://blog.mwaysolutions.com/sample/api/v1/cars?offset=5&limit=5>; rel="prev",
    result
  }
}