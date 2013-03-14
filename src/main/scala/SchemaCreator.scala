import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession

object SchemaCreator {
  val conf = RankingIrcbot.getConf()
  val url = conf.getProperty("db.url")
  val driver = conf.getProperty("db.driver")

  def main(args: Array[String]) = {
    Database.forURL(url, driver = driver) withSession {
      (LogRecord.ddl).create
      println((LogRecord.ddl).createStatements)
    }
  }
}
