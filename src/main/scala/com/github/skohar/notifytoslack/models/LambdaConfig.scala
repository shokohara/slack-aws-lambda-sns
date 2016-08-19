package com.github.skohar.notifytoslack.models

import com.github.tototoshi.play.json.JsonNaming
import play.api.libs.json.Json

case class LambdaConfig(slackWebHookUrl: String)

object LambdaConfig {
  implicit val format = JsonNaming.snakecase(Json.format[LambdaConfig])
}
