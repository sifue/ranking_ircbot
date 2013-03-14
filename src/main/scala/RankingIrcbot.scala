import java.util.Properties
import java.io.FileInputStream

object RankingIrcbot {
  val configFilepath = "ranking_ircbot.properties"
  val conf = new Properties
  conf.load(new FileInputStream(configFilepath));

  def main(args: Array[String]) = {
    val client = new Client
    val bootNotice = "ランキングボットが起動しました"
    println(bootNotice)
  }

  def getConf() = {
    conf
  }

}
