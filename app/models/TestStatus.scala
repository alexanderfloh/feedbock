package models
import com.mongodb.casbah.Imports._
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class TestStatus(name: String) {
}
//  val name: String
//def toString = name
//object Passed extends TestStatus {
//  override val name = "Passed"
//}
//object Failed extends TestStatus {
//  override val name = "Failed"
//}
//object Fixed extends TestStatus {
//  override val name = "Fixed"
//}
//object Regression extends TestStatus {
//  override val name = "Regression"
//}

object TestStatus {
  //  def foo(status: String) : TestStatus = status.toUpperCase() match {
  //    case "PASSED" => Passed
  //    case "FAILED" => Failed
  //    case "FIXED" => Fixed
  //    case "REGRESSION" => Regression
  //case _ => throw new RuntimeException("invalid test status")
  //}

  // Conversions
  implicit val testStatusJsonWrite = new Writes[TestStatus] {
    def writes(a: TestStatus): JsValue = {
      Json.obj(
        "name" -> a.name)
    }
  }

  implicit val testStatusJsonRead: Reads[TestStatus] = (
    (JsPath \ 'name).read[String]).map(TestStatus.apply _)
}