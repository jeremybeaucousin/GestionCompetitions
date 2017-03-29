package v1.managers

import scala.concurrent.ExecutionContext
import v1.bo.Operation
import v1.bo.RequestContents
import v1.bo.Person
import v1.constantes.MessageConstants
import v1.controllers.routes
import javax.inject.Inject
import javax.inject.Singleton
import play.api.i18n.Messages
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import v1.utils.MongoDbUtil
import v1.constantes.HttpConstants

@Singleton
class DocumentationManager @Inject() (implicit val ec: ExecutionContext) {
  final val jsonPersonExemple = (Json.toJson(new Person))
  final val _idExemple = MongoDbUtil.generateId().stringify

  def getPersonOperations(implicit messages: Messages): Seq[Operation] = {
    var availableOperations: Seq[Operation] = Seq[Operation]()

    var listPersonsParams = Map[String, String]()
    listPersonsParams += getSortDescription
    listPersonsParams += getFieldsDescription
    listPersonsParams += getOffsetDescription
    listPersonsParams += getLimitDescription

    val listPersonsOperation = Operation()
    listPersonsOperation.call =
      Some(routes.PersonController.index(
        Some(Seq[String]("+" + Person.FIRST_NAME, "-" + Person.LAST_NAME)),
        Some(Seq[String](Person._ID, Person.FIRST_NAME, Person.LAST_NAME)),
        Some(0),
        Some(0)))
    listPersonsOperation.description = Some(messages(MessageConstants.documentation.person.listPersonsDescription))
    listPersonsOperation.request = Some(new RequestContents(Some(getGetPersonParameters), None))
    listPersonsOperation.response = Some(new RequestContents(None, Some(jsonPersonExemple)))

    //      ,
    //      Some(listPersonsParams),
    //      None,
    //      None,
    //      Some(messages(MessageConstants.documentation.person.listPersonsReturn)))
    availableOperations :+= listPersonsOperation

    val getPersonOperation = Operation()
    //    (
    //      Some(routes.PersonController.getPerson(_idExemple)),
    //      Some(messages(MessageConstants.documentation.person.getPersonDescription)),
    //      Some(getGetPersonParameters),
    //      None,
    //      None,
    //      Some(messages(MessageConstants.documentation.person.getPersonReturn)))
    availableOperations :+= getPersonOperation

    val addPersonOperation = Operation()
    //    (
    //      Some(routes.PersonController.addPerson()),
    //      Some(messages(MessageConstants.documentation.person.addPersonDescription)),
    //      None,
    //      Some(jsonPersonExemple),
    //      Some(getAddPersonsErrors.toMap),
    //      None)
    availableOperations :+= addPersonOperation

    val editPersonOperation = Operation()
    //    (
    //      Some(routes.PersonController.editPerson(_idExemple)),
    //      Some(messages(MessageConstants.documentation.person.editPersonDescription)),
    //      None,
    //      Some(jsonPersonExemple),
    //      None,
    //      None)
    availableOperations :+= editPersonOperation

    val deletePersonOperation = Operation()
    //      Some(routes.PersonController.deletePerson(_idExemple)),
    //      Some(messages(MessageConstants.documentation.person.deletePersonDescription)),
    //      None,
    //      None,
    //      None,
    //      None)
    availableOperations :+= deletePersonOperation

    availableOperations
  }

  private def getSortDescription(implicit messages: Messages): (String, String) = {
    (HttpConstants.queryFields.sort -> messages(MessageConstants.documentation.common.sortDescription))
  }

  private def getFieldsDescription(implicit messages: Messages): (String, String) = {
    (HttpConstants.queryFields.fields -> messages(MessageConstants.documentation.common.fieldsDescription))
  }

  private def getOffsetDescription(implicit messages: Messages): (String, String) = {
    (HttpConstants.queryFields.offset -> messages(MessageConstants.documentation.common.offsetDescription))
  }

  private def getLimitDescription(implicit messages: Messages): (String, String) = {
    (HttpConstants.queryFields.limit -> messages(MessageConstants.documentation.common.limitDescription))
  }

  private def getGetPersonParameters(implicit messages: Messages): Map[String, String] = {
    var parameters: Map[String, String] = Map[String, String]()
    parameters += (Person._ID -> messages(MessageConstants.documentation.person.getPersonIdParameterDescription))
    parameters
  }

  private def getAddPersonsErrors: Map[String, String] = {
    var errors: Map[String, String] = Map[String, String]()
    //    errors += ("error1" -> "errorValue")
    errors
  }
}
