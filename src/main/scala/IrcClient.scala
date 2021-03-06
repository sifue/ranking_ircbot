import com.sorcix.sirc.IrcAdaptor
import com.sorcix.sirc.IrcConnection
import com.sorcix.sirc.User
import com.sorcix.sirc.Channel
import java.nio.charset.Charset

import java.sql.Timestamp
import java.util.Date
import scala.slick.driver.H2Driver.simple._
import scala.util.matching.Regex
import scala.util.Random

class IrcClient extends IrcAdaptor {
  val conf = RankingIrcbot.getConf()
  val url = conf.getProperty("db.url")
  val driver = conf.getProperty("db.driver")

  val address = conf.getProperty("irc.address")
  val channels = conf.getProperty("irc.channel").split(" ")
  val nickname = conf.getProperty("irc.nickname")
  val charset = conf.getProperty("irc.charset", "UTF-8")
  val username = conf.getProperty("irc.username", "")
  val password = conf.getProperty("irc.password", "")
  val port = conf.getProperty("irc.port", "6667").toInt
  val useSSL = conf.getProperty("irc.use_ssl", "false").toBoolean
  val useSlackPost = conf.getProperty("irc.use_slack_post", "false").toBoolean
  val useNameSlashnize = conf.getProperty("irc.use_name_slashnize", "false").toBoolean

  val random = new Random()
  val detail = "https://github.com/sifue/ranking_ircbot"

  val twitterEnable = conf.getProperty("twitter.enable").toBoolean
  var twitterClient = if (twitterEnable ) new TwitterClient(conf) else null;

  private var irc: IrcConnection = null

  private def initConnection: Unit = {
    this.irc = new IrcConnection(address, port, password)
    this.irc.setCharset(Charset.forName(charset))
    this.irc.setNick(nickname)
    this.irc.setUsingSSL(useSSL)
    this.irc.setUsername(username)
    this.irc.addServerListener(this)
    this.irc.addMessageListener(this)
  }

  def connect: Unit = {
    initConnection
    if(useSSL) {
      irc.connect(TrustManagerIgnoreService.getIgnoreSSLContext)
    } else {
      irc.connect()
    }
  }

  override def onMessage(irc: IrcConnection, sender: User, target: Channel, message: String) = {
    if(channels.contains(target.getName)) { // 設定されたチャンネルにいる時だけ反応
      try {
        val trimmedMessage = message.trim
        handleLog(target, sender, "message", trimmedMessage)
        if (message.startsWith("@")) sendTweet(target, trimmedMessage)
        if (message.startsWith("hourlyranking>")) sendRankingHour(target)
        if (message.startsWith("dailyranking>")) sendRankingDay(target)
        if (message.startsWith("weeklyranking>")) sendRankingWeek(target)
        if (message.startsWith("monthlyranking>")) sendRankingMonth(target)
        if (message.startsWith("yearlyranking>")) sendRankingYear(target)
        if (message.startsWith("wiseranking>")) sendRankingWise(target)
        if (message.endsWith("曰く")) sendWise(target, trimmedMessage)
        if (message.startsWith("覚えろ:")) handleWise(target, sender, "message", message.trim)
        if (message.startsWith("消して:")) handleWiseDelete(target, sender, "message", trimmedMessage)
        if (message.startsWith("ping " + nickname)) sendNotice("Working now. > " + sender.getNick() + " " + detail, target.getName)
      } catch {
        case e: Throwable =>
          e.printStackTrace()
          sendMessage("例外発生: " + e.getMessage, target.getName)
      }
    }
  }

  def sendRankingHour(target: Channel) {
    val title = "1時間の発言数ランキング"
    val countedDate = new Timestamp(System.currentTimeMillis() - (1000 * 60 * 60));
    sendRanking(target, countedDate, title)
  }

  def sendRankingDay(target: Channel) {
    val title = "24時間の発言数ランキング"
    val countedDate = new Timestamp(System.currentTimeMillis() - (1000 * 60 * 60 * 24));
    sendRanking(target, countedDate, title)
  }

