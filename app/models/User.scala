package models

import org.jboss.netty.buffer._
import org.joda.time.DateTime
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._
import reactivemongo.bson._

case class User(
  globalId: String,
  alias: String,
  emailAdresses: List[UserEmail],
  passwordAuthentication: Option[UserPasswordAuthentication],
  isAdmin: Bool,
  sessions: List[UserSession],
  createDate: DateTime)

case class UserPasswordAuthentication(
  password: String)

case class UserSession(
  sessionId: String,
  createDate: DateTime,
  lastActivity: DateTime)

case class UserEmail(
  email: String)