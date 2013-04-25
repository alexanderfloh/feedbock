import com.mongodb.casbah.Imports._
import play.api._
import libs.ws.WS
import models._
import se.radley.plugin.salat._
import play.libs.Akka
import akka.actor.Props
import actors.TestResultLoadActor
import concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._


object Global extends GlobalSettings {
  override def onStart(app: Application) {
    val actor = Akka.system.actorOf(Props[TestResultLoadActor], name = "testResultLoadActor")
    Akka.system.scheduler.schedule(5.seconds, 5.minutes, actor, actors.LoadResult)
  }

  override def onStop(app: Application) {
  }
}