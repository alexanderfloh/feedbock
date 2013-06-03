package service

import play.api.libs.iteratee._
import reactivemongo.api._
import reactivemongo.bson._
import scala.concurrent.ExecutionContext.Implicits.global

object MongoService {

  private def connect() = {
    val driver = new MongoDriver
    val connection = driver.connection(List("localhost"))
    val db = connection("feedbock")
    val ret = db("testCases")
    ret
  }
  
  val driverConnection = connect

  def loadTestCase() = {
    val driverConn = connect()
    val query = BSONDocument("_id" -> BSONDocument(
      "suiteName" -> "suiteName",
      "className" -> "className",
      "testName" -> "testName"))
    println("try to load...")
    val driver = new MongoDriver
    val connection = driver.connection(List("localhost"))
    val db = connection("feedbock")
    val coll = db("testCases")
    val cursor = driverConnection.find(query).cursor[BSONDocument]
    cursor.next
  }
  
  def loadTestsCasesSortedByScore {
    
  }
    
}