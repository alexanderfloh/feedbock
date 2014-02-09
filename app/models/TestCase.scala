package models

import org.joda.time.DateTime
import reactivemongo.bson._
import reactivemongo.bson.Macros.Annotations.Key

case class TestCaseKey(
  suiteName: String,
  className: String,
  testName: String) {

  val suiteNameWithBreakHints = addBreakHints(suiteName)
  val classNameWithBreakHints = addBreakHints(className)
  val testNameWithBreakHints = addBreakHints(testName)

  private def addBreakHints(str: String) = {
    str.map(c => if (c.isUpper || c == '_') "<wbr/>" + c else c).mkString
  }

  def toUrlPart = {
    val urlParts = List(suiteName, className, testName)
    def encode(str: String) = java.net.URLEncoder.encode(str, "UTF-8")
    urlParts.map(encode).mkString("/")
  }
}

object TestCaseKey {
  implicit val handler = Macros.handler[TestCaseKey]
}

case class TestCaseConfiguration(
  name: String,
  var passed: List[Int] = List(),
  var failed: List[Int] = List())

object TestCaseConfiguration {
  implicit val handler = Macros.handler[TestCaseConfiguration]
}

case class TestCaseFeedback(
  user: String,
  alias: String,
  build: Int,
  timestamp: DateTime,
  defect: Boolean,
  codeChange: Boolean,
  timingIssue: Boolean,
  comment: String) {

  def scoreDelta = {
    var delta = 0
    if (defect) delta += 10
    if (codeChange) delta += 5
    if (timingIssue) delta -= 2
    delta
  }
}

object TestCaseFeedback {
  implicit object BSONDateTimeHandler extends BSONHandler[BSONDateTime, DateTime] {
    def read(time: BSONDateTime) = new DateTime(time.value)
    def write(jdtime: DateTime) = BSONDateTime(jdtime.getMillis)
  }
  
  implicit val handler = Macros.handler[TestCaseFeedback]
}

case class TestCase(
  @Key("_id") id: TestCaseKey,
  configurations: List[TestCaseConfiguration] = List(),
  feedback: List[TestCaseFeedback] = List(),
  score: Int = TestCase.initialScore) {
  
  def failedConfigsForBuild(buildNumber: Int) =
    configurations.filter(_.failed.contains(buildNumber))

  def passedConfigsForBuild(buildNumber: Int) =
    configurations.filter(_.passed.contains(buildNumber))

  def withConfiguration(configuration: TestCaseConfiguration) =
    copy(configurations = configuration :: configurations)

  def withFeedback(fb: TestCaseFeedback) =
    copy(feedback = fb :: feedback, score = calculateScore(fb :: feedback))

  private def calculateScore(feedback: List[TestCaseFeedback]) =
    TestCase.initialScore + feedback.map(_.scoreDelta).sum

}

object TestCase {
  val initialScore = 10
  
  implicit val handler = Macros.handler[TestCase]
}
