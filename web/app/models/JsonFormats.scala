package models

import play.api.libs.json.Json

object JsonFormats {

  implicit val tweetPhotoFormat = Json.format[TweetPhoto]

  implicit val twitterUserFormat = Json.format[TwitterUser]

  implicit val tweetStatusFormat = Json.format[TweetStatus]

}
