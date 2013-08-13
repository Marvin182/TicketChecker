import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

	val appName = "TicketServer"
	val appVersion = "0.1"

	val appDependencies = Seq(
		jdbc,
		"org.squeryl" %% "squeryl" % "0.9.5-6",
		"com.typesafe.akka" %% "akka-actor" % "2.2.0"
	)

	resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

	val main = play.Project(appName, appVersion, appDependencies).settings(
		scalaVersion := "2.10.2",
		scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions", "-language:postfixOps")
	)
}
