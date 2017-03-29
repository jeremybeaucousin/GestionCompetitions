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
import org.apache.http.HttpStatus
import play.mvc.Http

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

    def getListPersonsOperation: Operation = {
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
      
      def getListPersonsErrors: Map[String, String] = {
        var errors: Map[String, String] = Map[String, String]()
        errors += (Http.Status.NO_CONTENT.toString() -> messages(MessageConstants.error.http.noContent))
        errors
      }
      listPersonsOperation.errors = Some(getListPersonsErrors)
      
      listPersonsOperation
    }
    availableOperations :+= getListPersonsOperation

    def getSearchPersonsOperation = {
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

      def getSearchPersonsErrors: Map[String, String] = {
        var errors: Map[String, String] = Map[String, String]()
        errors += (Http.Status.NO_CONTENT.toString() -> messages(MessageConstants.error.http.noContent))
        errors
      }
      searchPersonsOperation.errors = Some(getSearchPersonsErrors)
      searchPersonsOperation
    }
    availableOperations :+= getSearchPersonsOperation

    def getGetPersonOperation = {
      val getPersonOperation = Operation()
      getPersonOperation.call = Some(routes.PersonController.getPerson(_idExemple))
      getPersonOperation.description = Some(messages(MessageConstants.documentation.person.getPersonDescription))

      def getGetPersonRequestParameters(implicit messages: Messages): Map[String, String] = {
        var parameters: Map[String, String] = Map[String, String]()
        parameters += (Person._ID -> messages(MessageConstants.documentation.person.getPersonIdParameterDescription))
        parameters
      }

      val getPersonRequest = RequestContents()
      getPersonRequest.body = None
      getPersonRequest.parameters = Some(getGetPersonRequestParameters)
      getPersonOperation.request = Some(getPersonRequest)

      val getPersonsResponse = RequestContents()
      getPersonsResponse.body = Some(jsonPersonExemple)
      getPersonsResponse.headers = None
      getPersonOperation.response = Some(getPersonsResponse)

      def getGetPersonsErrors: Map[String, String] = {
        var errors: Map[String, String] = Map[String, String]()
        errors += (Http.Status.NOT_FOUND.toString() -> messages(MessageConstants.error.http.notFound))
        errors
      }
      getPersonOperation.errors = Some(getGetPersonsErrors)
      getPersonOperation
    }
    availableOperations :+= getGetPersonOperation

    def getAddPersonsErrors: Map[String, String] = {
      var errors: Map[String, String] = Map[String, String]()
      //    errors += ("error1" -> "errorValue")
      errors
    }

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
