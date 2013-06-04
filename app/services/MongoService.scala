package services

import play.api.libs.iteratee._
import reactivemongo.api._
import reactivemongo.bson._
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.api.collections.default.BSONCollection
import models._

object MongoService {
  
  def getDb = {
    import reactivemongo.api._
    import scala.concurrent.ExecutionContext.Implicits.global
	val driver = new MongoDriver
	val connection = driver.connection(List("localhost"))
	connection("feedbock")
  }

  def loadTestCaseByKey(coll: BSONCollection, key: TestCaseKey) = {
    val query = BSONDocument("_id" -> BSONDocument(
      "suiteName" -> key.suiteName,
      "className" -> key.className,
      "testName" -> key.testName))
    val foo = coll.find(query).cursor[TestCase]
    foo.headOption
  }
  
  def loadFailedTestsCasesSortedDescByScore(coll: BSONCollection, build: Int) = {
    val query = BSONDocument(
        "$orderby" -> BSONDocument("score" -> -1),
        "$query" -> BSONDocument("configurations.failed" -> build))
    coll.find(query).cursor[TestCase]
  }
  
  def saveTestCase(coll: BSONCollection, testCase: TestCase) = {
    coll.save(testCase)
  }
    
}