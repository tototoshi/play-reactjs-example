package controllers

import javax.inject.Inject

import jp.t2v.lab.play2.auth.AuthElement
import models.JsonFormats._
import models._
import play.api.{ Environment, Logger }
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._
import twitter4j.{ Paging, Twitter, TwitterException }

import scala.collection.JavaConverters._

class APIController @Inject() (
  val environment: Environment,
  val twitterAccountRepository: TwitterAccountRepository,
  twitterConfig: TwitterConfig) extends Controller
    with AuthConfigImpl
    with AuthElement {

  private val logger = Logger(classOf[APIController])

  private val twitter4jFactory: Twitter4jFactory = new Twitter4jFactory(twitterConfig)

  private def withTwitter(account: User)(f: Twitter => Result): Result = {
    val tw = twitter4jFactory.getTwitter4jInstance(account)
    try {
      f(tw)
    } catch {
      case e: TwitterException =>
        logger.error(e.getMessage, e)
        InternalServerError(Json.toJson(Map("message" -> e.getMessage)))
    }
  }

  def timeline(sinceId: Option[Long]) = StackAction(AuthorityKey -> Normal) { implicit request =>
    val account = loggedIn

    withTwitter(account) { tw =>
      val statuses = sinceId.map { sid =>
        tw.getHomeTimeline(new Paging(sid))
      } getOrElse {
        tw.getHomeTimeline
      }
      Ok(Json.toJson(
        statuses.asScala.map { t =>
          convertTwitter4JStatusToTweetStatus(t)
        }))
    }
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
        withTwitter(account) { tw =>
          tw.retweetStatus(id)
          Ok
        }
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
        withTwitter(account) { tw =>
          tw.createFavorite(id)
          Ok
        }
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
        withTwitter(account) { tw =>
          tw.updateStatus(tweetText)
          Ok
        }
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
