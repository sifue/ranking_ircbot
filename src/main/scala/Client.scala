import com.sorcix.sirc.IrcAdaptor
import com.sorcix.sirc.IrcConnection
import com.sorcix.sirc.User
import com.sorcix.sirc.Channel
import java.nio.charset.Charset

import java.sql.Timestamp
import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession

class Client extends IrcAdaptor {
  val conf = RankingIrcbot.getConf()
  val url = conf.getProperty("db.url")
  val driver = conf.getProperty("db.driver")

  val address = conf.getProperty("irc.address")
  val channels = conf.getProperty("irc.channel").split(" ")
  val nickname = conf.getProperty("irc.nickname")
  val charset = conf.getProperty("irc.charset")

  val irc = new IrcConnection
  irc.setServerAddress(address)
  irc.setCharset(Charset.forName(charset))
  irc.setNick(nickname)
  irc.addServerListener(this)
  irc.addMessageListener(this)  
  irc.connect()

  override def onMessage(irc: IrcConnection, sender: User, target: Channel, message: String) = {
    handleLog(target, sender, "message", message)
    if (message.contains("hourlyranking>")) sendRankingHour(target)
    if (message.contains("daylyranking>")) sendRankingDay(target)
    if (message.contains("weeklyranking>")) sendRankingWeek(target)
    if (message.contains("monthlyranking>")) sendRankingMonth(target)
    if (message.contains("yearlyranking>")) sendRankingYear(target)
    if (message.contains("ping " + nickname)) sendNotice("Working now. > " + sender.getNick(), target.getName)
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
    Database.forURL(url, driver = driver) withSession {
      val q = (for {r <- LogRecord} yield r)
        .where(_.channel is target.getName)
        .where(_.updateAt >= countedDate)
        .groupBy(_.nickname)
      val qGroup = q.map {
        case (nickname, grouped) => (nickname, grouped.length)
      }
      val qSort = qGroup.sortBy(_._2.desc)

      val b = new StringBuilder
      b.append(target.getName + "の" + title + " ")
      qSort.list().zipWithIndex.foreach {
        r =>
          b.append("第%1$d位 %2$s %3$d回, ".format(r._2 + 1, r._1._1, r._1._2))
      }
      b.deleteCharAt(b.length - 1)
      b.deleteCharAt(b.length - 1)
      sendNotice(b.toString(), target.getName())
    }
  }

  override def onNotice(irc: IrcConnection, sender: User, target: Channel, message: String) = {
    handleLog(target, sender, "notice", message)
  }

  private def handleLog(target: Channel, sender: User, contentType: String, message: String) {
    Database.forURL(url, driver = driver) withSession {
      LogRecord.autoInc.insert(
        (target.getName,
          sender.getNick,
          contentType,
          message,
          new Timestamp(System.currentTimeMillis())))
    }
  }

  override def onConnect(irc: IrcConnection) = {
    channels.foreach{irc.createChannel(_).join()}
  }

  // 自分がオペレーターなら参加者全員にオペレータ権限を与える
  override def onJoin(irc: IrcConnection, channel: Channel, user: User) = {
    channel.giveOperator(user)
  }

  def sendMessage(message: String, channelName: String) = {
    irc.createChannel(channelName).send(message)
  }
  
  def sendNotice(notice: String, channelName: String) = {
    irc.createChannel(channelName).sendNotice(notice)
  }

}