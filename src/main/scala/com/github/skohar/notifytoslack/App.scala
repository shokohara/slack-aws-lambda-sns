package com.github.skohar.notifytoslack

import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model.GetFunctionRequest
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNS
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

  def toMessage(sns: SNS): Seq[(JsPath, Seq[ValidationError])] \/ Message =
    \/.fromEither(Json.parse(sns.getMessage).validate[Message].asEither)

  val Alarm = "ALARM"
  val InsufficientData = "INSUFFICIENT_DATA"
  val Ok = "OK"

  def handler(event: SNSEvent, context: Context): String = {
    context.getLogger.log("helpme")
    App.description2config(context).leftMap(ExceptionUtils.getStackTrace).leftMap(context.getLogger.log)
    (for {
      config <- App.description2config(context).leftMap(ExceptionUtils.getStackTrace)
    } yield try {
      context.getLogger.log("yay1")
      event.getRecords.map(_.getSNS).map(_.getMessage).foreach(context.getLogger.log)
      context.getLogger.log("yay2")
      val emoji = (_: String) match {
        case Alarm => "exclamation"
        case InsufficientData => "warning"
        case Ok => "+1"
      }
      def toText(x: Message) =
        s""":${emoji(x.NewStateReason)}: * ${x.NewStateValue} : ${x.AlarmDescription}*
            |${x.NewStateReason}""".stripMargin
      val messages: Seq[String] = event.getRecords.map(_.getSNS).map(toMessage).map {
        case -\/(x) => s"""``` ${ExceptionUtils.getStackTrace(JsResultException(x))} ```"""
        case \/-(x) => toText(x)
      }
      messages.foreach { message =>
        new SlackApi(s"https://${config.slackWebHookUrl}").call(new SlackMessage("CloudWatch", message))
      }
      messages.mkString
    } catch {
      case t: Throwable =>
        val stackTraceString = ExceptionUtils.getStackTrace(t)
        context.getLogger.log(stackTraceString)
        stackTraceString
    }).toString
  }
}

object App {

  def log(config: LambdaConfig, text: String) =
    new SlackApi(config.slackWebHookUrl).call(new SlackMessage("lambda:debug", s"``` $text ```"))

  def description2config(context: Context) = \/.fromEither(allCatch either {
    val description = new AWSLambdaClient()
      .getFunction(new GetFunctionRequest().withFunctionName(context.getFunctionName)).getConfiguration.getDescription
    Json.parse(description).as[LambdaConfig]
  })
}
