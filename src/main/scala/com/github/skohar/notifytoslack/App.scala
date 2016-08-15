package com.github.skohar.notifytoslack

import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model.GetFunctionRequest
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNS
import com.fasterxml.jackson.core.JsonParseException
import net.gpedro.integrations.slack.{SlackApi, SlackMessage}
import org.apache.commons.lang3.exception.ExceptionUtils
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsError, JsPath, JsResultException, Json}

import scala.collection.JavaConversions._
import scala.collection.Seq
import scala.concurrent.Future
import scala.util.control.Exception._
import scalaz.Scalaz._
import scalaz._
import scalaz._
import syntax.traverse._
import std.scalaFuture._
import std.option._
import syntax.bind._

class App {

  def toMessage(sns: SNS): JsResultException \/ Message = try {
    \/-(Json.parse(sns.getMessage).as[Message])
  } catch {
    case e: (_:JsResultException | _:JsonParseException)=> -\/(e)
  }

  def handler(event: SNSEvent, context: Context): String = {
    (for {
      config <- App.description2config(context).leftMap(ExceptionUtils.getStackTrace)
    } yield try {
      event.getRecords.map(_.getSNS).map(_.getMessage).foreach(App.log(config, _))
      val emoji = (_: String) match {
        case "ALARM" => "exclamation"
        case "INSUFFICIENT_DATA" => "warning"
        case "OK" => "+1"
      }
      def toText(message: Message) =
        s""":${emoji(message.NewStateReason)}: * ${message.NewStateValue} : ${message.AlarmDescription}*
            |${message.NewStateReason}""".stripMargin
      val messages = event.getRecords.map(_.getSNS).map(toMessage).map {
        case -\/(x) => s"""``` ${ExceptionUtils.getStackTrace(x)} ```"""
        case \/-(x) => toText(x)
      }
      messages.map(new SlackMessage("CloudWatch", _)).foreach(new SlackApi(s"https://${config.slackWebHookUrl}").call)
      messages.mkString
      ""
    } catch {
      case t: Throwable =>
        val stackTraceString = ExceptionUtils.getStackTrace(t)
        App.log(config, stackTraceString)
        stackTraceString
    }).toString
  }
}

object App {

  def log(config: LambdaConfig, text: String) =
    new SlackApi(config.slackWebHookUrl).call(new SlackMessage("lambda:debug", s"``` $text ```"))

  def description2config(context: Context) = \/.fromEither(allCatch either {
    val request = new GetFunctionRequest().withFunctionName(context.getFunctionName)
    val description = new AWSLambdaClient().getFunction(request).getConfiguration.getDescription
    Json.parse(description).as[LambdaConfig]
  })
}
