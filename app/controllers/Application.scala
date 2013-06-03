package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import models._
import service._
import results.{ Results, Build }
import org.joda.time.DateTime
import play.api.Play

object Application extends Controller {

  val jobUrl = Play.current.configuration.getString("jenkins.jobUrl")

  def index = Action {
    val testCase = MongoService.loadTestCase

    println("testcase: " + testCase)

    val history = BuildHistory.all.takeRight(6)
    val builds = history.map(_.buildNumber)
    val passedTests = history.map(_.value("passedTests"))
    val mostRecentBuild = MetaInformation.findByKey("mostRecentBuildNumber")
    val result = for {
      build <- mostRecentBuild
    } yield {
      val passedCount = 10// TestCase.findByBuildAndStatus(build.toInt, "Passed").size
      val failed = List[TestCase]() // TestCase.findByBuildAndStatus(build.toInt, "Failed").toList
      val scores = TestCaseScore.all
      val grouped = failed.groupBy(_.id).toList
      val groupedWithScores = grouped.map {
        case (key, testcases) => {
          val score = scores.find(_.id == key)
          (key, (testcases, score.map(_.value).getOrElse(10)))
        }
      }.sortBy { case (_, (_, score)) => score }.reverse

      Ok(views.html.index(passedCount, build.toInt, groupedWithScores, builds, passedTests))
    }
    result.getOrElse(BadRequest("unable to find most recent build number"))

  }

  def viewDetails(suite: String, clazz: String, test: String) = Action {
    val action = for {
      mostRecentBuild <- MetaInformation.findByKey("mostRecentBuildNumber")
      testcase <- TestCase.findOneById(TestCaseKey(suite, clazz, test))
    } yield {
      val failedConfigs = testcase.configurations.filter(configuration => configuration.failed.contains(mostRecentBuild))
      val history = testcase.feedback
      Ok(views.html.testCaseDetails(testcase.id, mostRecentBuild.toInt, history.toList, feedbackForm, failedConfigs.toList))
    }
    action.getOrElse(NotFound("testcase not found"))
  }

  def loadBuild(buildNumber: Int) = Action {
    val triggeringBuild = Results.findRootTriggerBuild(jobUrl.get + "/" + buildNumber.toString)

    val testcases = Results.loadResultsForBuild(Build(buildNumber, jobUrl.get + "/" + buildNumber.toString), triggeringBuild.number)
    testcases.foreach(TestCase.save _)
    Ok(testcases.toList.mkString("\n"))
  }

  def submitFeedback(suite: String, className: String, test: String) = Action { implicit request =>
    val (defect, codeChange, timing, comment) = feedbackForm.bindFromRequest.get

    val action = for {
      testcase <- TestCase.findOneById(TestCaseKey(suite, className, test))
    } yield {
      val mostRecentBuild = MetaInformation.findByKey("mostRecentBuildNumber").map(_.toInt).getOrElse(0)
      val additionalData = Map(
        "defect" -> defect,
        "codeChange" -> codeChange,
        "timing" -> timing).map { case (key, value) => (key, value.toString) }
      val history = TestCaseHistory(mostRecentBuild, className, suite, test, comment, DateTime.now, additionalData)
      TestCaseHistory.insert(history)
      Redirect(routes.Application.viewDetails(suite, className, test))

    }
    action.getOrElse(NotFound("testcase not found"))
  }

  def calc = Action {
    Ok(TestCase.countByStatus().mkString)
  }

  def calcScores = Action {
    Ok(TestCaseHistory.calculateScore.mkString)
  }

  val feedbackForm = Form(
    tuple(
      "defect" -> boolean,
      "codeChange" -> boolean,
      "timing" -> boolean,
      "comment" -> text))

}
