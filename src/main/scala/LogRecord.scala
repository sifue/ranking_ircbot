import java.sql.Timestamp
import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession
/**
 * IRCの一つのログを表すエンティティ
 */
object LogRecord extends Table[(Int, String, String, String, String, Timestamp)]("LOG_RECORD") {
  def id = column[Int]("LOG_RECORD_ID", O.PrimaryKey, O.AutoInc)
  def channel = column[String]("CHANNEL")
  def nickname = column[String]("NICKNAME")
  def contentType = column[String]("CONTENT_TYPE")
  def content = column[String]("CONTENT")
  def updateAt = column[Timestamp]("UPDATED_AT")
  def * = id ~ channel ~ nickname ~ contentType ~ content ~ updateAt
  def autoInc = channel ~ nickname ~ contentType ~ content ~ updateAt returning id
  def idx1 = index("channel_updateAt", (channel, updateAt), unique = true)
  def idx2 = index("channel_updateAt_nickname", (channel, updateAt, nickname), unique = true)
}
