import AssemblyKeys._ // put this at the top of the file

assemblySettings

name := "ranking_ircbot"

version := "2.0"

scalaVersion := "2.10.0"

mainClass in assembly := Some("RankingIrcbot")

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-actors" % _)
