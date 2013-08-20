import android.Keys._
 
android.Plugin.androidBuild
 
name := "TicketChecker"
 
scalaVersion := "2.10.2"
 
scalacOptions in Compile ++= Seq("-deprecation", "-feature")
 
proguardOptions in Android ++= Seq("-dontobfuscate"," -dontoptimize")

proguardOptions in Android ++= Seq("-keep public class play.api.libs.json.** {*;}")

proguardOptions in Android ++= Seq("-keep public class net.sourceforge.zbar.** {*;}")

proguardOptions in Android ++= Seq("-dontwarn **") // just bad, but against the proguard warnings the app runs fine

run <<= run in Android
 
install <<= install in Android

// Play-Json 2.2-SNAPSHOT
resolvers += "Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/"

libraryDependencies += "play" % "play-json_2.10" % "2.2-SNAPSHOT"

libraryDependencies += "org.java-websocket" % "Java-WebSocket" % "1.3.0"
