package managers

import scala.concurrent.ExecutionContext
import javax.inject.Singleton
import bo.Operation
import play.api.mvc.Call
import bo.Address
import controllers.routes
import bo.Person
import java.util.Date
import play.api.libs.json.Json
import javax.inject.Inject
import play.api.libs.json.JsValue
import play.api.libs.json.JsObject
import play.Logger
import play.mvc.Http

@Singleton
class DocumentationManager @Inject() (implicit val ec: ExecutionContext) {
  def getPersonOperations: List[Operation] = {
    var availableOperations: List[Operation] = List[Operation]()
    val route = Operation(
      Some(routes.PersonController.addPerson()),
      None,
      Some((Json.toJson(Person))),
      Some(getAddPersonsErrors.toMap),
      None)
    availableOperations = route :: availableOperations
    availableOperations
  }

  private def getAddPersonsErrors: Map[String, String] = {
    var errors: Map[String, String] = Map[String, String]()
//    errors += ("error1" -> "errorValue")
    errors
  }
}
