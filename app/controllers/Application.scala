package controllers

import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import org.joda.time.DateTime
import models.{User, TestCaseFeedback, TestCaseKey, TestCase}
import play.api._
import play.api.data.Form
import play.api.data.Forms.boolean
import play.api.data.Forms.text
import play.api.data.Forms.tuple
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.RequestHeader
import services.MongoService
import scala.concurrent.duration.Duration

object Application extends Controller with Secured {

  val jobUrl = Play.current.configuration.getString("jenkins.jobUrl")

  def index = IsAuthenticated { user => request =>
    Async {
      val buildFuture = MongoService.loadMetaInformation("mostRecentBuildNumber")
      buildFuture.flatMap { buildOpt =>
        buildOpt.map { buildMeta =>
          val build = buildMeta.value.toInt
          for {
            //passedTests <- MongoService.loadPassedTests(build).toList
            failed <- MongoService.loadFailedTestsSortedByScoreDesc(build).toList
          } yield {
            val passedScore = 12345 //passedTests.map(tc => tc.score * tc.passedConfigsForBuild(build).size).sum

            def generateDetailsView(testCase: TestCase, mostRecentBuildNumber: Int) =
              views.html.testCaseDetails(testCase, mostRecentBuildNumber, feedbackForm)

            val failedWithDetails = failed.map(tc => (tc, generateDetailsView(tc, build)))
            val userObj = Await.result(MongoService.loadUser(user), Duration.Inf)
            Ok(views.html.index(passedScore, build, failedWithDetails, userObj.get.alias))
          }
        }.getOrElse(Future(BadRequest("unable to access meta information")))
      }
    }
  }

  /**
   * Handle login form submission.
   */
  def authenticate = Action { implicit request =>
      val (user, password) = signinForm.bindFromRequest.get
      println("user: " + user)
      println("password: " + password)

      Async {
        val userFuture = MongoService.loadUser(user)
        userFuture.flatMap { userOpt =>
          userOpt.map { user =>
            if (user.password == password) {
              Future(Results.Redirect(routes.Application.index).withSession("userId" -> user.globalId))
            } else {
              Future(Results.Redirect(routes.Application.login).withSession("lastLoginFailed" -> "yes"))
            }
          }.getOrElse(Future(Results.Redirect(routes.Application.login).withSession("lastLoginFailed" -> "yes")))
      }
    }
  }

  def login = Action { implicit request =>
    session.get("lastLoginFailed").map { lastLoginFailed =>
      Ok(views.html.signIn(lastLoginFailed == "yes", signinForm))
    }.getOrElse(Ok(views.html.signIn(false, signinForm)))
  }

  def logout = Action {
    Results.Redirect(routes.Application.login).withNewSession
  }

  def submitFeedback(suite: String, className: String, test: String) = IsAuthenticated { user => implicit request =>
    Async {
      val userObj = Await.result(MongoService.loadUser(user), Duration.Inf)
      val (defect, codeChange, timing, comment) = feedbackForm.bindFromRequest.get
      val id = TestCaseKey(suite, className, test)
      for {
        currentBuildOpt <- MongoService.loadMetaInformation("mostRecentBuildNumber")
        testCaseOpt <- MongoService.loadTestCaseByKey(id)
      } yield {
        val actionOpt = for {
          currentBuild <- currentBuildOpt
          testCase <- testCaseOpt
        } yield {
          val feedback = TestCaseFeedback(
            userObj.get.globalId,
            userObj.get.alias,
            currentBuild.value.toInt,
            DateTime.now,
            defect,
            codeChange,
            timing,
            comment)
          val updated = testCase.withFeedback(feedback)
          MongoService.saveTestCase(updated)
          Redirect(routes.Application.index)
        }
        actionOpt.getOrElse(NotFound("invalid test case id"))
      }
    }
  }

  val feedbackForm = Form(
    tuple(
      "defect" -> boolean,
      "codeChange" -> boolean,
      "timing" -> boolean,
      "comment" -> text))

  val signinForm = Form(
    tuple(
      "user" -> text,
      "password" -> text))
}

trait Secured {
  
  private def userId(request: RequestHeader) = request.session.get("userId")

  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.login)

  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(userId, onUnauthorized) { user =>
    Action(request => f(user)(request))
  }

}
