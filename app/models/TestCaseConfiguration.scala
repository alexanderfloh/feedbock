package models

import reactivemongo.bson.Macros

case class TestCaseConfiguration(
  name: String,
  var passed: List[Int] = List(),
  var failed: List[Int] = List())

object TestCaseConfiguration {
  implicit val handler = Macros.handler[TestCaseConfiguration]
}