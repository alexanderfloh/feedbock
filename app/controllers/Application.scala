package controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import models._
import results.{ Results, Build }
import views.html.defaultpages.badRequest
import org.joda.time.DateTime

object Application extends Controller {

  val jobUrl = "http://lnz-bobthebuilder/hudson/job/Trigger%20BVT%20Testset%20AllInOne/"

  def index = Action {
    val mostRecentBuild = MetaInformation.findByKey("mostRecentBuildNumber")
    val result = for {
      build <- mostRecentBuild
    } yield {
      val failed = TestCase.findByBuildAndStatus(build.toInt, "Failed").toList
      val passedCount = TestCase.findByBuildAndStatus(build.toInt, "Passed").size
      val grouped = failed.groupBy(_.testName).toList.sortBy { x => x._2.size }.reverse
      Ok(views.html.index(passedCount, build.toInt, grouped))
    }
    result.getOrElse(BadRequest("unable to access jenkins"))

  }

  def viewDetails(suite: String, clazz: String, test: String) = Action {
    val results = TestCase.findBySuiteClassAndTest(suite, clazz, test)

    val firstResult = results.head
    val history = TestCaseHistory.getHistoryByTestCase(firstResult)

    Ok(views.html.testCaseDetails(firstResult, history, feedbackForm))

  }

  def loadBuild(buildNumber: Int) = Action {
    val triggeringBuild = Results.findRootTriggerBuild(jobUrl + "/" + buildNumber.toString)

    val testcases = Results.loadResultsForBuild(Build(buildNumber, jobUrl + "/" + buildNumber.toString), triggeringBuild.number)
    testcases.foreach(TestCase.save _)
    Ok(testcases.toList.mkString("\n"))
  }

  def submitFeedback(suite: String, className: String, test: String) = Action { implicit request =>
    val (defect, codeChange, timing, comment) = feedbackForm.bindFromRequest.get

    val results = TestCase.findBySuiteClassAndTest(suite, className, test)
    val firstResult = results.head

    val mostRecentBuild = MetaInformation.findByKey("mostRecentBuildNumber").map(_.toInt).getOrElse(0)
    val additionalData = Map("defect" -> defect, "codeChange" -> codeChange, "timing" -> timing).map { case (key, value) => (key, value.toString) }
    val history = TestCaseHistory(mostRecentBuild, className, suite, test, comment, DateTime.now, additionalData)
    TestCaseHistory.insert(history)

    Redirect(routes.Application.viewDetails(suite, className, test))
  }

  val feedbackForm = Form(
    tuple(
      "defect" -> boolean,
      "codeChange" -> boolean,
      "timing" -> boolean,
      "comment" -> text))

}
