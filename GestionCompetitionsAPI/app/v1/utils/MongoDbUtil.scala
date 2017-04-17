package v1.utils

import play.api.libs.json.{ JsObject, Json }
import play.api.Logger
import play.modules.reactivemongo.json._
import reactivemongo.api.collections.bson
import reactivemongo.api.collections.bson.BSONCollection

import reactivemongo.bson.{ BSON, BSONDocument, BSONObjectID }
import reactivemongo.bson.BSONArray
import reactivemongo.bson.BSONString
import reactivemongo.bson.BSONRegex
import reactivemongo.bson.BSONValue
import org.apache.commons.lang3.StringUtils

object MongoDbUtil {
  final val _ID = "_id"
  final val INDEX = "index"
  final val PERSONS_COLLECTION = "persons"

  def generateId(): BSONObjectID = {
    BSONObjectID.generate
  }

  def createSearchInValuesBson(searchValues: BSONDocument): BSONDocument = {
    var searchInValues = BSONDocument()
    // Browse fields of the document
    searchValues.elements.foreach(element => {
      val fieldName = element._1
      val fieldValue = element._2
      // if the value of the current field is an object we begin rebuild
      if (!fieldValue.isInstanceOf[BSONDocument] || !fieldValue.isInstanceOf[BSONArray]) {
        if (fieldValue.isInstanceOf[BSONString]) {
          searchInValues ++= (fieldName -> BSONDocument("$regex" -> BSONRegex(fieldValue.asInstanceOf[BSONString].value, "")))
        } else {
          searchInValues ++= (fieldName -> fieldValue)
        }
      }
    })
    searchInValues
  }

  def createSortBson(fields: Option[Seq[String]]): BSONDocument = {
    var sortingBson = BSONDocument()
    val regex = """(\-|\+)([\w]+)""".r
    if (fields.isDefined && !fields.get.isEmpty) {
      fields.get.map(field => {
        if (regex.pattern.matcher(field).matches) {
          field match {
            case regex(order, field) => {
              val mongoOrder = if (order == "-") -1 else 1
              sortingBson ++= (field -> mongoOrder)
            }
          }
        }
      })
    }
    sortingBson
  }

  def createProjectionBson(fields: Option[Seq[String]]): BSONDocument = {
    var projectionBson = BSONDocument()
    val regex = """([\w]+)""".r
    if (fields.isDefined) {
      // the fields can be filled with an empty string "" when fields is empty (ex : "fields=")
      if (!fields.get.isEmpty && !fields.get(0).isEmpty())
        projectionBson ++= (_ID -> 0)
      fields.get.map(field => {
        if (regex.pattern.matcher(field).matches) {
          field match {
            case regex(field) => {
              projectionBson ++= (field -> 1)
            }
          }
        }
      })
    }
    projectionBson
  }

  def getSafeOffset(optionOffset: Option[Int]): Int = {
    if (!optionOffset.isDefined || (optionOffset.isDefined && optionOffset.get < 0)) {
      0
    } else {
      optionOffset.get
    }
  }

  // TODO Optimise with constructBSONDocumentWithForUnset
  def constructBSONDocumentWithForUnset(fields: List[String]): BSONDocument = {
    var bsonObject = BSONDocument()
    fields.foreach(field =>{
      bsonObject  ++= (field -> StringUtils.EMPTY)
    })
    bsonObject
  }
  
  def constructBSONDocumentWithRootFields(document: BSONDocument): BSONDocument = {
    var newDocument = document.copy()
    // Browse fields of the document
    document.elements.foreach(element => {
      val fieldName = element._1
      val fieldValue = element._2
      // if the value of the current field is an object we begin rebuild
      if (fieldValue.isInstanceOf[BSONDocument] || fieldValue.isInstanceOf[BSONArray]) {
        newDocument = newDocument.remove(fieldName)
      } else if (fieldValue.isInstanceOf[BSONString]) {
        val stringValue = fieldValue.asInstanceOf[BSONString]
        if (stringValue.value.isEmpty()) {
          newDocument = newDocument.remove(fieldName)
        }
      }
      // Handleing of subdocument
      //      if (fieldValue.isInstanceOf[BSONDocument]) {
      //        newDocument = newDocument.remove(fieldName)
      //        val subObject: BSONDocument = fieldValue.asInstanceOf[BSONDocument]
      //        var newSubObject = subObject.copy()
      //        // Browse fields of the subObject
      //        subObject.elements.foreach(field => {
      //          val subObjectFieldName = field._1
      //          val subObjectFieldValue = field._2
      //          // if a value of the subObject is an object we call the method again for processing
      //          if (subObjectFieldValue.isInstanceOf[BSONDocument]) {
      //            // TODO not tested yet
      //            newSubObject = newSubObject.remove(subObjectFieldName)
      //            newSubObject = constructBSONDocumentForPartialUpdate(subObjectFieldValue.asInstanceOf[BSONDocument])
      //            // else we replace the sub object key with the document key as prefix 
      //            //(ex : document key : "Adress", sub object key: "Number", result :"Adress.Number")
      //          } else if (fieldValue.isInstanceOf[BSONArray]) {
      //            newDocument = newDocument.remove(fieldName)
      //            val subArray: BSONArray = fieldValue.asInstanceOf[BSONArray]
      //            var newArray = subArray.copy()
      //            // TODO Browse array            
      //          } else {
      //            val newFieldName = fieldName + "." + subObjectFieldName
      //            newSubObject = newSubObject.remove(subObjectFieldName)
      //            newSubObject = newSubObject.add(newFieldName -> subObjectFieldValue)
      //          }
      //        })
      //        newDocument = newDocument.add(newSubObject)
      //      }
    })
    newDocument
  }
}