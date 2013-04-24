import com.mongodb.casbah.Imports._
import play.api._
import libs.ws.WS
import models._
import se.radley.plugin.salat._

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    val c = TestCase(new ObjectId(), 1234, "test1", "class1j", "suite1", "Win 7 config", TestStatus("Passed"));
    TestCase.save(c)
    val c1 = TestCase(new ObjectId(), 1234, "test1", "class1j", "suite1", "Win 7 config", TestStatus("Passed"));
    TestCase.save(c1)
  }

  override def onStop(app: Application) {
  }
}