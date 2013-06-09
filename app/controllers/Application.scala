package controllers

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import org.joda.time.DateTime
import models.MetaInformation
import models.TestCaseFeedback
import models.TestCaseKey
import play.api.Play
import play.api.data.Form
import play.api.data.Forms.boolean
import play.api.data.Forms.text
import play.api.data.Forms.tuple
import play.api.mvc.Action
import play.api.mvc.Controller
import services.MongoService
import models.TestCase

object Application extends Controller {

  val jobUrl = Play.current.configuration.getString("jenkins.jobUrl")

  def index = Action {

    Async {
      val buildFuture = MongoService.loadMetaInformation("mostRecentBuildNumber")
      buildFuture.flatMap { buildOpt =>
        buildOpt.map { buildMeta =>
          val build = buildMeta.value.toInt
          for {
            //passedTests <- MongoService.loadPassedTests(build).toList
            failed <- MongoService.loadFailedTestsSortedByScoreDesc(build).toList
          } yield {
            val passedScore = 12345 //passedTests.map(tc => tc.score * tc.passedConfigsForBuild(build).size).sum

            def generateDetailsView(testCase: TestCase, mostRecentBuildNumber: Int) =
              views.html.testCaseDetails(testCase, mostRecentBuildNumber, feedbackForm)

            val failedWithDetails = failed.map(tc => (tc, generateDetailsView(tc, build)))
            Ok(views.html.index(passedScore, build, failedWithDetails))
          }
        }.getOrElse(Future(BadRequest("unable to access meta information")))
      }
    }
  }

  def submitFeedback(suite: String, className: String, test: String) = Action { implicit request =>
    Async {
      val (defect, codeChange, timing, comment) = feedbackForm.bindFromRequest.get
      val id = TestCaseKey(suite, className, test)
      for {
        currentBuildOpt <- MongoService.loadMetaInformation("mostRecentBuildNumber")
        testCaseOpt <- MongoService.loadTestCaseByKey(id)
      } yield {
        val actionOpt = for {
          currentBuild <- currentBuildOpt
          testCase <- testCaseOpt
        } yield {
          val feedback = TestCaseFeedback(
            "hugo",
            currentBuild.value.toInt,
            DateTime.now,
            defect,
            codeChange,
            timing,
            comment)
          val updated = testCase.withFeedback(feedback)
          MongoService.saveTestCase(updated)
          Redirect(routes.Application.index)
        }
        actionOpt.getOrElse(NotFound("invalid test case id"))
      }
    }
  }

  val feedbackForm = Form(
    tuple(
      "defect" -> boolean,
      "codeChange" -> boolean,
      "timing" -> boolean,
      "comment" -> text))

}
