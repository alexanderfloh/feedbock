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

case class BuildHistory(
  @Key("_id") buildNumber: Int,
  value: Map[String, Int])

object BuildHistory extends ModelCompanion[BuildHistory, ObjectId] {
  def collection = mongoCollection("buildHistory")
  val dao = new SalatDAO[BuildHistory, ObjectId](collection) {}

  def all(): List[BuildHistory] = dao.find(MongoDBObject.empty).toList
}

