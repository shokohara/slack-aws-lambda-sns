package com.github.skohar.lambda

import cats.data.Xor
import com.github.skohar.notifytoslack.models.LambdaConfig
import net.gpedro.integrations.slack.{SlackApi, SlackMessage}

import scala.util.control.Exception._

object Slack {

  def log(config: LambdaConfig, slackMessage: SlackMessage): Throwable Xor Unit =
    Xor.fromEither(allCatch either new SlackApi(config.slackWebHookUrl).call(slackMessage))

  def log(config: LambdaConfig, username: String, text: String): Throwable Xor Unit =
    log(config, new SlackMessage(username, text))
}
