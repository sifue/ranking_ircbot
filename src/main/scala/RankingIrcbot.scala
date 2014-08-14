import java.util.{Date, Properties}
import java.io.FileInputStream

object RankingIrcbot {
  val configFilepath = "ranking_ircbot.properties"
  val conf = new Properties
  conf.load(new FileInputStream(configFilepath));

  def main(args: Array[String]) = {
    new IrcClient
    println(s"RankingIrcbot booted at ${new Date().toString}")
  }

  def getConf() = {
    conf
  }

}
