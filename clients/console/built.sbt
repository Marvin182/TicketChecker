name := "TicketTestClient"

version := "0.1"

scalaVersion := "2.10.2"

resolvers += "Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/" // Play 2.2-SNAPSHOT

libraryDependencies += "play" %% "play-json" % "2.2-SNAPSHOT"

libraryDependencies += "org.java-websocket" % "Java-WebSocket" % "1.3.0"

javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.6", "-target", "1.6")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions", "-language:postfixOps")

initialCommands in console := """
import de.mritter.ticketchecker.console._
import de.mritter.ticketchecker.api._
val api = new TicketApi
val exampleConnect = "api connect "ws://localhost:9000/api"
val exampleCkeckIn = "api send CheckInTicket(20, "JUg5Gi9e")"
"""