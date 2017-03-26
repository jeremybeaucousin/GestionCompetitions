package constantes

object Title {
  private final val PREFIX:String = "title.";
  private final val ERROR:String = "error";
  private final val DOCUMENTATION:String = "documentation";
  
  def error:String = PREFIX + ERROR;
  def documentation:String = PREFIX + DOCUMENTATION;
}


object Error {
  private final val PREFIX = "error.";
  private final val SERVER = "server";
  private final val CLIENT = "client";
  
  def server:String = PREFIX + SERVER;
  def client:String = PREFIX + CLIENT;
}

object Database {
  private final val PREFIX = "database.";
  private final val INSERTED = "inserted";
  
  def inserted:String = PREFIX + INSERTED;
}

object Template {
  private final val PREFIX = "template.";
  private final val REQUEST = "request";
  private final val PARAMETERS = "parameters";
  private final val BODY = "body";
  private final val EMPTY = "empty";
  private final val ERRORS = "errors";
  private final val RESPONSE = "response";
  
  def request:String = PREFIX + REQUEST;
  def parameters:String = PREFIX + PARAMETERS;
  def body:String = PREFIX + BODY;
  def empty:String = PREFIX + EMPTY;
  def errors:String = PREFIX + ERRORS;
  def response:String = PREFIX + RESPONSE;
}

object MessageConstant {
  val title = Title
  val error = Error
  val database = Database
  val template = Template
  
}