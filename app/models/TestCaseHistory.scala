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
import org.joda.time.DateTime
import play.libs.Json._

case class TestCaseHistory(
  buildNumber: Int,
  className: String,
  suiteName: String,
  testName: String,
  comment: String,
  timestamp: DateTime,
  additionalData: Map[String, String])

object TestCaseHistory extends ModelCompanion[TestCaseHistory, ObjectId] {
  def collection = mongoCollection("testCaseHistory")
  val dao = new SalatDAO[TestCaseHistory, ObjectId](collection) {}

  def getHistoryByTestCase(testCase: TestCase): List[TestCaseHistory] = {
    dao.find(MongoDBObject("testName" -> testCase.testName, "className" -> testCase.className, "suiteName" -> testCase.suiteName)).sort(orderBy = MongoDBObject("timestamp" -> -1)).toList
  }
}

