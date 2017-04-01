package v1.constantes

final object MessageConstants {
  final object Title {
    private final val PREFIX: String = "title."
    private final val ERROR: String = "error"
    private final val DOCUMENTATION: String = "documentation"

    def error: String = PREFIX + ERROR
    def documentation: String = PREFIX + DOCUMENTATION
  }

  final object Error {
    private final val PREFIX = "error."
    private final val SERVER = "server"
    private final val CLIENT = "client"
    private final val SAME_NAMES_HOMONYM: String = "sameNamesHomonym"
    private final val SAME_NAMES_AND_BIRTH_HOMONYM: String = "sameNamesAndbirthDateHomonym"

    def server: String = PREFIX + SERVER
    def client: String = PREFIX + CLIENT
    def sameNamesHomonym: String = PREFIX + SAME_NAMES_HOMONYM
    def sameNamesAndbirthDateHomonym: String = PREFIX + SAME_NAMES_AND_BIRTH_HOMONYM

  }

  final object Http {
    private final val PREFIX = "http."
    private final val NOT_FOUND = "notFound"
    private final val NO_CONTENT = "noContent"
    private final val UNPROCESSABLE_ENTITY = "unprocessableEntity"
    private final val OK = "ok"
    private final val CREATED = "created"

    def notFound: String = PREFIX + NOT_FOUND
    def noContent: String = PREFIX + NO_CONTENT
    def unprocessableEntity: String = PREFIX + UNPROCESSABLE_ENTITY
    def ok: String = PREFIX + OK
    def created: String = PREFIX + CREATED
  }

  final object Database {
    private final val PREFIX = "database."
    private final val INSERTED = "inserted"

    def inserted: String = PREFIX + INSERTED
  }

  final object Template {
    private final val PREFIX = "template."
    private final val HEADERS = "headers"
    private final val REQUEST = "request"
    private final val PARAMETERS = "parameters"
    private final val BODY = "body"
    private final val EMPTY = "empty"
    private final val ERRORS = "errors"
    private final val RESPONSE = "response"
    private final val CODES = "codes"

    def headers: String = PREFIX + HEADERS
    def request: String = PREFIX + REQUEST
    def parameters: String = PREFIX + PARAMETERS
    def body: String = PREFIX + BODY
    def empty: String = PREFIX + EMPTY
    def errors: String = PREFIX + ERRORS
    def response: String = PREFIX + RESPONSE
    def codes: String = PREFIX + CODES
  }

  final object Documentation {
    private final val PREFIX = "documentation."

    object Common {
      private final val PREFIX = Documentation.PREFIX + "common."
      private final val SORT_DESCRIPTION = "sortDescription"
      private final val FIELDS_DESCRIPTION = "fieldsDescription"
      private final val OFFSET_DESCRIPTION = "offsetDescription"
      private final val LIMIT_DESCRIPTION = "limitDescription"
      private final val X_TOTAL_COUNT_DESCRIPTION = "xTotalCountDescription"
      private final val LINK_DESCRIPTION = "linkDescription"
      private final val LOCATION_DESCRIPTION = "locationDescription"

      def sortDescription: String = PREFIX + SORT_DESCRIPTION
      def fieldsDescription: String = PREFIX + FIELDS_DESCRIPTION
      def offsetDescription: String = PREFIX + OFFSET_DESCRIPTION
      def limitDescription: String = PREFIX + LIMIT_DESCRIPTION
      def xTotalCountDescription: String = PREFIX + X_TOTAL_COUNT_DESCRIPTION
      def linkDescription: String = PREFIX + LINK_DESCRIPTION
      def locationDescription: String = PREFIX + LOCATION_DESCRIPTION

    }

    final object Person {
      private final val PREFIX = Documentation.PREFIX + "person."
      private final val LIST_PERSONS_DESCRIPTION = "listPersonsDescription"
      private final val SEARCH_PERSONS_DESCRIPTION = "searchPersonsDescription"
      private final val GET_PERSON_DESCRIPTION = "getPersonDescription"
      private final val GET_PERSON_ID_PARAMETER_DESCRIPTION = "getPersonIdParameterDescription"
      private final val ADD_PERSON_DESCRIPTION = "addPersonDescription"
      private final val EDIT_PERSON_DESCRIPTION = "editPersonDescription"
      private final val DELETE_PERSON_DESCRIPTION = "deletePersonDescription"

      def listPersonsDescription: String = PREFIX + LIST_PERSONS_DESCRIPTION
      def searchPersonsDescription: String = PREFIX + SEARCH_PERSONS_DESCRIPTION
      def getPersonDescription: String = PREFIX + GET_PERSON_DESCRIPTION
      def getPersonIdParameterDescription: String = PREFIX + GET_PERSON_ID_PARAMETER_DESCRIPTION
      def addPersonDescription: String = PREFIX + ADD_PERSON_DESCRIPTION
      def editPersonDescription: String = PREFIX + EDIT_PERSON_DESCRIPTION
      def deletePersonDescription: String = PREFIX + DELETE_PERSON_DESCRIPTION
      searchPersonsDescription
    }

    final val common = Common
    final val person = Person

  }

  final val title = Title
  final val error = Error
  final val http = Http
  final val database = Database
  final val template = Template
  final val documentation = Documentation

}