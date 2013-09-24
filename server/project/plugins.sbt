// Comment to get more information during initialization
logLevel := Level.Warn

// Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.0")

// https://github.com/jamesward/play-auto-refresh
// addSbtPlugin("com.jamesward" %% "play-auto-refresh" % "0.0.4")
// => seems like it is not yet available for sbt 0.13