  def sendRankingWeek(target: Channel) {
    val title = "1週間の発言数ランキング"
    val countedDate = new Timestamp(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 7));
    sendRanking(target, countedDate, title)
  }

  def sendRankingMonth(target: Channel) {
    val title = "30日間の発言数ランキング"
    val countedDate = new Timestamp(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 7 * 30));
    sendRanking(target, countedDate, title)
  }

  def sendRankingYear(target: Channel) {
    val title = "1年間の発言数ランキング"
    val countedDate = new Timestamp(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 7 * 365));
    sendRanking(target, countedDate, title)
  }

  private def sendRanking(target: Channel, countedDate: Timestamp, title: String) {
    Database.forURL(url, driver = driver) withSession { implicit session =>
      val q = (for {r <- LogRecord} yield r)
        .filter(_.channel === target.getName)
        .filter(_.updateAt >= countedDate)
        .groupBy(_.nickname)
      val qGroup = q.map {
        case (nickname, grouped) => (nickname, grouped.length)
      }
      val qSort = qGroup.sortBy(_._2.desc)

      val b = new StringBuilder
      b.append(target.getName + "の" + title + " ")
      val list = qSort.list
      var preRank = 1
      var preCount = 0
      list.zipWithIndex.foreach {
        r =>
          val name = r._1._1
          val rank = r._2 + 1
          val count = r._1._2
          val sendRank = if(count == preCount){preRank}else{rank}
          b.append("%1$d:%2$s %3$d回, ".format(sendRank, slashnize(name), count))
          preRank = sendRank
          preCount =  count
          // 10個 区切りまたは最後なら送信
          if(rank % 10 == 0 || rank == list.size) {
            b.deleteCharAt(b.length - 1)
            b.deleteCharAt(b.length - 1)
            sendNotice(b.toString(), target.getName())
            b.clear()
          }
      }
    }
  }

  private def sendRankingWise(target: Channel) {
    val title = "名言登録数のランキング"
    Database.forURL(url, driver = driver) withSession { implicit session =>
      val q = (for {r <- WiseRecord} yield r)
        .filter(_.channel === target.getName)
        .groupBy(_.nickname)
      val qGroup = q.map {
        case (nickname, grouped) => (nickname, grouped.length)
      }
      val qSort = qGroup.sortBy(_._2.desc)
      val list = qSort.list

      if (list.length == 0) {
        sendNotice(target.getName + "の登録名言はありません", target.getName())
        return
      }

      val b = new StringBuilder
      b.append(target.getName + "の" + title + " ")
      var preRank = 1
      var preCount = 0
      list.zipWithIndex.foreach {
        r =>
          val name = r._1._1
          val rank = r._2 + 1
          val count = r._1._2
          val sendRank = if(count == preCount){preRank}else{rank}
          b.append("%1$d:%2$s %3$d個, ".format(sendRank, slashnize(name), count))
          preRank = sendRank
          preCount =  count
          // 10個 区切りまたは最後なら送信
          if(rank % 10 == 0 || rank == list.size) {
            b.deleteCharAt(b.length - 1)
            b.deleteCharAt(b.length - 1)
            sendNotice(b.toString(), target.getName())
            b.clear()
          }
      }
    }
  }

  /**
   * 与えられた文字列の最初の文字の後にサイズゼロスペースを入れる
   * @param string
   * @return
   */
  private def slashnize(string: String):String = {
    if (string.size > 1 && useNameSlashnize) {
      string.head + "\u200B" + string.tail
    } else {
      string
    }
  }

  private def sendTweet(target: Channel, message: String) {
    if (twitterClient != null) {
      val p : Regex = "@([0-9A-Za-z_]+)(| ([0-9]+))".r;
      val (screenName, tweet) = message match {
        case p(screenName, after, index) => {
          if(index != null && index.toInt < 20) {
            (screenName, twitterClient.getTweet(screenName, index.toInt))
          } else if(index != null && index.toInt >= 20) {
            ("例外発生", "20個以上前のtweetは参照できません")
          } else {
            (screenName, twitterClient.getTweet(screenName, 0))
          }
        }
        case _ => ("", "")
      }
      if(!tweet.isEmpty) sendNotice(screenName + ": " + tweet.replaceAll("\n", ""), target.getName)
    }
  }

  private def sendWise(target: Channel, message: String) {
    val p : Regex = "([^曰]*)曰く".r;
    message match {case p(nickname) =>
      Database.forURL(url, driver = driver) withSession { implicit session =>
        val trimmedNickname = nickname.trim
        val q = (for {r <- WiseRecord} yield r)
          .filter(_.channel === target.getName)
          .filter(_.nickname === trimmedNickname)
        val length: Int = q.list.length
        if (length == 0) {
          sendNotice(trimmedNickname + "の発言は登録されていません", target.getName)
          return
        }
        var wiseMessage = random.shuffle(q.list).head
        if (wiseMessage._5.isEmpty) return;
        sendNotice(slashnize(trimmedNickname) + ": " + wiseMessage._5, target.getName)
      }
    }
  }

  override def onNotice(irc: IrcConnection, sender: User, target: Channel, message: String) = {
    try {
      handleLog(target, sender, "notice", message)
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        sendMessage("例外発生: " + e.getMessage, target.getName)
    }
  }

  private def handleLog(target: Channel, sender: User, contentType: String, message: String) {
    Database.forURL(url, driver = driver) withSession { implicit session =>
      LogRecord.map(c => (c.channel, c.nickname, c.contentType, c.content, c.updateAt)) +=
        (target.getName,
          sender.getNick,
          contentType,
          message,
          new Timestamp(System.currentTimeMillis()))
    }
  }

  private def handleWise(target: Channel, sender: User, contentType: String, message: String) {
    val p : Regex = "^.*覚えろ:([^ ]+) (.+)$".r;
    message match {case p(nickname, wiseMessage) =>
      val trimmedNickname = nickname.trim
      val trimmedWiseMessage = wiseMessage.trim
      if (trimmedNickname.isEmpty || trimmedWiseMessage.isEmpty) return
      Database.forURL(url, driver = driver) withSession { implicit session =>
        WiseRecord.map(c => (c.channel, c.nickname, c.contentType, c.content, c.updateAt)) +=
          (target.getName,
            trimmedNickname,
            contentType,
            trimmedWiseMessage,
            new Timestamp(System.currentTimeMillis()))
        sendNotice((slashnize(trimmedNickname) + ": " + trimmedWiseMessage + " を覚えました"), target.getName)
      }
    }
  }

  private def handleWiseDelete(target: Channel, sender: User, contentType: String, message: String) {
    val p : Regex = "^.*消して:([^ ]+) (.+)$".r;
    message match {case p(nickname, wiseMessage) =>
      val trimmedNickname = nickname.trim
      val trimmedWiseMessage = wiseMessage.trim
      if (trimmedNickname.isEmpty || trimmedWiseMessage.isEmpty) return
      Database.forURL(url, driver = driver) withSession { implicit session =>
        val q = (for {r <- WiseRecord} yield r)
          .filter(_.channel === target.getName)
          .filter(_.nickname === trimmedNickname)
          .filter(_.content === trimmedWiseMessage)
        q.delete
        sendNotice((slashnize(trimmedNickname) + ": " + trimmedWiseMessage + " を消しました"), target.getName)
      }
    }
  }


  override def onConnect(irc: IrcConnection) = {
    channels.foreach{irc.createChannel(_).join()}
  }

  val reconnectWaitMilliSec = 3000
  override def onDisconnect(irc: IrcConnection) = {
    try {
      connect
    } catch {
      case e: Throwable => e.printStackTrace()
    }
    System.err.println(
      s"Unexpected disconnection and reconnection. (${address}:${port.toString}) at ${new Date().toString}")
    while (!this.irc.isConnected) {
      Thread.sleep(reconnectWaitMilliSec)
      try {
        connect
      } catch {
        case e: Throwable => e.printStackTrace()
      }
      System.err.println(
        s"Unexpected disconnection and retry reconnection. (${address}:${port.toString}) at ${new Date().toString}")
    }
  }

  // 自分がオペレーターなら参加者全員にオペレータ権限を与える
  override def onJoin(irc: IrcConnection, channel: Channel, user: User) = {
    try {
      channel.giveOperator(user)
    } catch { case e : Throwable =>
      e.printStackTrace()
      sendMessage("例外発生: " + e.getMessage, channel.getName )
    }
  }

  // 蹴られても帰ってくる
  override def onKick(irc: IrcConnection, channel: Channel, sender: User, user: User, message: String) = {
    try {
      irc.createChannel(channel.getName).join()
    } catch { case e : Throwable =>
      e.printStackTrace()
      sendMessage("例外発生: " + e.getMessage, channel.getName )
    }
  }

  def sendMessage(message: String, channelName: String) = {
    if(useSlackPost) {
      SlackClient.postMessage(message, channelName)
    } else {
      message.grouped(400).foreach(s => irc.createChannel(channelName).send(s.trim + " "))
    }
  }
  
  def sendNotice(notice: String, channelName: String) = {
    if(useSlackPost) {
      SlackClient.postMessage(notice, channelName)
    } else {
      notice.grouped(400).foreach(s => irc.createChannel(channelName).sendNotice(s.trim + " "))
    }
  }
}
