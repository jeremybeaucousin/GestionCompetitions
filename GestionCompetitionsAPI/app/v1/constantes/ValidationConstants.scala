package v1.constantes

final object ValidationConstants {
  final object Regex {
    final val PASSWORD = """[a-zA-Z0-9@*#_-]{8,15}""".r
    final val POSTAL_CODE = """[\d]{5}""".r
  }

  final val regex = Regex
}