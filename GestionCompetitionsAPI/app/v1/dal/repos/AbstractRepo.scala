package v1.dal.repos

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import reactivemongo.api.QueryOpts
import reactivemongo.api.ReadPreference
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSON
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter
import v1.utils.MongoDbUtil
import play.Logger

trait AbstractRepo[T] {

  def getTotalCount(personOption: Option[T], searchInValues: Option[Boolean]): Future[Int]

  def find(personOption: Option[T], searchInValues: Option[Boolean], sortOption: Option[Seq[String]], fieldsOption: Option[Seq[String]], offsetOption: Option[Int], limitOption: Option[Int])(implicit ec: ExecutionContext): Future[List[T]]

  def select(id: String, fieldsOption: Option[Seq[String]]): Future[Option[T]]

  def deleteFields(id: String, fields: List[String]): Future[Boolean]

  /**
   * Update the person and send back the insciption result.
   * @param id
   * @param person
   * @return
   */
  def update(id: String, person: T): Future[Boolean]

  /**
   * Delete the person and send back the insciption result.
   * @param id
   * @param person
   * @return
   */
  def remove(id: String): Future[Boolean]

  /**
   * Send back the inserted person if no error
   * @param person
   * @param bSONDocumentReader
   * @param bSONDocumentWriter
   * @return
   */
  def save(_id: String, person: T): Future[Boolean]
}

class AbstractRepoImpl[T] (val collection : Future[BSONCollection]) (
    implicit ec: ExecutionContext,
    bSONDocumentWriter :BSONDocumentWriter[T], 
    bSONDocumentReader: BSONDocumentReader[T])
    extends AbstractRepo[T] {


  override def find(
    personOption: Option[T],
    searchInValues: Option[Boolean],
    sortOption: Option[Seq[String]],
    fieldsOption: Option[Seq[String]],
    offsetOption: Option[Int],
    limitOption: Option[Int])(implicit ec: ExecutionContext): Future[List[T]] = {
    val personSearch: BSONDocument = if (personOption.isDefined) MongoDbUtil.constructBSONDocumentWithRootFields(BSON.write(personOption.get)) else BSONDocument()
    val valuesSearch: BSONDocument = if (searchInValues.isDefined && searchInValues.get) MongoDbUtil.createSearchInValuesBson(personSearch) else personSearch
    
    val sortBson = MongoDbUtil.createSortBson(sortOption)
    val projectionBson = MongoDbUtil.createProjectionBson(fieldsOption)
    Logger.info(BSONDocument.pretty(projectionBson))
    val query = collection.map(_.find(valuesSearch).projection(projectionBson))
    val cursor = query.map(_.options(QueryOpts(skipN = MongoDbUtil.getSafeOffset(offsetOption))).sort(sortBson).cursor[T](ReadPreference.secondaryPreferred))
    cursor.flatMap(_.collect[List](limitOption.getOrElse(0)))
  }

  def getTotalCount(personOption: Option[T], searchInValues: Option[Boolean]): Future[Int] = {
    val personSearch: BSONDocument = if (personOption.isDefined) BSON.write(personOption.get) else BSONDocument()
    val valuesSearch: BSONDocument = if (searchInValues.isDefined && searchInValues.get) MongoDbUtil.createSearchInValuesBson(personSearch) else personSearch
    collection.flatMap(_.count(Some(valuesSearch)))
  }

  override def select(id: String, fieldsOption: Option[Seq[String]]): Future[Option[T]] = {
    val projectionBson = MongoDbUtil.createProjectionBson(fieldsOption)
    // Cannot set read preferences on creation
    collection.flatMap(_.find(constructId(id)).projection(projectionBson).one[T])
  }

  override def deleteFields(id: String, fields: List[String]): Future[Boolean] = {
    if (!fields.isEmpty) {
      val rebuildForUnset = MongoDbUtil.constructBSONDocumentWithForUnset(fields)
      val futureWriteResult = collection.flatMap(_.update(constructId(id), BSONDocument("$unset" -> rebuildForUnset)))
      handleWriteResult(futureWriteResult)
    } else {
      Future(false)
    }
  }

  override def update(id: String, document: T): Future[Boolean] = {
    val rebuildDocument = MongoDbUtil.constructBSONDocumentWithRootFields(BSON.write(document))
    val futureWriteResult = collection.flatMap(_.update(constructId(id), BSONDocument("$set" -> rebuildDocument)))
    handleWriteResult(futureWriteResult)
  }

  override def remove(id: String): Future[Boolean] = {
    val futureWriteResult = collection.flatMap(_.remove(constructId(id)))
    handleWriteResult(futureWriteResult)

  }

  override def save(_id: String, document: T): Future[Boolean] = {
    var bsonDocumentWithId = BSON.write(document)
    bsonDocumentWithId = bsonDocumentWithId.remove(MongoDbUtil._ID)
    bsonDocumentWithId ++= (MongoDbUtil._ID -> _id)
    val futureWriteResult = collection.flatMap(_.insert(bsonDocumentWithId))
    handleWriteResult(futureWriteResult)
  }

  private def constructId(id: String): BSONDocument = {
    BSONDocument("_id" -> id)
  }

  def handleWriteResult(futureWriteResult: Future[WriteResult]): Future[Boolean] = {
    futureWriteResult.map(writeResult => {
      !writeResult.hasErrors && writeResult.n > 0
    })
  }
}