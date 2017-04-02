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
import org.apache.commons.lang3.StringUtils
import java.util.Date
import models.ApiToken
import v1.bo.Address
import play.Logger

@Singleton
class DocumentationManager @Inject() (
    implicit val ec: ExecutionContext) {

  final val personCompleteExemple = new Person(
    Some(StringUtils.EMPTY),
    Some(StringUtils.EMPTY),
    Some(StringUtils.EMPTY),
    Some(new Date),
    Some(StringUtils.EMPTY),
    Some(StringUtils.EMPTY),
    Some(StringUtils.EMPTY),
    Some(List[Address](new Address, new Address)))

  final val jsonPersonCompleteExemple = (Json.toJson(personCompleteExemple))

  final val personWithRootFieldsExemple = new Person(
    Some(StringUtils.EMPTY),
    Some(StringUtils.EMPTY),
    Some(StringUtils.EMPTY),
    Some(new Date),
    Some(StringUtils.EMPTY),
    None,
    Some(StringUtils.EMPTY),
    None)
  final val jsonPersonWithRootFieldsExemple = (Json.toJson(personWithRootFieldsExemple))
  final val jsonPersonArrayExemple = (Json.toJson(List[Person](personCompleteExemple, personCompleteExemple)))
  final val _idExemple = MongoDbUtil.generateId().stringify
  final val sortExemple = Seq[String]("+" + Person.FIRST_NAME, "-" + Person.LAST_NAME)
  final val fieldsExemple = Seq[String](Person._ID, Person.FIRST_NAME, Person.LAST_NAME)

  def getPersonOperations(implicit messages: Messages): Seq[Operation] = {
    var availableOperations: Seq[Operation] = Seq[Operation]()

    def getSortDescription: (String, String) = {
      (HttpConstants.queryFields.sort -> messages(MessageConstants.documentation.common.sortDescription))
    }

    def getFieldsDescription: (String, String) = {
      (HttpConstants.queryFields.fields -> messages(MessageConstants.documentation.common.fieldsDescription))
    }

    def getOffsetDescription: (String, String) = {
      (HttpConstants.queryFields.offset -> messages(MessageConstants.documentation.common.offsetDescription))
    }

    def getLimitDescription: (String, String) = {
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
        parameters += (HttpConstants.headerFields.xTotalCount -> messages(MessageConstants.documentation.common.totalCountDescription))
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

      def getSearchPersonsRequestParameters: Map[String, String] = {
        var parameters: Map[String, String] = Map[String, String]()
        parameters += getSortDescription
        parameters += getFieldsDescription
        parameters += getOffsetDescription
        parameters += getLimitDescription
        parameters
      }

      val searchPersonsRequest = RequestContents()
      searchPersonsRequest.body = Some(jsonPersonCompleteExemple)
      searchPersonsRequest.parameters = Some(getSearchPersonsRequestParameters)
      searchPersonsOperation.request = Some(searchPersonsRequest)

      def getSearchPersonsHeadersParameters: Map[String, String] = {
        var parameters: Map[String, String] = Map[String, String]()
        parameters += (HttpConstants.headerFields.xTotalCount -> messages(MessageConstants.documentation.common.totalCountDescription))
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

      def getGetPersonRequestParameters: Map[String, String] = {
        var parameters: Map[String, String] = Map[String, String]()
        parameters += (Person._ID -> messages(MessageConstants.documentation.person.getPersonIdParameterDescription))
        parameters += getFieldsDescription
        parameters
      }

      val getPersonRequest = RequestContents()
      getPersonRequest.parameters = Some(getGetPersonRequestParameters)
      getPersonOperation.request = Some(getPersonRequest)

      val getPersonsResponse = RequestContents()
      getPersonsResponse.body = Some(jsonPersonCompleteExemple)
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
      getPersonRequest.body = Some(jsonPersonCompleteExemple)
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

      def getEditPersonRequestParameters: Map[String, String] = {
        var parameters: Map[String, String] = Map[String, String]()
        parameters += (Person._ID -> messages(MessageConstants.documentation.person.getPersonIdParameterDescription))
        parameters
      }

      val editPersonRequest = RequestContents()
      editPersonRequest.body = Some(jsonPersonWithRootFieldsExemple)
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

      def getDeletePersonRequestParameters: Map[String, String] = {
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

  def getAuthenticationOperations(implicit messages: Messages): Seq[Operation] = {
    var availableOperations: Seq[Operation] = Seq[Operation]()

    def getSignInOperation = {
      val signInOperation = Operation()
      signInOperation.call = Some(routes.AuthenticationController.signin())
      signInOperation.description = Some(messages(MessageConstants.documentation.authentication.signInDescription))

      def getSignInParameters: Map[String, String] = {
        var parameters: Map[String, String] = Map[String, String]()
        parameters += (HttpConstants.headerFields.apiKey -> messages(MessageConstants.documentation.common.apiKeyDescription))
        parameters
      }

      val signInpersonJson = Json.obj(
        Person.EMAIL -> StringUtils.EMPTY,
        Person.PASSWORD -> StringUtils.EMPTY)

      val signInRequest = RequestContents()
      signInRequest.headers = Some(getSignInParameters)
      signInRequest.body = Some(signInpersonJson)
      signInOperation.request = Some(signInRequest)

      val jsonResponse = Json.obj(
        ApiToken.TOKEN_FIELD -> StringUtils.EMPTY,
        ApiToken.DURATION_FIELD -> 0)

      val signInResponse = RequestContents()
      signInResponse.body = Some(jsonResponse)
      signInOperation.response = Some(signInResponse)

      def signInPersonsCodes: Map[String, String] = {
        var codes: Map[String, String] = Map[String, String]()
        codes += (Http.Status.OK.toString() -> messages(MessageConstants.http.ok))
        codes += (Http.Status.UNPROCESSABLE_ENTITY.toString() -> messages(MessageConstants.http.unprocessableEntity))
        codes += (Http.Status.FORBIDDEN.toString() -> messages(MessageConstants.http.forbidden))
        codes
      }

      signInOperation.codes = Some(signInPersonsCodes)
      signInOperation
    }
    availableOperations :+= getSignInOperation

    def getSignOutOperation = {
      val signOutOperation = Operation()
      signOutOperation.call = Some(routes.AuthenticationController.signout())
      signOutOperation.description = Some(messages(MessageConstants.documentation.authentication.signOutDescription))

      def getSignOutParameters: Map[String, String] = {
        var parameters: Map[String, String] = Map[String, String]()
        parameters += (HttpConstants.headerFields.apiKey -> messages(MessageConstants.documentation.common.apiKeyDescription))
        parameters += (HttpConstants.headerFields.authToken -> messages(MessageConstants.documentation.common.authTokenDescription))
        parameters
      }

      val signOutRequest = RequestContents()
      signOutRequest.headers = Some(getSignOutParameters)
      signOutOperation.request = Some(signOutRequest)

      val signOutResponse = RequestContents()
      signOutOperation.response = Some(signOutResponse)

      def signOutPersonsCodes: Map[String, String] = {
        var codes: Map[String, String] = Map[String, String]()
        codes += (Http.Status.FORBIDDEN.toString() -> messages(MessageConstants.http.forbidden))
        codes += (Http.Status.NO_CONTENT.toString() -> messages(MessageConstants.http.noContent))
        codes
      }

      signOutOperation.codes = Some(signOutPersonsCodes)
      signOutOperation
    }
    availableOperations :+= getSignOutOperation

    availableOperations
  }
}
