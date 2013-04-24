package models
import com.mongodb.casbah.Imports._
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class TestStatus(name: String) 

object TestStatus {
  def fromStringCaseInsensitive(status: String) = status.toUpperCase() match {
    case "PASSED" => TestStatus("Passed")
    case "FAILED" => TestStatus("Failed")
    case "FIXED" => TestStatus("Fixed")
    case "REGRESSION" => TestStatus("Regression")
    case _ => throw new RuntimeException("invalid test status")
  }

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