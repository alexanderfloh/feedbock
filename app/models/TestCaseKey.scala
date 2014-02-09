package models

import reactivemongo.bson.Macros

case class TestCaseKey(
  suiteName: String,
  className: String,
  testName: String) {

  val suiteNameWithBreakHints = addBreakHints(suiteName)
  val classNameWithBreakHints = addBreakHints(className)
  val testNameWithBreakHints = addBreakHints(testName)

  private def addBreakHints(str: String) = {
    str.map(c => if (c.isUpper || c == '_') "<wbr/>" + c else c).mkString
  }

  def toUrlPart = {
    val urlParts = List(suiteName, className, testName)
    def encode(str: String) = java.net.URLEncoder.encode(str, "UTF-8")
    urlParts.map(encode).mkString("/")
  }
}

object TestCaseKey {
  implicit val handler = Macros.handler[TestCaseKey]
}