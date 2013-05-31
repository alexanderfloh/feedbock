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

case class TestCaseScore(
  @Key("_id") id: TestCaseKey,
  value: Int)

object TestCaseScore extends ModelCompanion[TestCaseScore, TestCaseKey] {
  def collection = mongoCollection("testCaseScores")
  val dao = new SalatDAO[TestCaseScore, TestCaseKey](collection) {}

  def all(): List[TestCaseScore] = dao.find(MongoDBObject.empty).toList
}