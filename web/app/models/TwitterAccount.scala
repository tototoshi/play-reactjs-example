package models

import javax.inject.Inject

import scalikejdbc._

case class TwitterAccount(
  id: Long,
  screenName: String,
  accessToken: String,
  accessTokenSecret: String)

class TwitterAccountRepository @Inject() (connectionPool: ConnectionPool) {

  private def db: DB = DB(connectionPool.borrow())

  private def *(rs: WrappedResultSet): TwitterAccount = TwitterAccount(
    id = rs.long("id"),
    screenName = rs.string("screen_name"),
    accessToken = rs.string("access_token"),
    accessTokenSecret = rs.string("access_token_secret")
  )

  def find(id: Long): Option[TwitterAccount] = db.readOnly { implicit session =>
    sql"select * from twitter_accounts where id = $id"
      .map(*)
      .single()
      .apply()
  }

  def upsert(id: Long, screenName: String, accessToken: String, accessTokenSecret: String): TwitterAccount = db.localTx { implicit session =>
    sql"""
        INSERT INTO
          twitter_accounts(id, screen_name, access_token, access_token_secret)
        VALUES
          ($id, $screenName, $accessToken, $accessTokenSecret)
        ON CONFLICT (id) DO UPDATE
        SET
          screen_name = $screenName,
          access_token = $accessToken,
          access_token_secret = $accessTokenSecret
      """
      .update()
      .apply()
    TwitterAccount(id, screenName, accessToken, accessTokenSecret)
  }

}