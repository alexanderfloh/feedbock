package services.sc

import akka.actor._
import play.Logger

case class LoadResults(build: Int, xml: String)

class SCResultLoadActor extends Actor {
  def receive = {
    case LoadResults(build, xml) => {
      sender ! SCResultParser.parseResultsForRun(xml)
    }
    case _ => {
      Logger.warn("got unknown message")
    }
  }

}