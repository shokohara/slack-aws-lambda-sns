package com.github.skohar.notifytoslack

import cats.data.Xor
import cats.std.all._
import cats.syntax.traverse._
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNS
import com.github.skohar.lambda.Slack
import com.github.skohar.notifytoslack.models.{LambdaConfig, Message}
import net.gpedro.integrations.slack.SlackMessage
import play.api.libs.json.Json

import scala.collection.JavaConversions._
import scala.util.control.Exception._


object Main {

  def toMessage(sns: SNS): Throwable Xor Message =
    Xor.fromEither(allCatch either Json.parse(sns.getMessage).as[Message])

  def toTextForSlack(message: Message) =
    s"""AutoScalingGroupName: ${message.AutoScalingGroupName}
        |Description: ${message.Description}""".stripMargin

  def run(event: SNSEvent, config: LambdaConfig): Throwable Xor String = {
    (for {
      messages <- (event.getRecords.map(_.getSNS).map(toMessage).toList: List[Throwable Xor Message]).sequenceU
        .map(_.map(toTextForSlack))
      voids <- (messages.map(new SlackMessage("AutoScalingGroup", _))
        .map(x => Slack.log(config, x)): List[Throwable Xor Unit]).sequenceU
    } yield {
      messages.mkString(System.lineSeparator)
    }).map(_.toString)
  }
}
