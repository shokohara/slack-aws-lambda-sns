name := "notify-to-slack"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats" % "0.4.1",
  "com.amazonaws" % "aws-lambda-java-core" % "1.0.0",
  "com.amazonaws" % "aws-lambda-java-events" % "1.0.0",
  "com.amazonaws" % "aws-java-sdk-route53" % "1.10.14",
  "com.amazonaws" % "aws-java-sdk-elasticloadbalancing" % "1.10.50",
  "com.typesafe" % "config" % "1.3.0",
  "com.typesafe.play" %% "play-json" % "2.4.6",
  "com.github.tototoshi" %% "play-json-naming" % "1.0.0",
  "net.gpedro.integrations.slack" % "slack-webhook" % "1.1.1",
  "org.slf4j" % "slf4j-api" % "1.7.12",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.scalatest" %% "scalatest" % "3.0.0-M7" % "test"
)
