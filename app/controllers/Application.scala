package controllers

import play.api._
import play.api.mvc._
import models._
import views.html.defaultpages.badRequest

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index(TestCase.all))
  }
  
  def viewDetails(id: String) = Action {
    TestCase.getById(id).map{ tc =>
      Ok(views.html.testCaseDetails(tc))
      }.getOrElse(NotFound(""))
    
  }
  
  def load(buildNumber: Int) = Action {
    val testcases = results.Results.loadResultsForBuild(
        results.Build(buildNumber, "http://lnz-bobthebuilder/hudson/job/Trigger%20BVT%20Testset%20AllInOne/" + buildNumber + "/"))
    testcases.foreach(TestCase.save _)
    Ok(views.html.index(testcases.toList))
  }
  
  def bla(status: String) = Action {
    Ok(views.html.index(TestCase.findByStatus(status)))
  }

}