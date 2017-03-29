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
  final val jsonPersonArrayExemple = (Json.toJson(List[Person](new Person, new Person)))
  final val _idExemple = MongoDbUtil.generateId().stringify

  def getPersonOperations(implicit messages: Messages): Seq[Operation] = {
    var availableOperations: Seq[Operation] = Seq[Operation]()

    def getSortDescription(implicit messages: Messages): (String, String) = {
      (HttpConstants.queryFields.sort -> messages(MessageConstants.documentation.common.sortDescription))
    }

    def getFieldsDescription(implicit messages: Messages): (String, String) = {
      (HttpConstants.queryFields.fields -> messages(MessageConstants.documentation.common.fieldsDescription))
    }

    def getOffsetDescription(implicit messages: Messages): (String, String) = {
      (HttpConstants.queryFields.offset -> messages(MessageConstants.documentation.common.offsetDescription))
    }

    def getLimitDescription(implicit messages: Messages): (String, String) = {
      (HttpConstants.queryFields.limit -> messages(MessageConstants.documentation.common.limitDescription))
    }

    val listPersonsOperation = Operation()
    listPersonsOperation.call =
      Some(routes.PersonController.index(
        Some(Seq[String]("+" + Person.FIRST_NAME, "-" + Person.LAST_NAME)),
        Some(Seq[String](Person._ID, Person.FIRST_NAME, Person.LAST_NAME)),
        Some(0),
        Some(0)))
    listPersonsOperation.description = Some(messages(MessageConstants.documentation.person.listPersonsDescription))

    def getGetPersonsRequestParameters: Map[String, String] = {
      var parameters: Map[String, String] = Map[String, String]()
      parameters += getSortDescription
      parameters += getFieldsDescription
      parameters += getOffsetDescription
      parameters += getLimitDescription
      parameters
    }

    val listPersonsRequest = RequestContents()
    listPersonsRequest.parameters = Some(getGetPersonsRequestParameters)
    listPersonsOperation.request = Some(listPersonsRequest)

    def getGetPersonsResponseParameters: Map[String, String] = {
      var parameters: Map[String, String] = Map[String, String]()
      parameters += (HttpConstants.headerFields.xTotalCount -> messages(MessageConstants.documentation.common.xTotalCountDescription))
      parameters += (HttpConstants.headerFields.link -> messages(MessageConstants.documentation.common.linkDescription))
      parameters
    }
    
    val listPersonsResponse = RequestContents()
    listPersonsResponse.body = Some(jsonPersonArrayExemple)
    listPersonsResponse.headers = Some(getGetPersonsResponseParameters)
    listPersonsOperation.response = Some(listPersonsResponse)

    availableOperations :+= listPersonsOperation
    
    val searchPersonsOperation = Operation()
    searchPersonsOperation.call =
      Some(routes.PersonController.searchPersons(
        Some(Seq[String]("+" + Person.FIRST_NAME, "-" + Person.LAST_NAME)),
        Some(Seq[String](Person._ID, Person.FIRST_NAME, Person.LAST_NAME)),
        Some(0),
        Some(0)))
    searchPersonsOperation.description = Some(messages(MessageConstants.documentation.person.searchPersonsDescription))

    def getSearchPersonsRequestParameters(implicit messages: Messages): Map[String, String] = {
      var parameters: Map[String, String] = Map[String, String]()
      parameters += getSortDescription
      parameters += getFieldsDescription
      parameters += getOffsetDescription
      parameters += getLimitDescription
      parameters
    }

    val searchPersonsRequest = RequestContents()
    searchPersonsRequest.body = Some(jsonPersonExemple)
    searchPersonsRequest.parameters = Some(getSearchPersonsRequestParameters)
    searchPersonsOperation.request = Some(searchPersonsRequest)

    def getSearchPersonsHeadersParameters(implicit messages: Messages): Map[String, String] = {
      var parameters: Map[String, String] = Map[String, String]()
      parameters += (HttpConstants.headerFields.xTotalCount -> messages(MessageConstants.documentation.common.xTotalCountDescription))
      parameters += (HttpConstants.headerFields.link -> messages(MessageConstants.documentation.common.linkDescription))
      parameters
    }
    
    val searchPersonsResponse = RequestContents()
    searchPersonsResponse.body = Some(jsonPersonArrayExemple)
    searchPersonsResponse.headers = Some(getSearchPersonsHeadersParameters)
    searchPersonsOperation.response = Some(searchPersonsResponse)

    availableOperations :+= searchPersonsOperation

    def getGetPersonRequestParameters(implicit messages: Messages): Map[String, String] = {
      var parameters: Map[String, String] = Map[String, String]()
      parameters += (Person._ID -> messages(MessageConstants.documentation.person.getPersonIdParameterDescription))
      parameters
    }

    def getAddPersonsErrors: Map[String, String] = {
      var errors: Map[String, String] = Map[String, String]()
      //    errors += ("error1" -> "errorValue")
      errors
    }

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
}
