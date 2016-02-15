package com.github.skohar.notifytoslack

import java.nio.ByteBuffer
import java.util.Base64

import cats.std.all._
import cats.syntax.all._
import com.amazonaws.services.kms.AWSKMSClient
import com.amazonaws.services.kms.model.DecryptRequest
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNS
import net.gpedro.integrations.slack.{SlackApi, SlackMessage}
import play.api.libs.json.Json

import scala.collection.JavaConversions._

case class Message(AlarmName: String, AlarmDescription: String, AWSAccountId: String, NewStateValue: String,
                   NewStateReason: String, StateChangeTime: String, Region: String, OldStateValue: String,
                   Trigger: String)
object Message {
  implicit val format = Json.format[Message]
}

class App {
  val encryptedHookUrl = "CiBtXHThDHG4eY1P+iJ2keI45NjH9ijviFAGCv25sGNimBLQAQEBAgB4bVx04QxxuHmNT/oidpHiOOTYx/Yo74hQBgr9ubBjYpgAAACnMIGkBgkqhkiG9w0BBwaggZYwgZMCAQAwgY0GCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQMEDF0UP5wx/XEEwKZAgEQgGABbDD0by8NrS0HI3i0a3nU5dJksHtJtsaZBcwERL/CU8S7uJIPukOPtmC2asZBxiIoUXomXZWiD8Sq5Qp94iljmkr0DQbHfZgT1Cpq4gZfzaz3sBaKRTudJujLwzsoTeg="
  val Alarm = "ALARM"
  val InsufficientData = "INSUFFICIENT_DATA"
  val Ok = "OK"

  def handler(event: SNSEvent, context: Context) {
    val messages = event.getRecords.map(_.getSNS).map(toMessage).filter(_.NewStateReason === Alarm).map { sns =>
      s""":exclamation: * ${sns.NewStateValue} : ${sns.AlarmDescription}*
          |${sns.NewStateReason}
        """.stripMargin
    } ++ event.getRecords.map(_.getSNS).map(toMessage).filter(_.NewStateReason === InsufficientData).map { sns =>
      s""":warning: * ${sns.NewStateValue} : ${sns.AlarmDescription}*
          |${sns.NewStateReason}
        """.stripMargin
    } ++ event.getRecords.map(_.getSNS).map(toMessage).filter(_.NewStateReason === Ok).map { sns =>
      s""":+1: * ${sns.NewStateValue} : ${sns.AlarmDescription}*
          |${sns.NewStateReason}
        """.stripMargin
    }
    val decryptedHookUrl = decrypt(encryptedHookUrl)
    messages.foreach { message =>
      new SlackApi(s"https://$decryptedHookUrl").call(new SlackMessage("CloudWatch", message))
    }
  }

  def toMessage(sns: SNS) = Json.parse(sns.getMessage).as[Message]

  def decrypt(encryptedText: String) = {
    val byteBufferEncryptedText = ByteBuffer.wrap(Base64.getDecoder.decode(encryptedText))
    val decryptRequest = new AWSKMSClient().decrypt(new DecryptRequest().withCiphertextBlob(byteBufferEncryptedText))
    new String(decryptRequest.getPlaintext.array())
  }
}
