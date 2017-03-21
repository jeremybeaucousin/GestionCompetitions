package http

import java.io.{ StringWriter, PrintWriter }
import play.api.libs.json.Json
import play.api.http.HttpErrorHandler
import play.api.Logger
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent._
import javax.inject.Singleton;
import org.apache.http.HttpStatus

@Singleton
class ErrorHandler extends HttpErrorHandler {

  def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    Future.successful(
      Status(statusCode)("A client error occurred: " + message))
  }

  def onServerError(request: RequestHeader, exception: Throwable) = {
    val sw = new StringWriter
    exception.printStackTrace(new PrintWriter(sw))
    Logger.error(sw.toString)
    Future.successful(
      InternalServerError(
        Json.obj(
          "status" -> HttpStatus.SC_INTERNAL_SERVER_ERROR,
          "detail" -> exception.getMessage)))
  }
}