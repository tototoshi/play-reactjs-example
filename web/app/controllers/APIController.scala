package controllers

import jp.t2v.lab.play2.auth.AuthElement
import models.JsonFormats._
import models._
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._
import twitter4j.{ Paging, Twitter, TwitterFactory }

import scala.collection.JavaConverters._

object APIController extends Controller with AuthConfigImpl with AuthElement with Twitter4jHelper {

  val twitterAccountRepository = new TwitterAccountRepository

  val twitterConfig = new TwitterConfig

  def timeline(sinceId: Option[Long]) = StackAction(AuthorityKey -> Normal) { implicit request =>
    val account = loggedIn
    val tw = getTwitter4jInstance(twitterConfig, account)
    val statuses = sinceId.map { sid =>
      tw.getHomeTimeline(new Paging(sid))
    } getOrElse {
      tw.getHomeTimeline
    }
    Ok(Json.toJson(
      statuses.asScala.toSeq.map { t =>
        convertTwitter4JStatusToTweetStatus(t)
      }))
  }

  val rtForm = Form(
    mapping("id" -> longNumber)(RtForm.apply)(RtForm.unapply)
  )

  def rt = StackAction(AuthorityKey -> Normal) { implicit request =>
    val account = loggedIn
    rtForm.bindFromRequest.fold({
      formWithError => BadRequest
    }, {
      case RtForm(id) =>
        val tw = getTwitter4jInstance(twitterConfig, account)
        tw.retweetStatus(id)
        Ok
    })
  }

  val favForm = Form(
    mapping("id" -> longNumber)(FavForm.apply)(FavForm.unapply)
  )

  def fav = StackAction(AuthorityKey -> Normal) { implicit request =>
    val account = loggedIn
    favForm.bindFromRequest.fold({
      formWithError => BadRequest
    }, {
      case FavForm(id) =>
        val tw = getTwitter4jInstance(twitterConfig, account)
        tw.createFavorite(id)
        Ok
    })
  }

  val tweetForm = Form(
    mapping("text" -> nonEmptyText)(TweetForm.apply)(TweetForm.unapply)
  )

  def tweet = StackAction(AuthorityKey -> Normal) { implicit request =>
    val account = loggedIn
    tweetForm.bindFromRequest.fold({
      formWithError => BadRequest
    }, {
      case TweetForm(tweetText) =>
        val tw = getTwitter4jInstance(twitterConfig, account)
        tw.updateStatus(tweetText)
        Ok
    })
  }

  private def convertTwitter4JStatusToTweetStatus(t: twitter4j.Status): TweetStatus = {
    val u = TwitterUser(
      t.getUser.getId,
      t.getUser.getScreenName,
      t.getUser.getProfileImageURL
    )
    TweetStatus(
      t.getId.toString,
      t.getText,
      u,
      (t.getMediaEntities.filter(_.getType == "photo").map(e => TweetPhoto(e.getMediaURL))
        ++ t.getExtendedMediaEntities.filter(_.getType == "photo").map(e => TweetPhoto(e.getMediaURL))).distinct,
      t.isRetweet,
      t.isRetweetedByMe,
      t.isFavorited,
      Option(t.getRetweetedStatus).map { rt =>
        TweetStatus(
          rt.getId.toString,
          rt.getText,
          TwitterUser(
            rt.getUser.getId,
            rt.getUser.getScreenName,
            rt.getUser.getProfileImageURL
          ),
          (rt.getMediaEntities.filter(_.getType == "photo").map(e => TweetPhoto(e.getMediaURL))
            ++ rt.getExtendedMediaEntities.filter(_.getType == "photo").map(e => TweetPhoto(e.getMediaURL))).distinct,
          isRetweet = false,
          rt.isRetweetedByMe,
          rt.isFavorited,
          None
        )
      }
    )
  }

}
