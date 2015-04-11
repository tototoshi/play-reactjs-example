package models

import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder

trait Twitter4jHelper {

  def getTwitter4jInstance(twitterConfig: TwitterConfig, accessToken: String, accessTokenSecret: String): twitter4j.Twitter = {
    val twitter4jConfBuilder = new ConfigurationBuilder
    val twitter4jConf = twitter4jConfBuilder
      .setOAuthConsumerKey(twitterConfig.serviceInfo.key.key)
      .setOAuthConsumerSecret(twitterConfig.serviceInfo.key.secret)
      .setOAuthAccessToken(accessToken)
      .setOAuthAccessTokenSecret(accessTokenSecret)
      .build
    new TwitterFactory(twitter4jConf).getInstance()
  }

  def getTwitter4jInstance(twitterConfig: TwitterConfig, account: TwitterAccount): twitter4j.Twitter = {
    getTwitter4jInstance(twitterConfig, account.accessToken, account.accessTokenSecret)
  }
}
