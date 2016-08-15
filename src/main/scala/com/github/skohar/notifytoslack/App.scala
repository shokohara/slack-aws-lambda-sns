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

  def toMessage(sns: SNS): Throwable \/ Message = try {
    \/-(Json.parse(sns.getMessage).as[Message])
  } catch {
    case e: Throwable => -\/(e)
  }

  def handler(event: SNSEvent, context: Context): String = {
    (for {
      config <- App.description2config(context).leftMap(ExceptionUtils.getStackTrace)
    } yield try {
      def toText(message: Message) =
        s"""${message.AutoScalingGroupName}
           |${message.Description}""".stripMargin
      val messages = event.getRecords.map(_.getSNS).map(toMessage).map {
        case -\/(x) => s"""``` ${ExceptionUtils.getStackTrace(x)} ```"""
        case \/-(x) => toText(x)
      }
      messages.map(new SlackMessage("AutoScalingGroup", _)).foreach(new SlackApi(config.slackWebHookUrl).call)
      messages.mkString
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
