package models

import play.api.Play.current
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders._
import mongoContext._
import com.mongodb.casbah.commons.MongoDBObject

case class MetaInformation(key: String, value: String)

object MetaInformation extends ModelCompanion[MetaInformation, ObjectId] {
  def collection = mongoCollection("metaInformation")
  val dao = new SalatDAO[MetaInformation, ObjectId](collection) {}

  private def internalFindByKey(key: String) = dao.findOne(MongoDBObject("key" -> key))
  
  def findByKey(key: String) = {
    internalFindByKey(key).map(_.value)
  }

  def insertOrUpdate(key: String, value: String) = {
    internalFindByKey(key).map(old => {
      val newVal = MetaInformation(key, value)
      dao.update(dao.toDBObject(old), dao.toDBObject(newVal), true, false)
    }).getOrElse(dao.insert(MetaInformation(key, value)))
    
  }

}