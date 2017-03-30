package v1.dal.repos

import javax.inject.Inject
import v1.bo.Person
import play.api.libs.json.{ JsObject, Json }
import play.api.Logger
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.{ MongoController, ReactiveMongoApi, ReactiveMongoComponents }
import reactivemongo.api.collections.bson
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{ BSON, BSONDocument, BSONObjectID }

import scala.concurrent.{ ExecutionContext, Future }
import v1.utils.MongoDbUtil
import reactivemongo.bson.BSONWriter
import reactivemongo.api.QueryOpts
import reactivemongo.core.commands.Count
import v1.bo.Person.PersonReads
import v1.bo.Person.PersonWrites

trait PersonRepo {
  
  def getTotalCount(personOption: Option[Person], searchInValues: Option[Boolean])(implicit ec: ExecutionContext): Future[Int]
  
  def find(personOption: Option[Person], searchInValues: Option[Boolean], sortOption: Option[Seq[String]], fieldsOption: Option[Seq[String]], offsetOption: Option[Int], limitOption: Option[Int])(implicit ec: ExecutionContext): Future[List[Person]]

  def select(id: String, fieldsOption: Option[Seq[String]])(implicit ec: ExecutionContext): Future[Option[Person]]

  def update(id: String, person: Person)(implicit ec: ExecutionContext): Future[WriteResult]

  def remove(id: String)(implicit ec: ExecutionContext): Future[WriteResult]

  def save(person: Person)(implicit ec: ExecutionContext): Future[WriteResult]
}

class PersonRepoImpl @Inject() (val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext)
    extends PersonRepo with MongoController with ReactiveMongoComponents {

  def collection = reactiveMongoApi.database.
    map(_.collection[BSONCollection]("persons"))

  override def find(
      personOption: Option[Person],
      searchInValues: Option[Boolean],
      sortOption: Option[Seq[String]], 
      fieldsOption: Option[Seq[String]], 
      offsetOption: Option[Int], 
      limitOption: Option[Int])(implicit ec: ExecutionContext): Future[List[Person]] = {
    val personSearch:BSONDocument = if(personOption.isDefined) BSON.write(personOption.get) else BSONDocument() 
    val valuesSearch: BSONDocument = if(searchInValues.isDefined && searchInValues.get) MongoDbUtil.createSearchInValuesBson(personSearch) else personSearch
    val sortBson = MongoDbUtil.createSortBson(sortOption)
    val projectionBson = MongoDbUtil.createProjectionBson(fieldsOption)
    val query = collection.map(_.find(valuesSearch).projection(projectionBson))
    val cursor = query.map(_.options(QueryOpts(skipN = MongoDbUtil.getSafeOffset(offsetOption))).sort(sortBson).cursor[Person]())
    cursor.flatMap(_.collect[List](limitOption.getOrElse(0)))
  }

  def getTotalCount(personOption: Option[Person], searchInValues: Option[Boolean])(implicit ec: ExecutionContext):Future[Int] = {
    val personSearch:BSONDocument = if(personOption.isDefined) BSON.write(personOption.get) else BSONDocument() 
    val valuesSearch: BSONDocument = if(searchInValues.isDefined && searchInValues.get) MongoDbUtil.createSearchInValuesBson(personSearch) else personSearch
    collection.flatMap(_.count(Some(valuesSearch)))
  }

  override def select(id: String, fieldsOption: Option[Seq[String]])(implicit ec: ExecutionContext): Future[Option[Person]] = {
    val projectionBson = MongoDbUtil.createProjectionBson(fieldsOption)
    collection.flatMap(_.find(constructId(id)).projection(projectionBson).one[Person])
  }

  override def update(id: String, person: Person)(implicit ec: ExecutionContext): Future[WriteResult] = {
    val rebuildDocument = MongoDbUtil.constructBSONDocumentForPartialUpdate(BSON.write(person))
    collection.flatMap(_.update(constructId(id), BSONDocument("$set" -> rebuildDocument)))
  }

  override def remove(id: String)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.flatMap(_.remove(constructId(id)))
  }

  override def save(person: Person)(implicit ec: ExecutionContext): Future[WriteResult] = {
    person._id = Some(MongoDbUtil.generateId().stringify)
    collection.flatMap(_.insert(person))
  }

  private def constructId(id: String): BSONDocument = {
    BSONDocument("_id" -> id)
  }
}