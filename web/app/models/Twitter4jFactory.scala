package models

import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder

class Twitter4jFactory(twitterConfig: TwitterConfig) {

  def getTwitter4jInstance(accessToken: String, accessTokenSecret: String): twitter4j.Twitter = {
    val twitter4jConfBuilder = new ConfigurationBuilder
    val twitter4jConf = twitter4jConfBuilder
      .setOAuthConsumerKey(twitterConfig.serviceInfo.key.key)
      .setOAuthConsumerSecret(twitterConfig.serviceInfo.key.secret)
      .setOAuthAccessToken(accessToken)
      .setOAuthAccessTokenSecret(accessTokenSecret)
      .build
    new TwitterFactory(twitter4jConf).getInstance()
  }

  def getTwitter4jInstance(account: TwitterAccount): twitter4j.Twitter = {
    getTwitter4jInstance(account.accessToken, account.accessTokenSecret)
  }

}
