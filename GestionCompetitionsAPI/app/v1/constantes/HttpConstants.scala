package v1.constantes

final object HttpConstants {
  final object QueryFields {
    final val sort: String = "sort"
    final val fields: String = "fields"
    final val offset: String = "offset"
    final val limit: String = "limit"
  }

  final object HeaderFields {
    final val apiKey = "X-Api-Key"
    final val authToken = "X-Auth-Token"
    final val location: String = "Location"
    final val xTotalCount: String = "X-Total-Count"
    final val link: String = "link"
  }

  final val FIRST: String = "first"
  final val PREV: String = "prev"
  final val NEXT: String = "next"
  final val LAST: String = "last"
  final val HTML_LT: String = "<"
  final val HTML_GT: String = ">"

  final val queryFields = QueryFields
  final val headerFields = HeaderFields
}