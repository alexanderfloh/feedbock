package models

import org.jboss.netty.buffer._
import org.joda.time.DateTime
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._
import reactivemongo.bson._

case class TestCaseKey(suiteName: String, className: String, testName: String) {
  def toUrlPart = {
    val urlParts = List(suiteName, className, testName)
    def encode(str: String) = java.net.URLEncoder.encode(str, "UTF-8")
    urlParts.map(encode).mkString("/")
  }
}

case class TestCaseConfiguration(
    name: String, 
    var passed: List[Int] = List(), 
    var failed: List[Int] = List())

object TestCaseConfiguration {
  implicit object TestCaseConfigurationBSONReader extends BSONDocumentReader[TestCaseConfiguration] {
    def read(config: BSONDocument): TestCaseConfiguration = {
      TestCaseConfiguration(
        config.getAs[String]("name").get,
        config.getAs[List[Int]]("passed").get,
        config.getAs[List[Int]]("failed").get)
    }
  }
  implicit object TestCaseConfigurationBSONWriter extends BSONDocumentWriter[TestCaseConfiguration] {
    def write(config: TestCaseConfiguration): BSONDocument = {
      BSONDocument(
        "name" -> config.name,
        "passed" -> config.passed,
        "failed" -> config.failed)
    }
  }
}

case class TestCaseFeedback(
    user: String,
    build: Int,
    timestamp: DateTime,
    defect: Boolean,
    codeChange: Boolean,
    timingIssue: Boolean,
    comment: String)

object TestCaseFeedback {
  implicit object TestCaseFeedbackBSONReader extends BSONDocumentReader[TestCaseFeedback] {
    def read(doc: BSONDocument): TestCaseFeedback = {
      val timestamp = doc.getAs[BSONDateTime]("timestamp").get
      TestCaseFeedback(
        doc.getAs[String]("user").get,
        doc.getAs[Int]("build").get,
        new DateTime(timestamp.value),
        doc.getAs[Boolean]("defect").get,
        doc.getAs[Boolean]("codeChange").get,
        doc.getAs[Boolean]("timingIssue").get,
        doc.getAs[String]("comment").get)
    }
  }
  implicit object TestCaseConfigurationBSONWriter extends BSONDocumentWriter[TestCaseFeedback] {
    def write(feedback:TestCaseFeedback): BSONDocument = {
      val timestamp = feedback.timestamp
      BSONDocument(
        "user" -> feedback.user,
        "build" -> feedback.build,
        "timestamp" -> BSONDateTime(timestamp.getMillis),
        "defect" -> feedback.defect,
        "codeChange" -> feedback.codeChange,
        "timingIssue" -> feedback.timingIssue,
        "comment" -> feedback.comment)
    }
  }
}

case class TestCase(
  var id: TestCaseKey,
  var configurations: List[TestCaseConfiguration] = List(),
  var feedback: List[TestCaseFeedback] = List(),
  score: Int = 10) {
  def failedConfigsForBuild(buildNumber: Int) = {
    var failedConfigs = List[TestCaseConfiguration]()
    this.configurations.foreach({
      config =>
        failedConfigs = failedConfigs ++ List(config)
        })
    failedConfigs
  }

}

object TestCase {
  implicit object TestCaseBSONReader extends BSONDocumentReader[TestCase] {
    def read(doc: BSONDocument): TestCase = {
      val objId = doc.getAs[BSONDocument]("_id").get
      TestCase(
        TestCaseKey(
          objId.getAs[String]("suiteName").get,
          objId.getAs[String]("className").get,
          objId.getAs[String]("testName").get),
        doc.getAs[List[TestCaseConfiguration]]("configurations").toList.flatten,
        doc.getAs[List[TestCaseFeedback]]("feedback").toList.flatten,
        doc.getAs[Int]("score").get)
    }
  }
  implicit object TestCaseBSONWriter extends BSONDocumentWriter[TestCase] {
    def write(testCase:TestCase): BSONDocument = {
      BSONDocument(
      "_id" -> BSONDocument(
        "suiteName" -> testCase.id.suiteName,
        "className" -> testCase.id.className,
        "testName" -> testCase.id.testName),
      "configurations" -> testCase.configurations,
      "feedback" -> testCase.feedback,
      "score" -> testCase.score)
    }
  }
}

/*
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
*/