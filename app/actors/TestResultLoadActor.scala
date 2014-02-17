package actors

import akka.actor._
import models.MetaInformation
import results._
import models._
import services._
import play.api._
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

case class LoadResult()

class TestResultLoadActor extends Actor {

  val jobUrl = Play.current.configuration.getString("jenkins.jobUrl")

  private def isNewBuildAvailable = {
    val localMostRecent = Await.result(MongoService.loadMetaInformation("mostRecentBuildNumber"), Duration.Inf).getOrElse(MetaInformation("mostRecentBuildNumber", "0"))
    Logger.info("local most recent build number: " + localMostRecent.value)
    val optResult = for {
      remoteMostRecent <- results.Results.findMostRecentBuild(jobUrl.get)
    } yield {
      val triggeringBuild = results.Results.findRootTriggerBuild(remoteMostRecent.url)
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
        for {
          loadedBuild <- results.Results.loadMostRecentBuild(jobUrl.get)
          stats <- Await.result(MongoService.calcScoreForBuild(loadedBuild), Duration.Inf)
        } yield (MongoService.saveBuildStats(stats))
      }
    }
  }
}