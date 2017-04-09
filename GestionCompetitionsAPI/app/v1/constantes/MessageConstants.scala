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
    private final val PASSWORD: String = "password"
    private final val POSTALCODE: String = "postalCode"
    private final val EMAIL_ALREADY_REGISTERED: String = "emailAlreadyRegistered"
    private final val FIRST_NAME_AND_LAST_NAME_REQUIRED: String = "firstNameAndLastNameRequired"
    private final val LOGIN_CANNOT_BE_SET: String = "loginCannotBeSet"
    private final val EMAIL_PASSWORD_LOGIN_REQUIRED: String = "emailPasswordLoginRequired"
    private final val LOGIN_ALREADY_REGISTERED: String = "loginAlreadyRegistered"
    private final val EMAIL_CANNOT_BE_SET: String = "emailCannotBeSet"
    private final val PASSWORD_CANNOT_BE_SET: String = "passwordCannotBeSet"
    private final val PASSWORD_NOT_RECOGNIZED: String = "passwordNotRecognized"
    private final val NEW_PASSWORDS_DOES_NOT_MATCH: String = "newPasswordsDoesNotMatch"
    private final val TOKEN_HAS_EXPIRED: String = "tokenHasExpired"
    private final val ACCOUNT_ALREADY_CREATED: String = "accountAlreadyCreated"

    def server: String = PREFIX + SERVER
    def client: String = PREFIX + CLIENT
    def sameNamesHomonym: String = PREFIX + SAME_NAMES_HOMONYM
    def sameNamesAndbirthDateHomonym: String = PREFIX + SAME_NAMES_AND_BIRTH_HOMONYM
    def emailAlreadyRegistered: String = PREFIX + EMAIL_ALREADY_REGISTERED
    def password: String = PREFIX + PASSWORD
    def postalCode: String = PREFIX + POSTALCODE
    def firstNameAndLastNameRequired: String = PREFIX + FIRST_NAME_AND_LAST_NAME_REQUIRED
    def loginCannotBeSet: String = PREFIX + LOGIN_CANNOT_BE_SET
    def emailPasswordLoginRequired: String = PREFIX + EMAIL_PASSWORD_LOGIN_REQUIRED
    def loginAlreadyRegistered: String = PREFIX + LOGIN_ALREADY_REGISTERED
    def emailCannotBeSet: String = PREFIX + EMAIL_CANNOT_BE_SET
    def passwordCannotBeSet: String = PREFIX + PASSWORD_CANNOT_BE_SET
    def passwordNotRecognized: String = PREFIX + PASSWORD_NOT_RECOGNIZED
    def newPasswordsDoesNotMatch: String = PREFIX + NEW_PASSWORDS_DOES_NOT_MATCH
    def tokenHasExpired: String = PREFIX + TOKEN_HAS_EXPIRED
    def accountAlreadyCreated: String = PREFIX + ACCOUNT_ALREADY_CREATED
  }

  final object Http {
    private final val PREFIX = "http."
    private final val NOT_FOUND = "notFound"
    private final val NO_CONTENT = "noContent"
    private final val UNPROCESSABLE_ENTITY = "unprocessableEntity"
    private final val OK = "ok"
    private final val CREATED = "created"
    private final val FORBIDDEN = "forbidden"
    private final val CONFLICT = "conflict"

    def notFound: String = PREFIX + NOT_FOUND
    def noContent: String = PREFIX + NO_CONTENT
    def unprocessableEntity: String = PREFIX + UNPROCESSABLE_ENTITY
    def ok: String = PREFIX + OK
    def created: String = PREFIX + CREATED
    def forbidden: String = PREFIX + FORBIDDEN
    def conflict: String = PREFIX + CONFLICT
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
    private final val MAIN_ERROR = "mainError"
    private final val SUB_ERRORS = "subErrors"

    def headers: String = PREFIX + HEADERS
    def request: String = PREFIX + REQUEST
    def parameters: String = PREFIX + PARAMETERS
    def body: String = PREFIX + BODY
    def empty: String = PREFIX + EMPTY
    def errors: String = PREFIX + ERRORS
    def response: String = PREFIX + RESPONSE
    def codes: String = PREFIX + CODES
    def mainError: String = PREFIX + MAIN_ERROR
    def subErrors: String = PREFIX + SUB_ERRORS
  }

  final object Documentation {
    private final val PREFIX = "documentation."

    object Common {
      private final val PREFIX = Documentation.PREFIX + "common."
      private final val SORT_DESCRIPTION = "sortDescription"
      private final val FIELDS_DESCRIPTION = "fieldsDescription"
      private final val OFFSET_DESCRIPTION = "offsetDescription"
      private final val LIMIT_DESCRIPTION = "limitDescription"
      private final val TOTAL_COUNT_DESCRIPTION = "totalCountDescription"
      private final val LINK_DESCRIPTION = "linkDescription"
      private final val LOCATION_DESCRIPTION = "locationDescription"
      private final val API_KEY_DESCRIPTION = "apiKeyDescription"
      private final val AUTH_TOKEN_DESCRIPTION = "authTokenDescription"

      def sortDescription: String = PREFIX + SORT_DESCRIPTION
      def fieldsDescription: String = PREFIX + FIELDS_DESCRIPTION
      def offsetDescription: String = PREFIX + OFFSET_DESCRIPTION
      def limitDescription: String = PREFIX + LIMIT_DESCRIPTION
      def totalCountDescription: String = PREFIX + TOTAL_COUNT_DESCRIPTION
      def linkDescription: String = PREFIX + LINK_DESCRIPTION
      def locationDescription: String = PREFIX + LOCATION_DESCRIPTION
      def apiKeyDescription: String = PREFIX + API_KEY_DESCRIPTION
      def authTokenDescription: String = PREFIX + AUTH_TOKEN_DESCRIPTION
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

      final object Address {
        private final val PREFIX = Person.PREFIX + "addresse."
        private final val LIST_ADDRESSES_DESCRIPTION = "listAddressesDescription"
        private final val ADD_ADDRESS_DESCRIPTION = "addAddressDescription"
        private final val GET_ADDRESS_DESCRIPTION = "getAddressDescription"
        private final val EDIT_ADDRESS_DESCRIPTION = "editAddressDescription"
        private final val DELETE_ADDRESS_DESCRIPTION = "deleteAddressDescription"

        def listAddressesDescription: String = PREFIX + LIST_ADDRESSES_DESCRIPTION
        def addAddressDescription: String = PREFIX + ADD_ADDRESS_DESCRIPTION
        def getAddressDescription: String = PREFIX + GET_ADDRESS_DESCRIPTION
        def editAddressDescription: String = PREFIX + EDIT_ADDRESS_DESCRIPTION
        def deleteAddressDescription: String = PREFIX + DELETE_ADDRESS_DESCRIPTION
      }
      final val address = Address
    }

    final object Authentication {
      private final val PREFIX = Documentation.PREFIX + "authentication."
      private final val SIGN_IN_DESCRIPTION = "signInDescription"
      private final val SIGN_OUT_DESCRIPTION = "signOutDescription"
      private final val SIGN_UP_DESCRIPTION = "signUpDescription"
      private final val RESET_PASSWORD = "resetPassword"
      private final val VALIDATE_ACCOUNT = "validateAccount"
      private final val VALIDATE_ACCOUNT_EMAIL_TOKEN_PARAMETER_DESCRIPTION = "validateAccountEmailTokenParameterDescription"
      private final val CHANGE_PASSWORD = "changePassword"

      def signInDescription: String = PREFIX + SIGN_IN_DESCRIPTION
      def signOutDescription: String = PREFIX + SIGN_OUT_DESCRIPTION
      def signUpDescription: String = PREFIX + SIGN_UP_DESCRIPTION
      def resetPassword: String = PREFIX + RESET_PASSWORD
      def validateAccount: String = PREFIX + VALIDATE_ACCOUNT
      def validateAccountEmailTokenParameterDescription: String = PREFIX + VALIDATE_ACCOUNT_EMAIL_TOKEN_PARAMETER_DESCRIPTION
      def changePassword: String = PREFIX + CHANGE_PASSWORD
    }

    final val common = Common
    final val person = Person
    final val authentication = Authentication

  }

  final val title = Title
  final val error = Error
  final val http = Http
  final val database = Database
  final val template = Template
  final val documentation = Documentation

}