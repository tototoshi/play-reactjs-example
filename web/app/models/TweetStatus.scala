package models

case class TwitterUser(id: Long, screenName: String, profileImageURL: String)

case class TweetPhoto(url: String)

case class TweetStatus(
  id: String,
  text: String,
  user: TwitterUser,
  photos: Seq[TweetPhoto],
  isRetweet: Boolean,
  isRetweetedByMe: Boolean,
  isFavoritedByMe: Boolean,
  retweetedStatus: Option[TweetStatus])

