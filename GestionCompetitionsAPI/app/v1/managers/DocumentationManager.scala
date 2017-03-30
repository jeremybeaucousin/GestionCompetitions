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
  final val sortExemple = Seq[String]("+" + Person.FIRST_NAME, "-" + Person.LAST_NAME)
  final val fieldsExemple = Seq[String](Person._ID, Person.FIRST_NAME, Person.LAST_NAME)

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
          Some(sortExemple),
          Some(fieldsExemple),
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

      def getListPersonsCodes: Map[String, String] = {
        var codes: Map[String, String] = Map[String, String]()
        codes += (Http.Status.OK.toString() -> messages(MessageConstants.http.ok))
        codes += (Http.Status.NO_CONTENT.toString() -> messages(MessageConstants.http.noContent))
        codes
      }
      listPersonsOperation.codes = Some(getListPersonsCodes)

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

      def getSearchPersonsCodes: Map[String, String] = {
        var codes: Map[String, String] = Map[String, String]()
        codes += (Http.Status.OK.toString() -> messages(MessageConstants.http.ok))
        codes += (Http.Status.NO_CONTENT.toString() -> messages(MessageConstants.http.noContent))
        codes
      }
      searchPersonsOperation.codes = Some(getSearchPersonsCodes)
      searchPersonsOperation
    }
    availableOperations :+= getSearchPersonsOperation

    def getGetPersonOperation = {
      val getPersonOperation = Operation()
      getPersonOperation.call = Some(routes.PersonController.getPerson(_idExemple, Some(fieldsExemple)))
      getPersonOperation.description = Some(messages(MessageConstants.documentation.person.getPersonDescription))

      def getGetPersonRequestParameters(implicit messages: Messages): Map[String, String] = {
        var parameters: Map[String, String] = Map[String, String]()
        parameters += (Person._ID -> messages(MessageConstants.documentation.person.getPersonIdParameterDescription))
        parameters += getFieldsDescription
        parameters
      }

      val getPersonRequest = RequestContents()
      getPersonRequest.parameters = Some(getGetPersonRequestParameters)
      getPersonOperation.request = Some(getPersonRequest)

      val getPersonsResponse = RequestContents()
      getPersonsResponse.body = Some(jsonPersonExemple)
      getPersonOperation.response = Some(getPersonsResponse)

      def getGetPersonsCodes: Map[String, String] = {
        var codes: Map[String, String] = Map[String, String]()
        codes += (Http.Status.OK.toString() -> messages(MessageConstants.http.ok))
        codes += (Http.Status.NOT_FOUND.toString() -> messages(MessageConstants.http.notFound))
        codes
      }
      getPersonOperation.codes = Some(getGetPersonsCodes)
      getPersonOperation
    }
    availableOperations :+= getGetPersonOperation

    def getAddPersonOperation = {

      val addPersonOperation = Operation()
      addPersonOperation.call = Some(routes.PersonController.addPerson())
      addPersonOperation.description = Some(messages(MessageConstants.documentation.person.addPersonDescription))

      val getPersonRequest = RequestContents()
      getPersonRequest.body = Some(jsonPersonExemple)
      addPersonOperation.request = Some(getPersonRequest)

      def getAddPersonsResponseParameters: Map[String, String] = {
        var parameters: Map[String, String] = Map[String, String]()
        parameters += (HttpConstants.headerFields.location -> messages(MessageConstants.documentation.common.locationDescription))
        parameters
      }

      val addPersonsResponse = RequestContents()
      addPersonsResponse.headers = Some(getAddPersonsResponseParameters)

      def getAddPersonsCodes: Map[String, String] = {
        var codes: Map[String, String] = Map[String, String]()
        codes += (Http.Status.OK.toString() -> messages(MessageConstants.http.ok))
        codes += (Http.Status.UNPROCESSABLE_ENTITY.toString() -> messages(MessageConstants.http.unprocessableEntity))
        codes
      }
      addPersonOperation.codes = Some(getAddPersonsCodes)
      addPersonOperation
    }
    availableOperations :+= getAddPersonOperation

    def getEditPersonOperation = {

      val editPersonOperation = Operation()
      editPersonOperation.call = Some(routes.PersonController.editPerson(_idExemple))
      editPersonOperation.description = Some(messages(MessageConstants.documentation.person.editPersonDescription))

      def getEditPersonRequestParameters(implicit messages: Messages): Map[String, String] = {
        var parameters: Map[String, String] = Map[String, String]()
        parameters += (Person._ID -> messages(MessageConstants.documentation.person.getPersonIdParameterDescription))
        parameters
      }

      val editPersonRequest = RequestContents()
      editPersonRequest.body = Some(jsonPersonExemple)
      editPersonRequest.parameters = Some(getEditPersonRequestParameters)
      editPersonOperation.request = Some(editPersonRequest)

      def getEditPersonsCodes: Map[String, String] = {
        var codes: Map[String, String] = Map[String, String]()
        codes += (Http.Status.OK.toString() -> messages(MessageConstants.http.ok))
        codes += (Http.Status.UNPROCESSABLE_ENTITY.toString() -> messages(MessageConstants.http.unprocessableEntity))
        codes
      }
      editPersonOperation.codes = Some(getEditPersonsCodes)
      editPersonOperation
    }
    availableOperations :+= getEditPersonOperation

    def getDeletePersonOperation = {
      val deletePersonOperation = Operation()
      deletePersonOperation.call = Some(routes.PersonController.deletePerson(_idExemple))
      deletePersonOperation.description = Some(messages(MessageConstants.documentation.person.deletePersonDescription))

      def getDeletePersonRequestParameters(implicit messages: Messages): Map[String, String] = {
        var parameters: Map[String, String] = Map[String, String]()
        parameters += (Person._ID -> messages(MessageConstants.documentation.person.getPersonIdParameterDescription))
        parameters
      }

      val editPersonRequest = RequestContents()
      editPersonRequest.parameters = Some(getDeletePersonRequestParameters)
      deletePersonOperation.request = Some(editPersonRequest)

      def getEditPersonsCodes: Map[String, String] = {
        var codes: Map[String, String] = Map[String, String]()
        codes += (Http.Status.OK.toString() -> messages(MessageConstants.http.ok))
        codes += (Http.Status.UNPROCESSABLE_ENTITY.toString() -> messages(MessageConstants.http.unprocessableEntity))
        codes
      }
      deletePersonOperation.codes = Some(getEditPersonsCodes)
      deletePersonOperation
    }
    availableOperations :+= getDeletePersonOperation

    availableOperations
  }
}
