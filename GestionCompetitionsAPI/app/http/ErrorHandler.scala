package http

import akka.util.Switch
import java.io.{ StringWriter, PrintWriter }
import javax.inject.Inject
import javax.inject.Singleton;
import org.apache.http.HttpStatus
import play.api.http.HttpErrorHandler
import play.api.http.MimeTypes
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.Logger
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent._
import scala.util.parsing.json.JSONArray
import play.api.i18n.Lang
import java.util.Locale
import play.api.libs.concurrent.Execution
import play.api.Play
import play.api.Application

@Singleton
class ErrorHandler @Inject()(val messagesApi: MessagesApi) (implicit ec: ExecutionContext)
  extends HttpErrorHandler with I18nSupport  {

  def onClientError(requestHeader: RequestHeader, statusCode: Int, message: String) = {
    val messages = messagesApi.preferred(requestHeader)
    // TODO define constant
    val userMessage = messages("error.client", requestHeader.path)
    var errors: List[Error] = List[Error]()
    val error: Error = new Error(
      Some(statusCode),
      Some(userMessage),
      Some(message),
      None)
    errors = error :: errors
    if (requestHeader.accepts(MimeTypes.JSON)) {
      Future.successful(
        Status(statusCode)(
          Json.obj("errors" -> errors)))
    } else {
      Future.successful(
        Status(statusCode)(userMessage))
    }
  }

  def onServerError(requestHeader: RequestHeader, exception: Throwable) = { 
    val messages = messagesApi.preferred(requestHeader)
    val userMessage = messages("error.server", requestHeader.path)
    var errors: List[Error] = List[Error]()
    val sw = new StringWriter
    exception.printStackTrace(new PrintWriter(sw))
    Logger.error(sw.toString)
    val error: Error = new Error(
      Some(HttpStatus.SC_INTERNAL_SERVER_ERROR),
      Some(userMessage),
      Some(exception.getMessage),
      Some(sw.toString))
    errors = error :: errors
    if (requestHeader.accepts(MimeTypes.JSON)) {
      Future.successful(
        InternalServerError(
           // TODO define constant
          Json.obj("errors" -> errors)))
    } else {
      Future.successful(
        InternalServerError(userMessage))
    }
  }
  
}