package models

import org.joda.time.DateTime
import reactivemongo.bson._

case class TestCaseFeedback(
  user: String,
  alias: String,
  build: Int,
  timestamp: DateTime,
  defect: Boolean,
  codeChange: Boolean,
  timingIssue: Boolean,
  comment: String) {

  def scoreDelta = {
    var delta = 0
    if (defect) delta += 10
    if (codeChange) delta += 5
    if (timingIssue) delta -= 2
    delta
  }
}

object TestCaseFeedback {
  implicit object BSONDateTimeHandler extends BSONHandler[BSONDateTime, DateTime] {
    def read(time: BSONDateTime) = new DateTime(time.value)
    def write(jdtime: DateTime) = BSONDateTime(jdtime.getMillis)
  }
  
  implicit val handler = Macros.handler[TestCaseFeedback]
}