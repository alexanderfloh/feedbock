package services

import play.api.libs.iteratee._
import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.api.collections.default.BSONCollection
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import models._
import reactivemongo.core.commands._

object MongoService {
  /** Returns the default database (as specified in `application.conf`). */
  private def db = ReactiveMongoPlugin.db

  private def testCases = db[BSONCollection]("testCases")
  private def metaInformation = db[BSONCollection]("metaInformation")
  private def users = db[BSONCollection]("users")

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
  
  def buildStatsStream(latestBuild: Int) = {
    for(build <- (latestBuild to(0, -1)).toStream) yield {
      calcScoreForBuild(build)
    }
  }

  def calcScoreForBuild(build: Int) = {
    val cmd = Aggregate("testCases", Seq(
      Match(BSONDocument("configurations.passed" -> BSONInteger(build))),
      Unwind("configurations"),
      Match(BSONDocument("configurations.passed" -> BSONInteger(build))),
      Group(BSONInteger(build))("scoreOfBuild" -> SumField("score"))
    ))
    val result = db.command(cmd, ReadPreference.Primary)
    val converted = result.map(_.headOption.map(d => d.as[BuildStats]))
    converted
  }
}