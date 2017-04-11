package v1.constantes

final object ValidationConstants {
  final object Regex {
    // TODO Ckeck other password
    final val PASSWORD = """[a-zA-Z0-9@*$&~#;:,`"\.<>_\^\-\+\=\\/\|{}\[\]\(\)\*]{8,15}""".r
    final val PHONE_NUMBER = """^0[1-6]{1}(([0-9]{2}){4})|((\s[0-9]{2}){4})|((-[0-9]{2}){4})$""".r
    final val POSTAL_CODE = """[\d]{5}""".r
  }

  final val regex = Regex
}