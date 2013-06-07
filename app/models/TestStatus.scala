package models

class TestStatus(name: String) 
case object Passed extends TestStatus("Passed")
case object Failed extends TestStatus("Failed")


object TestStatus {
  def fromStringCaseInsensitive(status: String) : TestStatus = status.toUpperCase() match {
    case "PASSED" => Passed
    case "FIXED" => Passed
    case "FAILED" => Failed
    case "REGRESSION" => Failed
    case _ => throw new RuntimeException("invalid test status")
  }
}