name := "notify-to-slack"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-lambda" % "1.11.26",
  "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
  "com.amazonaws" % "aws-lambda-java-events" % "1.1.0",
  "com.typesafe.play" %% "play-json" % "2.5.4",
  "com.github.tototoshi" %% "play-json-naming" % "1.1.0",
  "net.gpedro.integrations.slack" % "slack-webhook" % "1.2.0",
  "org.typelevel" %% "cats" % "0.4.1",
  "org.scalaz" %% "scalaz-core" % "7.2.5",
  "org.apache.commons" % "commons-lang3" % "3.4",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)
