import com.sorcix.sirc.IrcAdaptor
import com.sorcix.sirc.IrcConnection
import com.sorcix.sirc.User
import com.sorcix.sirc.Channel
import java.nio.charset.Charset

import java.sql.Timestamp
import java.util.Date
import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession


class Client(address: String, channel: String, nickname: String, charset: String) extends IrcAdaptor {
  val irc = new IrcConnection
  irc.setServerAddress(address)
  irc.setCharset(Charset.forName(charset))
  irc.setNick(nickname)
  irc.addServerListener(this)
  irc.addMessageListener(this)  
  irc.connect()

  override def onMessage(irc: IrcConnection, sender: User, target: Channel, message: String) = {

    Database.forURL("jdbc:h2:file:ranking_ircbot", driver = "org.h2.Driver") withSession {
      LogRecord.autoInc.insert(
        ( target.getName,
          sender.getNick,
          "message",
          message,
          new Timestamp(System.currentTimeMillis())))

      val q = for{
        nickname <- Parameters[String]
        t <- LogRecord if t.nickname is nickname
      } yield t.id.count
      val count : Int = q(sender.getNick()).firstOption getOrElse 0
      sendMessage(sender.getNick() + " : " + count)
    }

    // ping
    if (message.contains("ping " + nickname) || message.contains("PING " + nickname)) {
      sendNotice("Working now. > " + sender.getNick())
    }
  }

  override def onConnect(irc: IrcConnection) = {
    irc.createChannel(channel).join()
  }

  def sendMessage(message: String) = {
    irc.createChannel(channel).send(message)
  }
  
  def sendNotice(notice: String) = {
    irc.createChannel(channel).sendNotice(notice)
  }
}