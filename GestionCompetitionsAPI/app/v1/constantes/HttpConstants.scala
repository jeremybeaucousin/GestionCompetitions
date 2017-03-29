package v1.constantes


final object QueryFields {
  final val sort:String = "sort"
  final val fields:String = "fields"
  final val offset:String = "offset"
  final val limit:String = "limit"
}

final object HeaderFields {
  final val location: String = "Location"
  final val xTotalCount: String= "X-Total-Count"
  final val link: String= "link"
}

final object HttpConstants {
  final val queryFields = QueryFields  
  final val headerFields = HeaderFields
}
