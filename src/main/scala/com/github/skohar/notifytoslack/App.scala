package com.github.skohar.notifytoslack

import java.nio.ByteBuffer
import java.util.Base64

import com.amazonaws.services.kms.AWSKMSClient
import com.amazonaws.services.kms.model.DecryptRequest
import com.amazonaws.services.lambda.runtime.Context
import net.gpedro.integrations.slack.{SlackApi, SlackMessage}
import play.api.libs.json.Json

case class Message(AlermName: String, AlarmDescription: String, AWSAccountId: String, NewStateValue: String,
                   NewStateReason: String, StateChangeTime: String, Region: String, OldStateValue: String,
                   Trigger: String)
case class Sns(Message: Message)
case class Record(EventVersion: String, EventSubscriptionArn: String, EventSource: String, Sns: Option[Sns])
case class Event(Records: List[Record])
object Message {
  implicit val format = Json.format[Message]
}
object Sns {
  implicit val format = Json.format[Sns]
}
object Record {
  implicit val format = Json.format[Record]
}
object Event {
  implicit val format = Json.format[Event]
}

class App {
  val encryptedHookUrl = "CiBtXHThDHG4eY1P+iJ2keI45NjH9ijviFAGCv25sGNimBLQAQEBAgB4bVx04QxxuHmNT/oidpHiOOTYx/Yo74hQBgr9ubBjYpgAAACnMIGkBgkqhkiG9w0BBwaggZYwgZMCAQAwgY0GCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQMEDF0UP5wx/XEEwKZAgEQgGABbDD0by8NrS0HI3i0a3nU5dJksHtJtsaZBcwERL/CU8S7uJIPukOPtmC2asZBxiIoUXomXZWiD8Sq5Qp94iljmkr0DQbHfZgT1Cpq4gZfzaz3sBaKRTudJujLwzsoTeg="

  def handler(event: String, context: Context) {
    for {
      event <- Json.parse(event).asOpt[Event]
    } yield {
      val messages = event.Records.flatMap(_.Sns).filter(_.Message.NewStateValue == "ALERM").map { sns =>
        s""":exclamation: * ${sns.Message.NewStateValue} : ${sns.Message.AlarmDescription}*
            |${sns.Message.NewStateReason}
        """.stripMargin
      } ::: event.Records.flatMap(_.Sns).filter(_.Message.NewStateValue == "INSUFFICIENT_DATA").map { sns =>
        s""":warning: * ${sns.Message.NewStateValue} : ${sns.Message.AlarmDescription}*
            |${sns.Message.NewStateReason}
        """.stripMargin
      } ::: event.Records.flatMap(_.Sns).filter(_.Message.NewStateValue == "OK").map { sns =>
        s""":+1: * ${sns.Message.NewStateValue} : ${sns.Message.AlarmDescription}*
            |${sns.Message.NewStateReason}
        """.stripMargin
      }
      val api = new SlackApi("https://" + decrypt(encryptedHookUrl))
      messages.foreach { message =>
        api.call(new SlackMessage("CloudWatch", message))
      }
    }
  }

  def decrypt(encryptedText: String) = {
    val byteBufferEncryptedText = ByteBuffer.wrap(Base64.getDecoder.decode(encryptedText))
    val decryptRequest = new AWSKMSClient().decrypt(new DecryptRequest().withCiphertextBlob(byteBufferEncryptedText))
    new String(decryptRequest.getPlaintext.array())
  }
}
