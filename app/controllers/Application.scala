package controllers

import models._
import services._
import org.joda.time.DateTime
import scala.concurrent.Future
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import play.api.mvc._
import play.modules.reactivemongo.{ MongoController, ReactiveMongoPlugin }
import reactivemongo.api.gridfs.GridFS
import reactivemongo.api.gridfs.Implicits.DefaultReadFileReader
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson._
import scala.concurrent.duration.Duration
import scala.concurrent.Await

object Application extends Controller with MongoController {

  val jobUrl = Play.current.configuration.getString("jenkins.jobUrl")

  val collection = db[BSONCollection]("testCases")

  def index = Action {
    val mostRecentBuild = Await.result(MongoService.loadMetaInformation("mostRecentBuildNumber"), Duration.Inf).getOrElse(MetaInformation("mostRecentBuildNumber", "0"))
    Async {
      val history = List[BuildHistory]()
      val builds = history.map(_.buildNumber)
      val passedTests = history.map(_.value("passedTests"))
      val passedCount = 10 // TestCase.findByBuildAndStatus(build.toInt, "Passed").size
      val failed = Await.result(MongoService.loadFailedTestsSortedByScoreDesc(mostRecentBuild.value.toInt).toList, Duration.Inf)
      val failedWithDetails = failed.map(tc => (tc, generateDetailsView(tc.id)))
      Future(Ok(views.html.index(passedCount, mostRecentBuild.value.toInt, failedWithDetails, builds, passedTests)))
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
