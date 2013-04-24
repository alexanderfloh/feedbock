package models

class TestStatus
object Passed extends TestStatus {
  override def toString = "Passed"
}
object Failed extends TestStatus {
  override def toString = "Failed"
}
object Fixed extends TestStatus {
  override def toString = "Fixed"
}
object Regression extends TestStatus {
  override def toString = "Regression"
}

object TestStatus {
  def apply(status: String) = status.toUpperCase() match {
    case "PASSED" => Passed
    case "FAILED" => Failed
    case "FIXED" => Fixed
    case "REGRESSION" => Regression
  }

}