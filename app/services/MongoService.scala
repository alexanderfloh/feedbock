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
import scala.concurrent.Await
import scala.concurrent.duration._
import play.Logger

object MongoService {
  /** Returns the default database (as specified in `application.conf`). */
  private def db = ReactiveMongoPlugin.db

  private def testCases = db[BSONCollection]("testCases")
  private def metaInformation = db[BSONCollection]("metaInformation")
  private def users = db[BSONCollection]("users")
  private def buildStats = db[BSONCollection]("buildStats")

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
  
  def saveBuildStats(doc: BuildStats) = buildStats.save(doc) 
  
  def loadAllStats() = {
    val query = BSONDocument(
        "$query" -> BSONDocument(), 
        "$orderby" -> BSONDocument("_id" -> -1))
    buildStats.find(query).cursor[BuildStats]
  }
  
  def testCalc(fromBuild: Int, toBuild: Int) = {
    for(build <- fromBuild to toBuild) yield {
      val res = Await.result(calcScoreForBuild(build), 5 minutes)
      Logger.info(s"result $res")
      for(stat <- res) yield saveBuildStats(stat)
      "ok"
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