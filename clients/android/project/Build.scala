import sbt._
import Keys._
import Defaults._

import org.scalasbt.androidplugin.AndroidPlugin._

object AndroidBuild extends Build {

	val globalSettings = Seq(
		name := "TicketChecker",
		version := "0.1",
		versionCode := 0,

		scalaVersion := "2.10.2",
		platformName := "android-16",

		resolvers ++= Seq(
			"Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/" // Play 2.2-SNAPSHOT
		),
		libraryDependencies ++= Seq(
			"org.scaloid" %% "scaloid" % "2.1-8",
			"play" %% "play-json" % "2.2-SNAPSHOT",
			"org.java-websocket" % "Java-WebSocket" % "1.3.0"
		),

		javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.6", "-target", "1.6"),
		scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions", "-language:postfixOps"),
		
		proguardOptions += "-keep class net.sourceforge.zbar.** { *; }"
	)

	lazy val main = AndroidProject("main", file("."), settings = globalSettings)
}
