import com.mongodb.casbah.Imports._
import play.api._
import libs.ws.WS
import models._
import se.radley.plugin.salat._
import results.Passed

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    val c = TestCase(new ObjectId(), "test1", "class1j", "suite1", "Win 7 config", TestStatus("Passed"));
    TestCase.save(c)
    val c1 = TestCase(new ObjectId(), "test1", "class1j", "suite1", "Win 7 config", TestStatus("Passed"));
    TestCase.save(c1)
//        if (User.count(DBObject(), Nil, Nil) == 0) {
//      Logger.info("Loading Testdata")
//      User.save(User(
//        username = "leon",
//        password = "1234",
//        address = Some(Address("Örebro", "123 45", "Sweden"))
//      ))
//
//      User.save(User(
//        username = "guillaume",
//        password = "1234",
//        address = Some(Address("Paris", "75000", "France"))
//      ))
//    }
  }

  override def onStop(app: Application) {
  }
}