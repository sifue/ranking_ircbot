import com.sorcix.sirc.IrcAdaptor
import com.sorcix.sirc.IrcConnection
import com.sorcix.sirc.User
import com.sorcix.sirc.Channel
import java.nio.charset.Charset
import scala.actors.Actor 
import com.sorcix.sirc.NickNameException

class Client(address: String, channel: String, nickname: String, charset: String) extends IrcAdaptor {
  val irc = new IrcConnection
  irc.setServerAddress(address)
  irc.setCharset(Charset.forName(charset))
  irc.setNick(nickname)
  irc.addServerListener(this)
  irc.addMessageListener(this)  
  irc.connect()

  override def onMessage(irc: IrcConnection, sender: User, target: Channel, message: String) = {
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