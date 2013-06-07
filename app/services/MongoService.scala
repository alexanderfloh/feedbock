package services

import play.api.libs.iteratee._
import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.api.collections.default.BSONCollection
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import models._

object MongoService {
  /** Returns the default database (as specified in `application.conf`). */
  def db = ReactiveMongoPlugin.db

  def testCases = db[BSONCollection]("testCases")
  def metaInformation = db[BSONCollection]("metaInformation")
  
  def loadTestCaseByKey(key: TestCaseKey) = {
    val query = BSONDocument("_id" -> BSONDocument(
      "suiteName" -> key.suiteName,
      "className" -> key.className,
      "testName" -> key.testName))
    testCases.find(query).cursor[TestCase].headOption
  }

  def loadFailedTestsSortedByScoreDesc(build: Int) = {
    val query = BSONDocument(
      "$orderby" -> BSONDocument("score" -> -1),
      "$query" -> BSONDocument("configurations.failed" -> build))
    testCases.find(query).cursor[TestCase]
  }
  
  def loadPassedTests(build: Int) = {
    val query = BSONDocument(
      "$query" -> BSONDocument("configurations.passed" -> build))
    testCases.find(query).cursor[TestCase]
  }

  def saveTestCase(testCase: TestCase) = {
    testCases.save(testCase)
  }

  def loadMetaInformation(key: String) = {
    val query = BSONDocument(
      "_id" -> key)
    metaInformation.find(query).cursor[MetaInformation].headOption
  }

  def saveMetaInformation(doc: MetaInformation) = {
    metaInformation.save(doc)
  }

}