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
import scala.util.parsing.json.JSONArray
import akka.util.Switch
import play.api.http.MimeTypes

@Singleton
class ErrorHandler extends HttpErrorHandler {

  def onClientError(requestHeader: RequestHeader, statusCode: Int, message: String) = {
    var errors: List[Error] = List[Error]()
    val error: Error = new Error(
      Some(statusCode),
      Some("Une erreur s'est produite sur le serveur lors de l'appel suivant : " + requestHeader.path),
      Some(message),
      None)
    errors = error :: errors
    if (requestHeader.accepts(MimeTypes.JSON)) {
      Future.successful(
        Status(statusCode)(
          Json.obj("errors" -> errors)))
    } else {
      Future.successful(
        Status(statusCode)("Une erreur s'est produite sur le serveur lors de l'appel suivant : " + errors.toString()))
    }
  }

  def onServerError(requestHeader: RequestHeader, exception: Throwable) = {
    var errors: List[Error] = List[Error]()
    val sw = new StringWriter
    exception.printStackTrace(new PrintWriter(sw))
    Logger.error(sw.toString)
    val error: Error = new Error(
      Some(HttpStatus.SC_INTERNAL_SERVER_ERROR),
      Some("Une erreur s'est produite sur le serveur lors de l'appel suivant : " + requestHeader.path),
      Some(exception.getMessage),
      Some(sw.toString))
    errors = error :: errors
    if (requestHeader.accepts(MimeTypes.JSON)) {
      Future.successful(
        InternalServerError(
          Json.obj("errors" -> errors)))
    } else {
      Future.successful(
        InternalServerError("Une erreur s'est produite sur le serveur: " + errors.toString()))
    }
  }
}