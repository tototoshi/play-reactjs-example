package models

import scalikejdbc._

case class TwitterAccount(
    id: Long,
    screenName: String,
    accessToken: String,
    accessTokenSecret: String) {

  def save()(implicit session: DBSession = TwitterAccount.autoSession): TwitterAccount = TwitterAccount.save(this)(session)

  def destroy()(implicit session: DBSession = TwitterAccount.autoSession): Unit = TwitterAccount.destroy(this)(session)

}

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

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[TwitterAccount] = {
    withSQL {
      select.from(TwitterAccount as ta).where.eq(ta.id, id)
    }.map(TwitterAccount(ta.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[TwitterAccount] = {
    withSQL(select.from(TwitterAccount as ta)).map(TwitterAccount(ta.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(TwitterAccount as ta)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[TwitterAccount] = {
    withSQL {
      select.from(TwitterAccount as ta).where.append(where)
    }.map(TwitterAccount(ta.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[TwitterAccount] = {
    withSQL {
      select.from(TwitterAccount as ta).where.append(where)
    }.map(TwitterAccount(ta.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(TwitterAccount as ta).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    id: Long,
    screenName: String,
    accessToken: String,
    accessTokenSecret: String)(implicit session: DBSession = autoSession): TwitterAccount = {
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

  def batchInsert(entities: Seq[TwitterAccount])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity =>
      Seq(
        'id -> entity.id,
        'screenName -> entity.screenName,
        'accessToken -> entity.accessToken,
        'accessTokenSecret -> entity.accessTokenSecret))
    SQL("""insert into twitter_accounts(
        id,
        screen_name,
        access_token,
        access_token_secret
      ) values (
        {id},
        {screenName},
        {accessToken},
        {accessTokenSecret}
      )""").batchByName(params: _*).apply()
  }

  def save(entity: TwitterAccount)(implicit session: DBSession = autoSession): TwitterAccount = {
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
    accessTokenSecret: String)(implicit session: DBSession = autoSession): TwitterAccount = {
    find(id) match {
      case Some(_) =>
        save(TwitterAccount(id, screenName, accessToken, accessTokenSecret))
      case None =>
        create(id, screenName, accessToken, accessTokenSecret)
    }
  }

  def destroy(entity: TwitterAccount)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(TwitterAccount).where.eq(column.id, entity.id) }.update.apply()
  }

}
