package actors

import akka.actor._
import models.MetaInformation
import results._
import models._
import play.api._

case class LoadResult()

class TestResultLoadActor extends Actor {

  val jobUrl = "http://lnz-bobthebuilder/hudson/job/Trigger%20BVT%20Testset%20AllInOne/"

  private def isNewBuildAvailable = {
    val optResult = for {
      localMostRecent <- MetaInformation.findByKey("mostRecentBuildNumber")
      remoteMostRecent <- results.Results.findMostRecentBuild(jobUrl)
    } yield localMostRecent.toInt < remoteMostRecent.number
    optResult.getOrElse(true)
  }

  def receive = {
    case LoadResult => {
      Logger.info("checking for new test results")
      if (isNewBuildAvailable) {
        Logger.info("loading new test results")
        val testcases = results.Results.loadMostRecentBuild(jobUrl)
        testcases.map {
          case (buildNumber, cases) => {
            MetaInformation.insertOrUpdate("mostRecentBuildNumber", buildNumber.toString)
            cases.foreach(TestCase.save _)
          }
        }
      }
    }
  }
}