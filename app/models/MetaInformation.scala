package models

import reactivemongo.bson._
import reactivemongo.bson.Macros.Annotations.Key

case class MetaInformation(@Key("_id") key: String, value: String)

object MetaInformation {
  implicit val handler = Macros.handler[MetaInformation]
}