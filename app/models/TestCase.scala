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
import utils.MapReduceFunctionLoader
import com.mongodb.casbah.map_reduce.MapReduceStandardOutput

case class TestCase(
  id: ObjectId,
  buildNumber: Long,
  testName: String,
  className: String,
  suiteName: String,
  configurationName: String,
  status: TestStatus) {
  
  def testCaseKey = TestCaseKey(suiteName, className, testName)
}

object TestCase extends TestCaseDAO with TestCaseJson

trait TestCaseDAO extends ModelCompanion[TestCase, ObjectId] {
  def collection = mongoCollection("testCases")
  val dao = new SalatDAO[TestCase, ObjectId](collection) {}

  // Indexes
  //collection.ensureIndex(DBObject("username" -> 1), "user_email", unique = true)

  // Queries
  def all(): List[TestCase] = dao.find(MongoDBObject.empty).toList
  def findByBuildNumber(buildNumber: Long): List[TestCase] = dao.find(MongoDBObject("buildNumber" -> buildNumber)).toList
  def findByStatus(status: String) = dao.find(MongoDBObject("status.name" -> status)).toList
  def findByBuildAndStatus(build: Int, status: String) = dao.find(MongoDBObject("buildNumber" -> build, "status.name" -> status))
  def getById(id: String) = dao.findOneById(new ObjectId(id))

  def findBySuiteClassAndTest(suite: String, clazz: String, test: String): List[TestCase] = {
    // find highest build number
    val cursor = dao.find(MongoDBObject(
      "suiteName" -> suite,
      "className" -> clazz,
      "testName" -> test))
      .sort(orderBy = MongoDBObject("buildNumber" -> -1));
    if (!cursor.hasNext) {
      throw new RuntimeException("no data");
    }
    val testCase = cursor.next();
    dao.find(MongoDBObject("suiteName" -> suite, "className" -> clazz, "testName" -> test, "buildNumber" -> testCase.buildNumber)).toList
  }

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

/**
 * Trait used to convert to and from json
 */
trait TestCaseJson {

  implicit val testCaseJsonWrite = (
    (__ \ 'id).write[ObjectId] and
    (__ \ 'buildNumber).write[Long] and
    (__ \ 'testName).write[String] and
    (__ \ 'className).write[String] and
    (__ \ 'suiteName).write[String] and
    (__ \ 'configurationName).write[String] and
    (__ \ 'status).write[TestStatus])

  implicit val testCaseJsonRead = (
    (__ \ 'id).read[ObjectId] ~
    (__ \ 'buildNumber).read[Long] ~
    (__ \ 'testName).read[String] ~
    (__ \ 'className).read[String] ~
    (__ \ 'suiteName).read[String] ~
    (__ \ 'configurationName).read[String] ~
    (__ \ 'status).read[TestStatus])(TestCase.apply _)
}
