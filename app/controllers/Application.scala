package controllers

import models._
import services._
import org.joda.time.DateTime
import scala.concurrent.Future
import play.api.Logger
import play.api.Play
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
      val failed = Await.result(MongoService.loadFailedTestsSortedByScoreDesc(mostRecentBuild.value.toInt).toList, Duration.Inf) // TestCase.findByBuildAndStatus(build.toInt, "Failed").toList
      Future(Ok(views.html.index(passedCount, mostRecentBuild.value.toInt, failed, builds, passedTests)))
    }
  }

  def viewDetails(suite: String, clazz: String, test: String) = Action {
    BadRequest("TODO")
    /*
    val action = for {
      mostRecentBuild <- MetaInformation.findByKey("mostRecentBuildNumber")
      testcase <- TestCase.findOneById(TestCaseKey(suite, clazz, test))
    } yield {
      val failedConfigs = testcase.configurations.filter(configuration => configuration.failed.contains(mostRecentBuild))
      val history = testcase.feedback
      Ok(views.html.testCaseDetails(testcase.id, mostRecentBuild.toInt, history.toList, feedbackForm, failedConfigs.toList))
    }
    action.getOrElse(NotFound("testcase not found"))
    */
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
    BadRequest("TODO")
    /*
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
    */
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
