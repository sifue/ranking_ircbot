import java.util.Properties
import java.io.FileInputStream

object RankingIrcbot {
  def main(args: Array[String]) = {
    // load config
    val conf = new Properties
    conf.load(new FileInputStream(if (args.length < 1) "ranking_ircbot.properties" else args(0)));
    val client = new Client(
        conf.getProperty("irc.address"),
        conf.getProperty("irc.channel"), 
        conf.getProperty("irc.nickname"),
        conf.getProperty("irc.charset"))
    
    val bootNotice = "Irc bot started successfly."
    client.sendNotice(bootNotice);
    println(bootNotice)
  } 
}
