package v1.utils

import org.mindrot.jbcrypt.BCrypt

object SecurityUtil {
  def createPassword(clearString: String): String = {
    if (clearString == null) {
      throw new Exception("empty.password");
    }
    BCrypt.hashpw(clearString, BCrypt.gensalt());
  }

  def checkPassword(candidate: String, encryptedPassword: String): Boolean = {
    if (candidate == null) {
      false
    }
    if (encryptedPassword == null) {
      false
    }
    BCrypt.checkpw(candidate, encryptedPassword);
  }
}