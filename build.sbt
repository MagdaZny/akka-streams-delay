name := "akka-streams-delay"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.5.9",
  "com.typesafe.akka" %% "akka-actor" % "2.5.9",
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.18",
  "org.scalatest" %% "scalatest" % "3.0.4" % Test,
  "org.mockito" % "mockito-core" % "2.10.0"  % Test,
  "net.manub" %% "scalatest-embedded-kafka" % "1.0.0" % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.9" % Test
)

fork in run := false