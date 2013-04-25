package controllers

import play.api._
import play.api.mvc._
import models._
import results.{ Results, Build }
import views.html.defaultpages.badRequest

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

  def viewDetails(id: String) = Action {
    TestCase.getById(id).map { tc =>
      Ok(views.html.testCaseDetails(tc))
    }.getOrElse(NotFound(""))

  }

  def isNewBuildAvailable = {
    val optResult = for {
      localMostRecent <- MetaInformation.findByKey("mostRecentBuildNumber")
      remoteMostRecent <- results.Results.findMostRecentBuild(jobUrl)
    } yield localMostRecent.toInt < remoteMostRecent.number
    optResult.getOrElse(true)
  }

  def load = Action {
    if (isNewBuildAvailable) {
      val testcases = results.Results.loadMostRecentBuild(jobUrl)
      testcases.map {
        case (buildNumber, cases) => {
          MetaInformation.insertOrUpdate("mostRecentBuildNumber", buildNumber.toString)
          cases.foreach(TestCase.save _)
        }
      }
      Ok(testcases.toList.mkString("\n"))
    } else { Ok("up to date") }
  }

  def loadBuild(buildNumber: Int) = Action {
    val triggeringBuild = Results.findRootTriggerBuild(jobUrl + "/" + buildNumber.toString)

    val testcases = Results.loadResultsForBuild(Build(buildNumber, jobUrl + "/" + buildNumber.toString), triggeringBuild.number)
    testcases.foreach(TestCase.save _)
    Ok(testcases.toList.mkString("\n"))
  }

}
