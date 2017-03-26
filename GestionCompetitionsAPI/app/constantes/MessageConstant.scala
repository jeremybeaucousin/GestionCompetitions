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

object Documentation {
  private final val PREFIX = "documentation.";
  private final val LIST_PERSONS_DESCRIPTION = "listPersonsDescription";
  private final val LIST_PERSONS_RETURN = "listPersonsReturn";
  private final val GET_PERSON_DESCRIPTION = "getPersonDescription";
  private final val GET_PERSON_ID_PARAMETER_DESCRIPTION = "getPersonIdParameterDescription";
  private final val GET_PERSON_RETURN = "getPersonReturn";
  private final val ADD_PERSON_DESCRIPTION = "addPersonDescription";
  private final val EDIT_PERSON_DESCRIPTION = "editPersonDescription";
  private final val DELETE_PERSON_DESCRIPTION = "deletePersonDescription";
  
  def listPersonsDescription:String = PREFIX + LIST_PERSONS_DESCRIPTION;
  def listPersonsReturn:String = PREFIX + LIST_PERSONS_RETURN;
  def getPersonDescription:String = PREFIX + GET_PERSON_DESCRIPTION;
  def getPersonIdParameterDescription:String = PREFIX + GET_PERSON_ID_PARAMETER_DESCRIPTION;
  def getPersonReturn:String = PREFIX + GET_PERSON_RETURN;
  def addPersonDescription:String = PREFIX + ADD_PERSON_DESCRIPTION;
  def editPersonDescription:String = PREFIX + EDIT_PERSON_DESCRIPTION;
  def deletePersonDescription:String = PREFIX + DELETE_PERSON_DESCRIPTION;
}

object MessageConstant {
  val title = Title
  val error = Error
  val database = Database
  val template = Template
  val documentation = Documentation
  
}