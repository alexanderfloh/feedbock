package controllers

import play.api._
import play.api.mvc._
import models._

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index(TestCase.all))
  }
  
  def foo(buildNumber: Long) = Action {
    Ok(views.html.index(TestCase.findByBuildNumber(buildNumber)))
  }
  
  def bla(status: String) = Action {
    Ok(views.html.index(TestCase.findByStatus(status)))
  }

}