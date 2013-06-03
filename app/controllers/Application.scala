package controllers

import models._
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

object Application  extends Controller with MongoController {

  val jobUrl = Play.current.configuration.getString("jenkins.jobUrl")

  val collection = db[BSONCollection]("testCases")

  def index = Action {
    BadRequest("Not implemented")
    /*
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
*/
  }
  
  def reactiveMongo = Action { implicit request =>
    Async {
      val query = BSONDocument(
        "$query" -> BSONDocument("_id.suiteName" -> "suiteName"))
      val found = collection.find(query).cursor[TestCase]
      val now = new DateTime
      val newTestCase = TestCase(
        Option(TestCaseKey("suiteName", "className", "testName9")),
        List[TestCaseConfiguration](
          TestCaseConfiguration("name", List[Int](1, 2), List[Int](3, 4))),
        List[TestCaseFeedback](TestCaseFeedback("peter", 3, Option[DateTime](now), true, true, true, "comment")),
        10)
      collection.insert[TestCase](newTestCase)
      found.toList.map { testCases =>
        Ok(views.html.reactiveMongo(testCases))
      }.recover {
        case e =>
          e.printStackTrace()
          BadRequest(e.getMessage())
      }
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
