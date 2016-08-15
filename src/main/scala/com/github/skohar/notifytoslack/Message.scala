package com.github.skohar.notifytoslack

import play.api.libs.json.Json

case class Message(
//                    Progress: String,
//                    AccountId: String,
                    Description: String,
//                    RequestId: String,
//                    EndTime: String,
//                    AutoScalingGroupARN: String,
//                    ActivityId: String,
//                    StartTime: String,
//                    Service: String,
//                    Time: String,
                    EC2InstanceId: String,
//                    StatusCode: String,
//                    StatusMessage: String,
//                    Details: String,
                    AutoScalingGroupName: String
//                    Cause: String,
//                    Event: String
                  )

object Message {
  implicit val format = Json.format[Message]
}
