package models

import play.api.Play.current
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders._
import mongoContext._

case class TestCase(id: ObjectId, testName: String, className: String, suiteName: String, configurationName: String, status: TestStatus)

object TestCase extends TestCaseDAO with TestCaseJson

trait TestCaseDAO extends ModelCompanion[TestCase, ObjectId] {
  def collection = mongoCollection("testCases")
  val dao = new SalatDAO[TestCase, ObjectId](collection) {}

  // Indexes
  //collection.ensureIndex(DBObject("username" -> 1), "user_email", unique = true)

  // Queries
  /*def findOneByUsername(username: String): Option[User] = dao.findOne(MongoDBObject("username" -> username))
  def findByCountry(country: String) = dao.find(MongoDBObject("address.country" -> country))
  def authenticate(username: String, password: String): Option[User] = findOne(DBObject("username" -> username, "password" -> password))

 
*/}

/**
 * Trait used to convert to and from json
 */
trait TestCaseJson {

  implicit val testCaseJsonWrite = new Writes[TestCase] {
    def writes(u: TestCase): JsValue = {
      Json.obj(
        "id" -> u.id,
        "testName" -> u.testName,
        "className" -> u.className,
        "suiteName" -> u.suiteName,
        "configurationName" -> u.configurationName,
        "status" -> u.status
      )
    }
  }
  //testName: String, className: String, suiteName: String, configurationName: String, status: TestStatus
  implicit val testCaseJsonRead = (
    (__ \ 'id).read[ObjectId] ~
    (__ \ 'testName).read[String] ~
    (__ \ 'className).read[String] ~
    (__ \ 'suiteName).read[String] ~
    (__ \ 'configurationName).read[String] ~
    (__ \ 'status).read[TestStatus]
    
  )(TestCase.apply _)
}
