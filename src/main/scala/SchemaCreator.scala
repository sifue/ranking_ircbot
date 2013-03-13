import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession

object SchemaCreator {
  def main(args: Array[String]) = {
    Database.forURL("jdbc:h2:file:ranking_ircbot", driver = "org.h2.Driver") withSession {
      (LogRecord.ddl).create
    }
  }
}
