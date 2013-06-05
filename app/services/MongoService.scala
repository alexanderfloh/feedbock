package services

import play.api.libs.iteratee._
import reactivemongo.api._
import reactivemongo.bson._
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.api.collections.default.BSONCollection
import models._

object MongoService {
  
  val db = {
    val driver = new MongoDriver
    val connection = driver.connection(List("localhost"))
    connection("feedbock")
  }

  def loadTestCaseByKey(key: TestCaseKey) = {
    val query = BSONDocument("_id" -> BSONDocument(
      "suiteName" -> key.suiteName,
      "className" -> key.className,
      "testName" -> key.testName))
    db("testCases").find(query).cursor[TestCase].headOption
  }

  def loadFailedTestsSortedByScoreDesc(build: Int) = {
    val query = BSONDocument(
      "$orderby" -> BSONDocument("score" -> -1),
      "$query" -> BSONDocument("configurations.failed" -> build))
    db("testCases").find(query).cursor[TestCase]
  }

  def saveTestCase(testCase: TestCase) = {
    db("testCases").save(testCase)
  }

  def loadMetaInformation(key: String) = {
    val query = BSONDocument(
      "_id" -> key)
    db("metaInformation").find(query).cursor[MetaInformation].headOption
  }

  def saveMetaInformation(doc: MetaInformation) = {
    db("metaInformation").save(doc)
  }

}