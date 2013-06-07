package controllers

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration

import org.joda.time.DateTime

import models.MetaInformation
import models.TestCaseFeedback
import models.TestCaseHistory
import models.TestCaseKey
import play.api.Play
import play.api.data.Form
import play.api.data.Forms.boolean
import play.api.data.Forms.text
import play.api.data.Forms.tuple
import play.api.mvc.Action
import play.api.mvc.Controller
import services.MongoService

object Application extends Controller {

  val jobUrl = Play.current.configuration.getString("jenkins.jobUrl")

  def index = Action {

    Async {
      val buildFuture = MongoService.loadMetaInformation("mostRecentBuildNumber")
      buildFuture.flatMap { buildOpt =>
        buildOpt.map { buildMeta =>
          val build = buildMeta.value.toInt
          for {
            passedTests <- MongoService.loadPassedTests(build).toList
            failed <- MongoService.loadFailedTestsSortedByScoreDesc(build).toList
          } yield {
            val passedScore = passedTests.map(tc => tc.score * tc.passedConfigsForBuild(build).size).sum
            val failedWithDetails = failed.map(tc => (tc, generateDetailsView(tc.id)))
            Ok(views.html.index(passedScore, build, failedWithDetails))
          }
        }.getOrElse(Future(BadRequest("unable to access meta information")))
      }
    }
  }

  def generateDetailsView(id: TestCaseKey) = {
    val mostRecentBuild = Await.result(MongoService.loadMetaInformation("mostRecentBuildNumber"), Duration.Inf)
      .getOrElse(MetaInformation("mostRecentBuildNumber", "0"))
    val testCase = Await.result(MongoService.loadTestCaseByKey(id), Duration.Inf)
    testCase.map { tc =>
      views.html.testCaseDetails(tc, mostRecentBuild.value.toInt, feedbackForm)
    }.getOrElse(play.api.templates.Html.empty)
  }

  def viewDetails(suite: String, clazz: String, test: String) = Action {
    Ok(generateDetailsView(TestCaseKey(suite, clazz, test)))
  }

  def loadBuild(buildNumber: Int) = Action {
    BadRequest("TODO")
    /*
    val triggeringBuild = Results.findRootTriggerBuild(jobUrl.get + "/" + buildNumber.toString)

    val testcases = Results.loadResultsForBuild(Build(buildNumber, jobUrl.get + "/" + buildNumber.toString), triggeringBuild.number)
    testcases.foreach(TestCase.save _)
    Ok(testcases.toList.mkString("\n"))
    */
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

  def calc = Action {
    BadRequest("TODO")
    //Ok(TestCase.countByStatus().mkString)
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
