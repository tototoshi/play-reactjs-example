package controllers

import jp.t2v.lab.play2.auth._
import models.{ Role, TwitterAccountRepository }
import play.api.{ Environment, Mode }
import play.api.mvc.Results._
import play.api.mvc.{ RequestHeader, Result }

import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.{ ClassTag, classTag }

trait AuthConfigImpl extends AuthConfig {

  val environment: Environment

  val twitterAccountRepository: TwitterAccountRepository

  type Id = Long

  type User = models.TwitterAccount

  type Authority = Role

  val idTag: ClassTag[Id] = classTag[Id]

  val sessionTimeoutInSeconds: Int = 3600

  def resolveUser(id: Id)(implicit ctx: ExecutionContext): Future[Option[User]] = Future.successful(twitterAccountRepository.find(id))

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.ApplicationController.index))

  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.ApplicationController.index
    ))

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.ApplicationController.request))

  override def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Forbidden("no permission"))
  }

  def authorizationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] = throw new AssertionError()

  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = Future.successful {
    true
  }

  override lazy val tokenAccessor = new CookieTokenAccessor(
    cookieSecureOption = environment.mode == Mode.Prod,
    cookieMaxAge = Some(sessionTimeoutInSeconds)
  )

}
