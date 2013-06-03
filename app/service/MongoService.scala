package service

import play.api.libs.iteratee._
import reactivemongo.api._
import reactivemongo.bson._
import scala.concurrent.ExecutionContext.Implicits.global

object MongoService {
  private def connect() {
    val driver = new MongoDriver
    val connection = driver.connection(List("localhost"))
    val db = connection("feedbock")
    val collection = db("testCases")
  }


  def loadTestCase() {
    val driver = new MongoDriver
    val connection = driver.connection(List("localhost"))
    val db = connection("feedbock")
    val collection = db("testCases")

    val query = BSONDocument("_id" -> BSONDocument(
      "suiteName" -> "suiteName",
      "className" -> "className",
      "testName" -> "testName"))
    println("try to load...")
    val cursor = collection.find(query).cursor[BSONDocument]
    cursor.next
    /*cursor.enumerate.apply(Iteratee.foreach { doc =>
      println("found document: " + BSONDocument.pretty(doc))

      })*/
  }

    
}