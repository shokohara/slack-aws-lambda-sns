package com.github.skohar.notifytoslack

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
import scalaz.Scalaz._
import scalaz._

class App {

//  def toMessage(sns: SNS) = Json.parse(sns.getMessage).as[Message]

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
//      val messages = event.getRecords.map(_.getSNS).map(toMessage).filter(_.NewStateReason === Alarm).map { sns =>
//        s""":exclamation: * ${sns.NewStateValue} : ${sns.AlarmDescription}*
//            |${sns.NewStateReason}""".stripMargin
//      } ++ event.getRecords.map(_.getSNS).map(toMessage).filter(_.NewStateReason === InsufficientData).map { sns =>
//        s""":warning: * ${sns.NewStateValue} : ${sns.AlarmDescription}*
//            |${sns.NewStateReason}""".stripMargin
//      } ++ event.getRecords.map(_.getSNS).map(toMessage).filter(_.NewStateReason === Ok).map { sns =>
//        s""":+1: * ${sns.NewStateValue} : ${sns.AlarmDescription}*
//            |${sns.NewStateReason}""".stripMargin
//      }
//      context.getLogger.log(config.toString)
//      messages.foreach(context.getLogger.log)
//      messages.foreach { message =>
//        new SlackApi(s"https://${config.slackWebHookUrl}").call(new SlackMessage("CloudWatch", message))
//      }
//      messages.mkString
      ""
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
