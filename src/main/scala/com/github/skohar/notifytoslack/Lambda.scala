package com.github.skohar.notifytoslack

import cats.data.Xor
import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model.GetFunctionRequest
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.github.skohar.lambda.Slack
import com.github.skohar.notifytoslack.models.LambdaConfig
import org.apache.commons.lang3.exception.ExceptionUtils
import play.api.libs.json.Json

import scala.util.control.Exception._

class Lambda {

  def handler(event: SNSEvent, context: Context): String = (for {
    config <- Lambda.description2config(context)
    r <- Main.run(event, config)
  } yield {
    r
  }).leftMap(ExceptionUtils.getStackTrace).leftMap(x => s"""``` $x ```""").merge
}

object Lambda {
  def description2config(context: Context): Throwable Xor LambdaConfig = Xor.fromEither(allCatch either {
    val request = new GetFunctionRequest().withFunctionName(context.getFunctionName)
    val description = new AWSLambdaClient().getFunction(request).getConfiguration.getDescription
    context.getLogger.log(description)
    val result = Json.parse(description).as[LambdaConfig]
    Slack.log(result, "Lambda:debug:AutoScalingGroup", "Succeeded in description2config")
    result
  })
}
