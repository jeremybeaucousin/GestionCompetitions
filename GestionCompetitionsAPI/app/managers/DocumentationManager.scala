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
import play.api.i18n.Messages
import constantes.MessageConstant
import org.apache.commons.lang3.StringUtils

@Singleton
class DocumentationManager @Inject() (implicit val ec: ExecutionContext) {
  def getPersonOperations(implicit messages: Messages): List[Operation] = {
    var availableOperations: List[Operation] = List[Operation]()
    var jsonPersonExemple = (Json.toJson(Person));
    
    val listPersonsOperation = Operation(
      Some(routes.PersonController.index()),
      Some(messages(MessageConstant.documentation.listPersonsDescription)),
      None,
      None,
      None,
      Some(messages(MessageConstant.documentation.listPersonsReturn)))
    availableOperations = listPersonsOperation :: availableOperations

    val getPersonOperation = Operation(
      Some(routes.PersonController.getPerson(Person._ID)),
      Some(messages(MessageConstant.documentation.getPersonDescription)),
      Some(getGetPersonParameters),
      None,
      None,
      Some(messages(MessageConstant.documentation.getPersonReturn)))
    availableOperations = getPersonOperation :: availableOperations

    val addPersonOperation = Operation(
      Some(routes.PersonController.addPerson()),
      Some(messages(MessageConstant.documentation.addPersonDescription)),
      None,
      Some(jsonPersonExemple),
      Some(getAddPersonsErrors.toMap),
      None)
    availableOperations = addPersonOperation :: availableOperations

    val editPersonOperation = Operation(
      Some(routes.PersonController.editPerson(Person._ID)),
      Some(messages(MessageConstant.documentation.editPersonDescription)),
      None,
      Some(jsonPersonExemple),
      None,
      None)
    availableOperations = editPersonOperation :: availableOperations
    
    val deletePersonOperation = Operation(
      Some(routes.PersonController.deletePerson(Person._ID)),
      Some(messages(MessageConstant.documentation.deletePersonDescription)),
      None,
      None,
      None,
      None)
    availableOperations = deletePersonOperation :: availableOperations
    
    availableOperations
  }

  private def getGetPersonParameters(implicit messages: Messages): Map[String, String] = {
    var parameters: Map[String, String] = Map[String, String]()
    parameters += (Person._ID -> messages(MessageConstant.documentation.getPersonIdParameterDescription))
    parameters
  }

  private def getAddPersonsErrors: Map[String, String] = {
    var errors: Map[String, String] = Map[String, String]()
    //    errors += ("error1" -> "errorValue")
    errors
  }
}
