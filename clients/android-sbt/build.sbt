import android.Keys._
 
android.Plugin.androidBuild
 
name := "TicketChecker"
 
scalaVersion := "2.10.2"
 
scalacOptions in Compile ++= Seq("-deprecation", "-feature") 
 
proguardOptions in Android ++= Seq("-dontobfuscate"," -dontoptimize")

proguardOptions in Android ++= Seq("-keep public class de.mritter.android.common.log.** {*;}")

proguardOptions in Android ++= Seq("-keep public class de.mritter.ticketchecker.android.** {*;}")

proguardOptions in Android ++= Seq("-keep public class android.support.v4.app.FragmentActivity.* {*;}") // referenced by com.actionbarsherlock.internal.app.ActionBarImpl.selectTab


proguardOptions in Android ++= Seq("-keep public class com.actionbarsherlock.** {*;}")

proguardOptions in Android ++= Seq("-keep public class android.app.** {*;}")


proguardOptions in Android ++= Seq("-keep public class play.api.libs.json.** {*;}")

proguardOptions in Android ++= Seq("-keep public class net.sourceforge.zbar.** {*;}")

proguardOptions in Android ++= Seq("-keep public class scala.collection.mutable.Publisher.** {*;}")

proguardOptions in Android ++= Seq("-keep public class org.java_websocket.client.WebSocketClient.** {*;}")

proguardOptions in Android ++= Seq("-dontwarn **") // just bad, but against the proguard warnings the app runs fine

run <<= run in Android
 
install <<= install in Android

// Play-Json 2.2-SNAPSHOT
resolvers += "Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/"

libraryDependencies += "play" % "play-json_2.10" % "2.2-SNAPSHOT"

libraryDependencies += "org.java-websocket" % "Java-WebSocket" % "1.3.0"

libraryDependencies += "com.actionbarsherlock" % "actionbarsherlock" % "4.4.0"
