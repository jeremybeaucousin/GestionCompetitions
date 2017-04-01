package v1.constantes

final object QueryFields {
  final val sort: String = "sort"
  final val fields: String = "fields"
  final val offset: String = "offset"
  final val limit: String = "limit"
}

final object HeaderFields {
  final val HEADER_API_KEY = "X-Api-Key"
  final val HEADER_AUTH_TOKEN = "X-Auth-Token"
  final val location: String = "Location"
  final val xTotalCount: String = "X-Total-Count"
  final val link: String = "link"
}

final object HttpConstants {
  final val FIRST: String = "first"
  final val PREV: String = "prev"
  final val NEXT: String = "next"
  final val LAST: String = "last"
  final val HTML_LT: String = "<"
  final val HTML_GT: String = ">"

  final val queryFields = QueryFields
  final val headerFields = HeaderFields
}