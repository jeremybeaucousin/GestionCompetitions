package v1.managers

import scala.concurrent.ExecutionContext
import v1.bo.Operation
import v1.bo.Person
import v1.constantes.MessageConstant
import v1.controllers.routes
import javax.inject.Inject
import javax.inject.Singleton
import play.api.i18n.Messages
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import v1.utils.MongoDbUtil

@Singleton
class DocumentationManager @Inject() (implicit val ec: ExecutionContext) {
  final val jsonPersonExemple = (Json.toJson(new Person))
  final val _idExemple = MongoDbUtil.generateId().stringify
  
  def getPersonOperations(implicit messages: Messages): List[Operation] = {
    var availableOperations: List[Operation] = List[Operation]()
    
    val listPersonsOperation = Operation(
      Some(routes.PersonController.index(
          Some(Seq[String]("+" + Person.FIRST_NAME, "-" +  Person.LAST_NAME)), 
          Some(Seq[String](Person._ID, Person.FIRST_NAME, Person.LAST_NAME)),
          Some(0), 
          Some(0))),
      Some(messages(MessageConstant.documentation.listPersonsDescription)),
      None,
      None,
      None,
      Some(messages(MessageConstant.documentation.listPersonsReturn)))
    availableOperations = listPersonsOperation :: availableOperations

    val getPersonOperation = Operation(
      Some(routes.PersonController.getPerson(_idExemple)),
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
      Some(routes.PersonController.editPerson(_idExemple)),
      Some(messages(MessageConstant.documentation.editPersonDescription)),
      None,
      Some(jsonPersonExemple),
      None,
      None)
    availableOperations = editPersonOperation :: availableOperations
    
    val deletePersonOperation = Operation(
      Some(routes.PersonController.deletePerson(_idExemple)),
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
