package controllers

case class FavForm(id: Long)

case class OAuthForm(token: String, verifier: String)

case class RtForm(id: Long)

case class TweetForm(text: String)
