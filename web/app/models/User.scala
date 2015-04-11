package models

sealed trait Role
case object Normal extends Role

case class User(id: Int, name: String)
