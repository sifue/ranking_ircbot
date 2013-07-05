import java.util.Properties
import twitter4j.auth.AccessToken
import twitter4j.{ResponseList, TwitterFactory}

class TwitterClient (val conf: Properties){

  val consumerKey = conf.getProperty("twitter.consumer_key")
  val consumerSecret = conf.getProperty("twitter.consumer_secret")
  val accessToken = conf.getProperty("twitter.access_token")
  val accessTokenSecret = conf.getProperty("twitter.access_token_secret")

  val factory = new TwitterFactory()
  val twitter = factory.getInstance()
  twitter.setOAuthConsumer(consumerKey, consumerSecret)
  twitter.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret))

  def getTweet(screenName: String, index : Int): String = {
    val statuses = twitter.getUserTimeline(screenName)
    statuses.get(index).getText
  }
}
