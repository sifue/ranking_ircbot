import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import java.util.Properties
import java.io.FileInputStream

object TwisearchIrcbot {
  def main(args: Array[String]) = {
    // load config
    val conf = new Properties
    conf.load(new FileInputStream(if (args.length < 1) "twisearch_ircbot.properties" else args(0)));
    val client = new Client(
        conf.getProperty("irc.address"),
        conf.getProperty("irc.channel"), 
        conf.getProperty("irc.nickname"),
        conf.getProperty("irc.charset"))
    
    val limitCount = conf.getProperty("limitCount").toInt
    val intervalSec = conf.getProperty("intervalSec").toInt
    val keyword = conf.getProperty("keyword")
    val messageFormat = conf.getProperty("messageFormat")
    val noticeFormat = conf.getProperty("noticeFormat")
    val twitter = TwitterFactory.getSingleton
    twitter.setOAuthConsumer(
        conf.getProperty("consumerKey"),
        conf.getProperty("consumerSecret"))
    twitter.setOAuthAccessToken(new AccessToken(
        conf.getProperty("accessToken"),
        conf.getProperty("accessTokenSecret")))
  
    // start TwitterSeacher
    val seacher = new TwitterSeacher(
        client,
        limitCount,
        intervalSec,
        keyword,
        messageFormat,
        noticeFormat,
        twitter)
    seacher.start
    
    val bootNotice = "Irc bot started successfly. args:[limitCount:%1$d, intervalSec:%2$d, keyword:'%3$s']"
    	.format(limitCount, intervalSec, keyword)
    client.sendNotice(bootNotice);
    println(bootNotice)
  } 
}
