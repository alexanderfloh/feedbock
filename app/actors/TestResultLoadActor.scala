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

class TestResultLoadActor extends Actor {

  val jobUrl = Play.current.configuration.getString("jenkins.jobUrl")

  private def isNewBuildAvailable = {
    val localMostRecent = Await.result(MongoService.loadMetaInformation("mostRecentBuildNumber"), Duration.Inf).getOrElse(MetaInformation("mostRecentBuildNumber", "0"))
    Logger.info("local most recent build number: " + localMostRecent.value)
    val optResult = for {
      remoteMostRecent <- results.Results.findMostRecentBuild(jobUrl.get)
    } yield {
      val triggeringBuild =  results.Results.findRootTriggerBuild(remoteMostRecent.url)
      Logger.info("remote most recent build number: " + triggeringBuild.number)
      localMostRecent.value.toInt < triggeringBuild.number
    }
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
  }
}