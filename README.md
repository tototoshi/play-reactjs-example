# play-reactjs-example

Simple Twitter App with Play2 and React.js

Built with:

 - Play2
 - React.js
 - Twitter Bootstrap
 - play2-auth
 - twitter4j
 - flyway-sbt
 - scalikejdbc

## How to run

```
$ export DB_DEFAULT_URL="jdbc:postgresql://localhost/yourdb"
$ export DB_DEFAULT_USER="username"
$ export DB_DEFAULT_PASSWORD="password"
$ export TWITTER_CONSUMER_KEY="yourconsumerkey"
$ export TWITTER_CONSUMER_SECRET="yourconsumersecret"
$ export TWITTER_CALLBACK_URL="http://localhost:9000/authorize"

$ sbt
> flyway/flywayMigrate
> project web
> compile
> run
```
