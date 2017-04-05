package v1.utils

import org.mindrot.jbcrypt.BCrypt
import scala.util.Random
import java.util.UUID

object SecurityUtil {
  def encryptString(clearString: String): String = {
    if (clearString == null) {
      throw new Exception("empty.password");
    }
    BCrypt.hashpw(clearString, BCrypt.gensalt());
  }

  def checkString(candidate: String, encryptedPassword: String): Boolean = {
    if (candidate == null) {
      false
    }
    if (encryptedPassword == null) {
      false
    }
    BCrypt.checkpw(candidate, encryptedPassword)
  }

  def generateUUID(): String = {
    UUID.randomUUID().toString
  }

  def generateString(length: Int): String = {
    val r = new scala.util.Random
    val sb = new StringBuilder
    for (i <- 1 to length) {
      sb.append(r.nextPrintableChar)
    }
    sb.toString
  }
}