import AssemblyKeys._ // put this at the top of the file

assemblySettings

name := "ranking_ircbot"

version := "2.0"

scalaVersion := "2.10.0"

libraryDependencies ++= List(
  "com.typesafe.slick" %% "slick" % "1.0.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.h2database" % "h2" % "1.3.166"
)

mainClass in assembly := Some("RankingIrcbot")