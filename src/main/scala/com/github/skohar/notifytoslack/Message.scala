package com.github.skohar.notifytoslack

import play.api.libs.json.Json

case class Message(AlarmName: String, AlarmDescription: String, AWSAccountId: String, NewStateValue: String,
                   NewStateReason: String, StateChangeTime: String, Region: String, OldStateValue: String,
                   Trigger: String)
object Message {
  implicit val format = Json.format[Message]
}
