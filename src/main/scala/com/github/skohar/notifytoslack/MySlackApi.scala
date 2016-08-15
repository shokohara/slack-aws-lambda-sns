package com.github.skohar.notifytoslack

import net.gpedro.integrations.slack.{SlackApi, SlackException}
import com.google.gson.JsonObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URL
import java.net.URLEncoder

class MySlackApi(service: String) extends SlackApi(service) {
  val POST: String = "POST"
  val PAYLOAD: String = "payload="
  val UTF_8: String = "UTF-8"
  val proxy: Proxy = Proxy.NO_PROXY
  val timeout: Int = 5000
  def send(message: JsonObject): String = {
    var connection: HttpURLConnection = null
    try {
      // Create connection
      val url: URL = new URL(this.service)
      connection = url.openConnection(proxy).asInstanceOf[HttpURLConnection]
      connection.setRequestMethod(POST)
      connection.setConnectTimeout(timeout)
      connection.setUseCaches(false)
      connection.setDoInput(true)
      connection.setDoOutput(true)
      val payload: String = PAYLOAD + URLEncoder.encode(message.toString, UTF_8)
      // Send request
      val wr: DataOutputStream = new DataOutputStream(connection.getOutputStream)
      wr.writeBytes(payload)
      wr.flush()
      wr.close()
      // Get Response
      val is: InputStream = connection.getInputStream
      val rd: BufferedReader = new BufferedReader(new InputStreamReader(is))
      var line: String = null
      val response: StringBuilder = new StringBuilder
      while ((line = rd.readLine) != null) {
        response.append(line)
        response.append('\n')
      }
      rd.close()
      response.toString
    } catch {
      case e: Exception => throw new SlackException(e)
    } finally if (connection != null) connection.disconnect()
  }
}
