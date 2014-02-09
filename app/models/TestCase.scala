package models

import org.joda.time.DateTime
import reactivemongo.bson._
import reactivemongo.bson.Macros.Annotations.Key

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
