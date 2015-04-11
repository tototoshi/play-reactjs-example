package models

import models.tables.Tables
import play.api.Application
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.Database

case class TwitterAccount(
  id: Long,
  screenName: String,
  accessToken: String,
  accessTokenSecret: String)

class TwitterAccountRepository(implicit app: Application) {

  def find(id: Long): Option[TwitterAccount] = {
    Database("default").withSession { implicit session =>
      Tables.TwitterAccounts.filter(_.id === id).firstOption.map { t =>
        TwitterAccount(
          t.id,
          t.screenName,
          t.accessToken,
          t.accessTokenSecret
        )
      }
    }
  }

  def save(id: Long, screenName: String, accessToken: String, accessTokenSecret: String): Unit = {
    Database("default").withSession { implicit session =>
      Tables.TwitterAccounts.filter(_.id === id).firstOption match {
        case None =>
          Tables.TwitterAccounts += Tables.TwitterAccountsRow(id, screenName, accessToken, accessTokenSecret)
        case Some(_) =>
      }
    }
  }

}
