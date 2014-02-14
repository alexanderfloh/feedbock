package models

import reactivemongo.bson.Macros

case class TestCaseConfiguration(
  name: String,
  var passed: Set[Int] = Set(),
  var failed: Set[Int] = Set())

object TestCaseConfiguration {
  implicit val handler = Macros.handler[TestCaseConfiguration]
}