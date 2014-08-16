import java.util.concurrent.Semaphore

import dispatch._
import dispatch.Defaults._

/**
 * SlackのAPIへの操作を行うクライアント
 * 制限に引っかかららないように1秒間に1回しか実行できない制御をかけてある
 */
object SlackClient {
  val conf = RankingIrcbot.getConf()

  val token = conf.getProperty("slack.token", "")
  val username = conf.getProperty("slack.username", "")
  val iconUrl = conf.getProperty("slack.icon_url", "")

  val available = new Semaphore(1, true)
  val apiLimitWaitMilliSec = 1000L

  def postMessage(message:String, channelName:String): Unit = {
    try {
      available.acquire();
      val request = url("https://slack.com/api/chat.postMessage")
      val requestWithParameters = request
        .POST
        .addParameter("token", token)
        .addParameter("channel", channelName)
        .addParameter("text", message)
        .addParameter("username", username)
        .addParameter("icon_url", iconUrl)
        .secure
      val response = Http(requestWithParameters OK as.String).apply()
      if(response.contains("\"ok\":false")) System.out.println(response)
      Thread.sleep(apiLimitWaitMilliSec)
    } catch {
      case e: Throwable => e.printStackTrace()
    } finally  {
      available.release()
    }
  }
}
