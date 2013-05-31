package models

case class TestCaseKey(suiteName: String, className: String, testName: String) {
  def toUrlPart = {
    val urlParts = List(suiteName, className, testName)
    def encode(str: String) = java.net.URLEncoder.encode(str, "UTF-8")
    urlParts.map(encode).mkString("/")
  }
}