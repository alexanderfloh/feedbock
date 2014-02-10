import scala.concurrent.duration.DurationInt
import actors.TestResultLoadActor
import akka.actor.Props
import play.api.Application
import play.api.GlobalSettings
import play.api.Logger
import play.api.Play
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.libs.Akka
import play.api.Mode
import org.joda.time.DateTime
import services.MongoService

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    if (Play.current.configuration.getBoolean("autoload.results").getOrElse(true)) {
      val actor = Akka.system.actorOf(Props[TestResultLoadActor], name = "testResultLoadActor")
      Akka.system.scheduler.schedule(5.seconds, 5.minutes, actor, actors.LoadResult)
    } else {
      Logger.info("result auto-loading disabled in config file")
    }

  }

  override def onStop(app: Application) {
  }
}