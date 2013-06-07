import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "feedbock"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    //jdbc,
    //anorm,
    //"org.reactivemongo" %% "reactivemongo" % "0.9",
    "org.reactivemongo" %% "play2-reactivemongo" % "0.9"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here     
    resolvers += Resolver.sonatypeRepo("snapshots")
  )

}
