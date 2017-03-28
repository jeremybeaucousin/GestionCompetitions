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

trait PersonRepo {
  def find(sort: Option[Seq[String]], fields: Option[Seq[String]], offset: Option[Int], limit: Option[Int])(implicit ec: ExecutionContext): Future[List[Person]]

  def select(id: String)(implicit ec: ExecutionContext): Future[Option[Person]]

  def update(id: String, person: Person)(implicit ec: ExecutionContext): Future[WriteResult]

  def remove(id: String)(implicit ec: ExecutionContext): Future[WriteResult]

  def save(person: Person)(implicit ec: ExecutionContext): Future[WriteResult]
}

class PersonRepoImpl @Inject() (val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext)
    extends PersonRepo with MongoController with ReactiveMongoComponents {

  def collection = reactiveMongoApi.database.
    map(_.collection[BSONCollection]("persons"))

  override def find(sort: Option[Seq[String]], fields: Option[Seq[String]], offset: Option[Int], limit: Option[Int])(implicit ec: ExecutionContext): Future[List[Person]] = {
    val sortBson = MongoDbUtil.createSortBson(sort)
    val projectionBson = MongoDbUtil.createProjectionBson(fields)
    val cursor = collection.map(_.find(Json.obj(), projectionBson).options(QueryOpts(skipN = offset.getOrElse(0))).sort(sortBson).cursor[Person]())
    cursor.flatMap(_.collect[List](limit.getOrElse(0)))
  }

  override def select(id: String)(implicit ec: ExecutionContext): Future[Option[Person]] = {
    collection.flatMap(_.find(constructId(id)).one[Person])
  }

  override def update(id: String, person: Person)(implicit ec: ExecutionContext): Future[WriteResult] = {
    person._id = Some(id)
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