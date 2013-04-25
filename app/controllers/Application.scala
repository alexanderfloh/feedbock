package controllers

import play.api._
import play.api.mvc._
import models._
import results.{Results, Build}

object Application extends Controller {

  val jobUrl = "http://lnz-bobthebuilder/hudson/job/Trigger%20BVT%20Testset%20AllInOne/"

  def index = Action {
    val mostRecentBuild = results.Results.findMostRecentBuild(jobUrl)
    val result = for {
      failed <- mostRecentBuild.map(b => TestCase.findByBuildAndStatus(b.number, "Failed"))
      passedCount <- mostRecentBuild.map(b => TestCase.findByBuildAndStatus(b.number, "Passed").size)
    } yield {
      val grouped = failed.toList.groupBy(_.testName).toList.sortBy { x => x._2.size }.reverse
      Ok(views.html.index(passedCount, grouped))
    }
    result.getOrElse(BadRequest("unable to access jenkins"))

  }

  def viewDetails(id: String) = TODO

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