package com.github.skohar.notifytoslack

import com.github.tototoshi.play.json.JsonNaming
import play.api.libs.json.Json

object Config {
  implicit val configFormat = JsonNaming.snakecase(Json.format[Config])
}

case class Config(hostedZoneId: String, hostName: String, ttl: Long)
