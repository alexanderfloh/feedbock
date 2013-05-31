package models

import org.joda.time.DateTime
import com.mongodb.casbah.Imports.MapReduceCommand
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.map_reduce.MapReduceStandardOutput
import com.novus.salat.dao.ModelCompanion
import com.novus.salat.dao.SalatDAO
import mongoContext.ctx
import play.api.Play.current
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.JsValue
import play.api.libs.json.Writes
import se.radley.plugin.salat.mongoCollection
import utils.MapReduceFunctionLoader
import com.novus.salat.annotations._

case class TestCaseConfiguration(
    @Key("name")
    name: String, 
    var passed: List[Int] = List(), 
    var failed: List[Int] = List())
    
    
object TestCaseConfiguration {
  implicit val testCaseConfigurationJsonWrite = new Writes[TestCaseConfiguration] {
    def writes(a: TestCaseConfiguration): JsValue = {
      Json.obj(
        "name" -> a.name,
        "passed" -> a.passed,
        "failed" -> a.failed
      )
    }
  }

  implicit val testCaseConfigurationJsonRead = (
    (__ \ 'name).read[String] ~
    (__ \ 'passed).read[List[Int]] ~
    (__ \ 'failed).read[List[Int]]
  )(TestCaseConfiguration.apply _)
}

case class TestCaseFeedback(
    user: String,
    build: Int,
    timestamp: DateTime,
    defect: Boolean,
    codeChange: Boolean,
    timingIssue: Boolean,
    comment: String    
    )

case class TestCase(
  @Key("_id") id: TestCaseKey,
  var configurations: List[TestCaseConfiguration] = List(),
  var feedback: List[TestCaseFeedback] = List(),
  score: Int = 10
  ) {

}

object TestCase extends TestCaseDAO

trait TestCaseDAO extends ModelCompanion[TestCase, TestCaseKey] {
  def collection = mongoCollection("testCases")
  val dao = new SalatDAO[TestCase, TestCaseKey](collection) {}

  // Indexes
  //collection.ensureIndex(DBObject("username" -> 1), "user_email", unique = true)

  // Queries
  def all(): List[TestCase] = dao.find(MongoDBObject.empty).toList
  def findByBuildNumber(buildNumber: Long): List[TestCase] = dao.find(MongoDBObject("buildNumber" -> buildNumber)).toList
  def findByStatus(status: String) = dao.find(MongoDBObject("status.name" -> status)).toList
  def findByBuildAndStatus(build: Int, status: String) = dao.find(MongoDBObject("buildNumber" -> build, "status.name" -> status))

  def countByStatus() = {
    val countByStatusFunctions = MapReduceFunctionLoader("feedbockJS", "countByStatus")
    val mrc = MapReduceCommand(
      input = "testCases",
      map = countByStatusFunctions.map,
      reduce = countByStatusFunctions.reduce,
      finalizeFunction = None,
      verbose = true,
      output = MapReduceStandardOutput("buildHistory"))
    collection.mapReduce(mrc).toList
  }
}
