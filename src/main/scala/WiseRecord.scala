import java.sql.Timestamp
import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted.Tag

/**
 * 名言のログの一つのログを表すエンティティ
 */
class  WiseRecord(tag: Tag) extends Table[(Int, String, String, String, String, Timestamp)](tag, "WISE_RECORD") {
  def id = column[Int]("WISE_RECORD_ID", O.PrimaryKey, O.AutoInc)
  def channel = column[String]("CHANNEL")
  def nickname = column[String]("NICKNAME")
  def contentType = column[String]("CONTENT_TYPE")
  def content = column[String]("CONTENT")
  def updateAt = column[Timestamp]("UPDATED_AT")
  def * = (id, channel, nickname, contentType, content, updateAt)
  def idx1 = index("channel_updateAt", (channel, updateAt), unique = true)
  def idx2 = index("channel_updateAt_nickname", (channel, updateAt, nickname), unique = true)
}

object WiseRecord extends TableQuery(new WiseRecord(_)) {

}