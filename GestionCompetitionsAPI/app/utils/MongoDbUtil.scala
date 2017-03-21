package utils

import play.api.libs.json.{ JsObject, Json }
import play.api.Logger
import play.modules.reactivemongo.json._
import reactivemongo.api.collections.bson
import reactivemongo.api.collections.bson.BSONCollection

import reactivemongo.bson.{ BSON, BSONDocument, BSONObjectID }

object MongoDbUtil {
  def generateId(): BSONObjectID = {
    BSONObjectID.generate
  }

  def constructBSONDocumentForPartialUpdate(document: BSONDocument): BSONDocument = {
    var newDocument = document.copy()
    document.elements.foreach(element => {
      val fieldName = element._1
      val fieldValue = element._2
      if (fieldValue.isInstanceOf[BSONDocument]) {
        newDocument = newDocument.remove(fieldName)
        val subObject: BSONDocument = fieldValue.asInstanceOf[BSONDocument]
        var newSubObject = subObject.copy()
        subObject.elements.foreach(field => {
          val subObjectFieldName = field._1
          val subObjectFieldValue = field._2
          if (subObjectFieldValue.isInstanceOf[BSONDocument]) {
            // TODO not tested yet
            newSubObject = newSubObject.remove(subObjectFieldName)
            newSubObject = constructBSONDocumentForPartialUpdate(subObjectFieldValue.asInstanceOf[BSONDocument])
          } else {
            val newFieldName = fieldName + "." + subObjectFieldName
            newSubObject = newSubObject.remove(subObjectFieldName)
            newSubObject = newSubObject.add(newFieldName -> subObjectFieldValue)
          }
        })
        newDocument = newDocument.add(newSubObject)
      }
    })
    Logger.info(BSONDocument.pretty(newDocument))
    newDocument
  }
}