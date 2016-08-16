package com.github.skohar.notifytoslack

import cats.data.Xor
import cats.std.all._
import cats.syntax.traverse._
import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model.GetFunctionRequest
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNS
import net.gpedro.integrations.slack.{SlackApi, SlackMessage}
import org.apache.commons.lang3.exception.ExceptionUtils
import play.api.libs.json.Json

import scala.collection.JavaConversions._
import scala.util.control.Exception._

class App {

  def toMessage(sns: SNS): Xor[Throwable, Message] =
    Xor.fromEither(allCatch either Json.parse(sns.getMessage).as[Message])

  def toTextForSlack(message: Message) =
    s"""AutoScalingGroupName: ${message.AutoScalingGroupName}
        |Description: ${message.Description}""".stripMargin

  def handler(event: SNSEvent, context: Context) = {
    val result: String = (for {
      config <- App.description2config(context)
      messages <- (event.getRecords.map(_.getSNS).map(toMessage).toList: List[Xor[Throwable, Message]]).sequenceU
      voids <- (messages.map(toTextForSlack).map(new SlackMessage("AutoScalingGroup", _))
        .map(x => Slack.log(config, x)): List[Xor[Throwable, Unit]]).sequenceU
    } yield {
      voids.mkString(System.lineSeparator)
    }).map(_.toString).leftMap(ExceptionUtils.getStackTrace).leftMap(x => s"""``` $x ```""").merge
    context.getLogger.log(result)
  }
}

object Slack {

  def log(config: LambdaConfig, slackMessage: SlackMessage): Throwable Xor Unit =
    Xor.fromEither(allCatch either new SlackApi(config.slackWebHookUrl).call(slackMessage))

  def log(config: LambdaConfig, username: String, text: String): Throwable Xor Unit =
    log(config, new SlackMessage(username, text))
}

object App {
  def description2config(context: Context): Xor[Throwable, LambdaConfig] = Xor.fromEither(allCatch either {
    val request = new GetFunctionRequest().withFunctionName(context.getFunctionName)
    val description = new AWSLambdaClient().getFunction(request).getConfiguration.getDescription
    context.getLogger.log(description)
    val result = Json.parse(description).as[LambdaConfig]
    Slack.log(result, "Lambda:debug:AutoScalingGroup", "Succeeded in description2config")
    result
  })
}
