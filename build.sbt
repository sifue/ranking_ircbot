import AssemblyKeys._

assemblySettings

name := "ranking_ircbot"

version := "4.0"

scalaVersion := "2.11.2"

libraryDependencies ++= List(
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "com.sorcix" % "sirc" % "1.1.5",
  "org.twitter4j" % "twitter4j-core" % "4.0.2",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
  "com.h2database" % "h2" % "1.3.166"
)

mainClass in assembly := Some("RankingIrcbot")

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
{
  case PathList("com", "sorcix", "sirc", xs @ _*)  => MergeStrategy.first
  case x => old(x)
}
}

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

scalacOptions += "-target:jvm-1.7"
