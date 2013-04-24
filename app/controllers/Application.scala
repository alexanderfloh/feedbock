package controllers

import play.api._
import play.api.mvc._
import models._

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index(TestCase.all))
  }
  
  def viewDetails(id: String) = TODO
  
  def load() = Action {
    val testcases = results.Results.loadResultsForBuild(results.Build(842, "http://lnz-bobthebuilder/hudson/job/Trigger%20BVT%20Testset%20AllInOne/842/"))
    Ok(views.html.index(testcases.toList))
    //Ok(views.html.index(TestCase.findByBuildNumber(buildNumber)))
  }
  
  def bla(status: String) = Action {
    Ok(views.html.index(TestCase.findByStatus(status)))
  }

}