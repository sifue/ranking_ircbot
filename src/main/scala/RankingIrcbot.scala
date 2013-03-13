import java.util.Properties
import java.io.FileInputStream

object RankingIrcbot {
  val configFilepath = "ranking_ircbot.properties"

  def main(args: Array[String]) = {
    val conf = getConfig()
    val client = new Client(
        conf.getProperty("irc.address"),
        conf.getProperty("irc.channel"), 
        conf.getProperty("irc.nickname"),
        conf.getProperty("irc.charset"))
    
    val bootNotice = "Irc bot started successfly."
    client.sendNotice(bootNotice);
    println(bootNotice)
  }

  def getConfig() = {
    val conf = new Properties
    conf.load(new FileInputStream(configFilepath));
    conf
  }

}
