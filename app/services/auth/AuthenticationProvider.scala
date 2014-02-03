package services.auth

import play.Application
import play.api.Play

case class User(val userName: String)

trait AuthenticationProvider {
  def retrieveUser(username: String, password: String): Option[User]
}

object AuthenticationProvider {
  def apply() : AuthenticationProvider = {
    Play.current.configuration.getString("feedbock.authentication.provider").getOrElse("ActiveDirectory") match {
      case "Mock" => new MockAuthenticationProvider
      case "ActiveDirectory" => ActiveDirectoryAuthenticationProvider()
      case _ => throw new RuntimeException("invalid authentication provider name")
    }
  }
}