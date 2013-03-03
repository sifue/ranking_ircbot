import scala.actors.Actor
import scala.actors.threadpool.TimeUnit
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.Query
import scala.collection.JavaConversions._
import twitter4j.Status
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.AtomicLong
import twitter4j.auth.AccessToken

class TwitterSeacher(
    client: Client,
    limitCount: Int,
    intervalSec: Int,
    keyword: String,
    messageFormat: String,
    noticeFormat: String,
    twitter: Twitter) extends Actor {
  val formatter = new SimpleDateFormat("(MM/dd HH:mm)", Locale.JAPAN)
  var maxId = 0L

  def act() = loop {
    val query = new Query
    query.setQuery(keyword)
    query.setSinceId(maxId)
    query.setResultType(Query.RECENT);
    val tweets = twitter.search(query).getTweets().reverse
    if(maxId != 0L) tweets.filter(t => t.getId() > maxId).foreach(sendNoticeToIRC)
    val count = tweets.filter(t => t.getId() > maxId).size
    if(count > limitCount && maxId != 0L) sendMessageToIRC(count)
    tweets.foreach(t => {if(t.getId() > maxId) maxId = t.getId()})
    TimeUnit.SECONDS.sleep(intervalSec)
  }
  
  override def exceptionHandler() = {
    case e : Throwable => {
      e.printStackTrace();
      client.sendNotice(e.toString());
    }
  }
  
  def sendNoticeToIRC(t: Status) {
    val notice = noticeFormat.format(
        t.getUser().getScreenName(),
        t.getText(),
        formatter.format(t.getCreatedAt()))
    client.sendNotice(notice)
  }
  
  def sendMessageToIRC(count: Int) {
    val message = messageFormat.format(intervalSec.toString, keyword, count.toString);
    client.sendMessage(message)
  }
}