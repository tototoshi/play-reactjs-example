package controllers

import jp.t2v.lab.play2.auth.{ AuthElement, LoginLogout }
import models._
import play.api.Logger
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.oauth._
import play.api.mvc._

import scala.concurrent.Future

object ApplicationController extends Controller
    with LoginLogout
    with AuthConfigImpl
    with AuthElement
    with Twitter4jHelper {

  val twitterConfig = new TwitterConfig

  def index = StackAction(AuthorityKey -> Normal) { implicit request =>
    Ok(views.html.index())
  }

  def logout = Action.async { implicit request =>
    gotoLogoutSucceeded
  }
  def request = Action { implicit request =>
    twitterConfig.oauth.retrieveRequestToken(twitterConfig.callbackURL) match {
      case Left(e) =>
        Logger.error(e.getMessage, e)
        BadRequest
      case Right(token) =>
        Redirect(twitterConfig.oauth.redirectUrl(token.token)).withSession(
          request.session + ("application.requestTokenSecret" -> token.secret)
        )
    }
  }

  val oauthLinkForm = Form(
    mapping(
      "oauth_token" -> nonEmptyText,
      "oauth_verifier" -> nonEmptyText
    )(OAuthForm.apply)(OAuthForm.unapply)
  )

  def authorize = Action.async { implicit request =>
    oauthLinkForm.bindFromRequest.fold({
      formWithError => Future.successful(BadRequest)
    }, {
      case OAuthForm(oauthToken, verifier) =>
        (for {
          tokenSecret <- request.session.get("application.requestTokenSecret")
          requestToken = RequestToken(oauthToken, tokenSecret)
          token <- twitterConfig.oauth.retrieveAccessToken(
            requestToken, verifier
          ).right.toOption
        } yield {
          val tw = getTwitter4jInstance(twitterConfig, token.token, token.secret)
          TwitterAccount.upsert(
            tw.getId, tw.getScreenName, token.token, token.secret
          )
          gotoLoginSucceeded(tw.getId)
        }).getOrElse(Future.successful(BadRequest))
    })
  }

}
