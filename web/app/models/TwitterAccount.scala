package models

import javax.inject.Inject

import scalikejdbc._

case class TwitterAccount(
  id: Long,
  screenName: String,
  accessToken: String,
  accessTokenSecret: String)

object TwitterAccount extends SQLSyntaxSupport[TwitterAccount] {

  override val schemaName = Some("public")

  override val tableName = "twitter_accounts"

  override val columns = Seq("id", "screen_name", "access_token", "access_token_secret")

  def apply(ta: SyntaxProvider[TwitterAccount])(rs: WrappedResultSet): TwitterAccount = apply(ta.resultName)(rs)
  def apply(ta: ResultName[TwitterAccount])(rs: WrappedResultSet): TwitterAccount = new TwitterAccount(
    id = rs.get(ta.id),
    screenName = rs.get(ta.screenName),
    accessToken = rs.get(ta.accessToken),
    accessTokenSecret = rs.get(ta.accessTokenSecret)
  )

  val ta = TwitterAccount.syntax("ta")

  def find(id: Long)(implicit session: DBSession): Option[TwitterAccount] = {
    withSQL {
      select.from(TwitterAccount as ta).where.eq(ta.id, id)
    }.map(TwitterAccount(ta.resultName)).single.apply()
  }

  private def create(
    id: Long,
    screenName: String,
    accessToken: String,
    accessTokenSecret: String)(implicit session: DBSession): TwitterAccount = {
    withSQL {
      insert.into(TwitterAccount).columns(
        column.id,
        column.screenName,
        column.accessToken,
        column.accessTokenSecret
      ).values(
          id,
          screenName,
          accessToken,
          accessTokenSecret
        )
    }.update.apply()

    TwitterAccount(
      id = id,
      screenName = screenName,
      accessToken = accessToken,
      accessTokenSecret = accessTokenSecret)
  }

  private def save(entity: TwitterAccount)(implicit session: DBSession): TwitterAccount = {
    withSQL {
      update(TwitterAccount).set(
        column.id -> entity.id,
        column.screenName -> entity.screenName,
        column.accessToken -> entity.accessToken,
        column.accessTokenSecret -> entity.accessTokenSecret
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def upsert(
    id: Long,
    screenName: String,
    accessToken: String,
    accessTokenSecret: String)(implicit session: DBSession): TwitterAccount = {
    find(id) match {
      case Some(_) =>
        save(TwitterAccount(id, screenName, accessToken, accessTokenSecret))
      case None =>
        create(id, screenName, accessToken, accessTokenSecret)
    }
  }

  def destroy(entity: TwitterAccount)(implicit session: DBSession): Unit = {
    withSQL { delete.from(TwitterAccount).where.eq(column.id, entity.id) }.update.apply()
  }

}

class TwitterAccountRepository @Inject() (connectionPool: ConnectionPool) {

  implicit def session: DBSession = DBSession(conn = connectionPool.borrow())

  def find(id: Long): Option[TwitterAccount] = TwitterAccount.find(id)(session)

  def upsert(id: Long, screenName: String, accessToken: String, accessTokenSecret: String): TwitterAccount = {
    TwitterAccount.upsert(id, screenName, accessToken, accessTokenSecret)(session)
  }

}