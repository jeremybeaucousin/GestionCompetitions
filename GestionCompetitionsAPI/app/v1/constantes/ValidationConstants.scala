package v1.constantes

final object ValidationConstants {
  final object Regex {
    final val PASSWORD = """[a-zA-Z0-9@*#]{8,15}""".r
    final val POSTAL_CODE = """[\d]{6}""".r
  }

  final val regex = Regex
}