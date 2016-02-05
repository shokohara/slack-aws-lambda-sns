package com.github.skohar.notifytoslack

import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.Json

class MySpec extends FlatSpec with Matchers {

//  val snsExample =
//    """{
//      |  "Records": [
//      |    {
//      |      "EventVersion": "1.0",
//      |      "EventSubscriptionArn": "arn:aws:sns:EXAMPLE",
//      |      "EventSource": "aws:sns",
//      |      "Sns": {
//      |        "SignatureVersion": "1",
//      |        "Timestamp": "1970-01-01T00:00:00.000Z",
//      |        "Signature": "EXAMPLE",
//      |        "SigningCertUrl": "EXAMPLE",
//      |        "MessageId": "95df01b4-ee98-5cb9-9903-4c221d41eb5e",
//      |        "Message": "Hello from SNS!",
//      |        "MessageAttributes": {
//      |          "Test": {
//      |            "Type": "String",
//      |            "Value": "TestString"
//      |          },
//      |          "TestBinary": {
//      |            "Type": "Binary",
//      |            "Value": "TestBinary"
//      |          }
//      |        },
//      |        "Type": "Notification",
//      |        "UnsubscribeUrl": "EXAMPLE",
//      |        "TopicArn": "arn:aws:sns:EXAMPLE",
//      |        "Subject": "TestInvoke"
//      |      }
//      |    }
//      |  ]
//      |}
//    """.stripMargin
//
//  "a" should "not equals to b" in {
//    val testString =
//      """
//        |{
//        |  Records:[
//        |    {
//        |      EventSource:'aws:sns',
//        |      EventVersion:'1.0',
//        |      EventSubscriptionArn:'arn:aws:sns:us-east-1:695752756382:sandbox:d8353073-b8aa-405b-92ac-bc4d80677755',
//        |      Sns:[
//        |        {
//        |  AlarmName:'awselb-whale-dev-High-Healthy-Hosts',
//        |  AlarmDescription:'Created from EC2 Console',
//        |  AWSAccountId:'695752756382',
//        |  NewStateValue:'OK',
//        |  NewStateReason:'Threshold Crossed: 1 datapoint (1.0) was not less than the threshold (1.0).',
//        |  StateChangeTime:'2016-02-04T05:26:51.631+0000',
//        |  Region:'US - N. Virginia',
//        |  OldStateValue:'INSUFFICIENT_DATA',
//        |  Trigger:{
//        |    MetricName:'HealthyHostCount',
//        |    Namespace:'AWS/ELB',
//        |    Statistic:'AVERAGE',
//        |    Unit:null,
//        |    Dimensions:[
//        |      [
//        |        Object
//        |      ]
//        |    ],
//        |    Period:60,
//        |    EvaluationPeriods:1,
//        |    ComparisonOperator:'LessThanThreshold',
//        |    Threshold:1
//        |  }
//        |}
//        |      ]
//        |    }
//        |  ]
//        |}
//      """.stripMargin
//    Json.parse(testString).as[Event]
//    noException should be thrownBy Json.parse(testString).as[Event]
//  }
}
