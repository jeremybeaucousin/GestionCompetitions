package constantes

object MessageConstant {

  // Commons
  private def getMessageKey(prefix: String, value: String): String = {
    s"$prefix.$value";
  }

  // Titles
  private final val TITLE_PREFIX = "title";
  private final val TITLE_ERROR = "error";
  
  def getErrorTitleMessageKey: String = {
    getMessageKey(TITLE_PREFIX, TITLE_ERROR);
  }
  
  // Errors
  private final val ERROR_PREFIX = "error";
  private final val ERROR_SERVER = "server";
  private final val ERROR_CLIENT = "client";

  def getServerErrorMessageKey: String = {
    getMessageKey(ERROR_PREFIX, ERROR_SERVER);
  }

  def getClientErrorMessageKey: String = {
    getMessageKey(ERROR_PREFIX, ERROR_CLIENT);
  }

  // Database
  private final val DATABASE_PREFIX = "database";
  private final val DATABASE_INSERTED = "inserted";

  def getDatabaseInsertedMessageKey: String = {
    getMessageKey(DATABASE_PREFIX, DATABASE_INSERTED);
  }
}