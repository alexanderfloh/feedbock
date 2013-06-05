package actors

import akka.actor._
import models.MetaInformation
import results._
import models._
import services._
import play.api._
import scala.concurrent.duration.Duration
import scala.concurrent.Await

case class LoadResult()
case class UpdateScores()

class TestResultLoadActor extends Actor {

  val jobUrl = Play.current.configuration.getString("jenkins.jobUrl")

  private def isNewBuildAvailable = {
    val localMostRecent = Await.result(MongoService.loadMetaInformation("mostRecentBuildNumber"), Duration.Inf).getOrElse(MetaInformation("mostRecentBuildNumber", "0"))
    val optResult = for {
      remoteMostRecent <- results.Results.findMostRecentBuild(jobUrl.get)
    } yield localMostRecent.value.toInt < remoteMostRecent.number
    optResult.getOrElse(true)
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