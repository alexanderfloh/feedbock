package actors

import akka.actor._
import models.MetaInformation
import results._
import models._
import play.api._

case class LoadResult()
case class UpdateScores()

class TestResultLoadActor extends Actor {

  val jobUrl = Play.current.configuration.getString("jenkins.jobUrl")

  private def isNewBuildAvailable = {
    /*
    val optResult = for {
      localMostRecent <- MetaInformation.findByKey("mostRecentBuildNumber")
      remoteMostRecent <- results.Results.findMostRecentBuild(jobUrl.get)
    } yield localMostRecent.toInt < remoteMostRecent.number
    optResult.getOrElse(true)
    */
    //true
    false
  }
  def receive = {
    case LoadResult => {
      Logger.info("checking for new test results")
      if (isNewBuildAvailable) {
        Logger.info("loading new test results")
        val testcases = results.Results.loadMostRecentBuild(jobUrl.get)
//        testcases.map {
//          case (buildNumber, cases) => {
//            MetaInformation.insertOrUpdate("mostRecentBuildNumber", buildNumber.toString)
//          }
//        }
      }
    }
    
    case UpdateScores => {
      Logger.info("updating scores")
      TestCaseHistory.calculateScore
    }
  }
}