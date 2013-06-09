import scala.concurrent.duration.DurationInt

import actors.TestResultLoadActor
import akka.actor.Props
import play.api.Application
import play.api.GlobalSettings
import play.api.Logger
import play.api.Play
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.libs.Akka

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    val actor = Akka.system.actorOf(Props[TestResultLoadActor], name = "testResultLoadActor")
    if (Play.current.configuration.getString("autoload.results").map(_.toBoolean).getOrElse(true)) {
      Akka.system.scheduler.schedule(5.seconds, 5.minutes, actor, actors.LoadResult)
    } else {
      Logger.info("result auto-loading disabled in config file")
    }
    //Akka.system.scheduler.schedule(0.seconds, 5.minutes, actor, actors.UpdateScores)
  }

  override def onStop(app: Application) {
  }
}