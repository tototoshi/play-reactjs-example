name := "play-reactjs-example"

version := "0.1.0-SNAPSHOT"

organization := "com.github.tototoshi"

lazy val databaseUrl = sys.env.getOrElse("DB_DEFAULT_URL", "DB_DEFAULT_URL is not set")

lazy val databaseUser = sys.env.getOrElse("DB_DEFAULT_USER", "DB_DEFAULT_USER is not set")

lazy val databasePassword = sys.env.getOrElse("DB_DEFAULT_PASSWORD", "DB_DEFAULT_PASSWORD is not set")

lazy val flyway = (project in file("flyway"))
  .settings(flywaySettings:_*)
  .settings(
  name := "flyway",
  scalaVersion := "2.11.8",
  flywayUrl := databaseUrl,
  flywayUser := databaseUser,
  flywayPassword := databasePassword,
  flywayLocations := Seq("filesystem:web/conf/db/migration/default")
)

lazy val web = (project in file("web"))
  .enablePlugins(PlayScala)
  .settings(scalariformSettings:_*)
  .settings(scalikejdbcSettings:_*)
  .settings(
    name := "web",
    scalaVersion := "2.11.8",
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    libraryDependencies ++= Seq(
      jdbc,
      ws,
      cache,
      "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
      "com.github.tototoshi" %% "dbcache-postgresql" % "0.1.0-SNAPSHOT",
      "jp.t2v" %% "play2-auth"      % "0.14.1",
      "jp.t2v" %% "play2-auth-test" % "0.14.1" % "test",
      "org.twitter4j" % "twitter4j-core" % "4.0.3",
      "org.scalikejdbc" %% "scalikejdbc" % "2.3.5",
      "org.scalikejdbc" %% "scalikejdbc-config" % "2.3.5"
    )
)
