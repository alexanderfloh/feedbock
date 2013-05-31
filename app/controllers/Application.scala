package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import models._
import results.{ Results, Build }
import org.joda.time.DateTime
import play.api.Play

object Application extends Controller {

  val jobUrl = Play.current.configuration.getString("jenkins.jobUrl")

  def index = Action {
    val history = BuildHistory.all.takeRight(6)
    val builds = history.map(_.buildNumber)
    val passedTests = history.map(_.value("passedTests"))
    val mostRecentBuild = MetaInformation.findByKey("mostRecentBuildNumber")
    val result = for {
      build <- mostRecentBuild
    } yield {
      val failed = TestCase.findByBuildAndStatus(build.toInt, "Failed").toList
      val passedCount = TestCase.findByBuildAndStatus(build.toInt, "Passed").size
      val grouped = failed.groupBy(_.testName).toList.sortBy { x => x._2.size }.reverse
      val scores = TestCaseScore.all
      val groupedWithScores = grouped.map(entry => {
        val score = scores.find(s => {
          val tc = entry._2.head
          s.id == TestCaseKey(tc.suiteName, tc.className, tc.testName)
        })
        (entry._1, (entry._2, score.map(_.value).getOrElse(10)))
      })

      Ok(views.html.index(passedCount, build.toInt, groupedWithScores, builds, passedTests))
    }
    result.getOrElse(BadRequest("unable to access jenkins"))

  }

  def viewDetails(suite: String, clazz: String, test: String) = Action {
    val results = TestCase.findBySuiteClassAndTest(suite, clazz, test)

    val firstResult = results.head

    val configurationNames = results.map(entry => (entry.configurationName, entry.status))
    val history = TestCaseHistory.getHistoryByTestCase(firstResult)

    Ok(views.html.testCaseDetails(firstResult, history, feedbackForm, configurationNames))
  }

  def loadBuild(buildNumber: Int) = Action {
    val triggeringBuild = Results.findRootTriggerBuild(jobUrl.get + "/" + buildNumber.toString)

    val testcases = Results.loadResultsForBuild(Build(buildNumber, jobUrl.get + "/" + buildNumber.toString), triggeringBuild.number)
    testcases.foreach(TestCase.save _)
    Ok(testcases.toList.mkString("\n"))
  }

  def submitFeedback(suite: String, className: String, test: String) = Action { implicit request =>
    val (defect, codeChange, timing, comment) = feedbackForm.bindFromRequest.get

    val results = TestCase.findBySuiteClassAndTest(suite, className, test)
    val firstResult = results.head

    val configurationNames = results.map(entry => (entry.configurationName, entry.status))

    val mostRecentBuild = MetaInformation.findByKey("mostRecentBuildNumber").map(_.toInt).getOrElse(0)
    val additionalData = Map("defect" -> defect, "codeChange" -> codeChange, "timing" -> timing).map { case (key, value) => (key, value.toString) }
    val history = TestCaseHistory(mostRecentBuild, className, suite, test, comment, DateTime.now, additionalData)
    TestCaseHistory.insert(history)
    Redirect(routes.Application.viewDetails(suite, className, test))
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
