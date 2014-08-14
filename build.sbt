import AssemblyKeys._

assemblySettings

name := "ranking_ircbot"

version := "3.0"

scalaVersion := "2.10.2"

libraryDependencies ++= List(
  "com.typesafe.slick" %% "slick" % "1.0.0",
  "org.twitter4j" % "twitter4j-core" % "4.0.2",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.h2database" % "h2" % "1.3.166"
)

mainClass in assembly := Some("RankingIrcbot")
