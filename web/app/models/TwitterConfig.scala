package models

import javax.inject.Inject

import play.api.{ Application, Configuration }
import play.api.libs.oauth.{ ConsumerKey, OAuth, ServiceInfo }

class TwitterConfig @Inject() (configuration: Configuration) {

  private val requestTokenURL = "https://api.twitter.com/oauth/request_token"

  private val accessTokenURL = "https://api.twitter.com/oauth/access_token"

  private val authorizationURL = "https://api.twitter.com/oauth/authorize"

  private val consumerKey = ConsumerKey(
    configuration.getString("twitter.consumerKey").getOrElse(sys.error("twitter.consumerKey is missing")),
    configuration.getString("twitter.consumerSecret").getOrElse(sys.error("twitter.consumerSecret is missing"))
  )

  val serviceInfo = ServiceInfo(
    requestTokenURL,
    accessTokenURL,
    authorizationURL,
    consumerKey
  )

  val callbackURL = configuration.getString("twitter.callbackURL").getOrElse(
    sys.error("twitter.callbackURL is missing")
  )

  val oauth = OAuth(serviceInfo)

}
