package models

import org.joda.time.DateTime
import reactivemongo.bson._

case class User(
  globalId: String,
  alias: String,
  password: String,
  createDate: DateTime)

object User {
  implicit object UserBSONReader extends BSONDocumentReader[User] {
    def read(user: BSONDocument): User = {
      User(
        user.getAs[String]("_id").get,
        user.getAs[String]("alias").get,
        user.getAs[String]("password").get,
        new DateTime(user.getAs[BSONDateTime]("createDate").get)
      )
    }
  }
  implicit object UserBSONWriter extends BSONDocumentWriter[User] {
    def write(user: User): BSONDocument = {
      BSONDocument(
        "_id" -> user.globalId,
        "alias" -> user.alias,
        "password" -> user.password,
        "createDate" -> BSONDateTime(user.createDate.getMillis)
      )
    }
  }
}
