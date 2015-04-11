import scala.slick.codegen.SourceCodeGenerator
import scala.slick.{ model => m }

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
  scalaVersion := "2.11.6",
  flywayUrl := databaseUrl,
  flywayUser := databaseUser,
  flywayPassword := databasePassword,
  flywayLocations := Seq("filesystem:web/conf/db/migration/default")
)

lazy val web = (project in file("web"))
  .enablePlugins(PlayScala)
  .settings(slickCodegenSettings:_*)
  .settings(scalariformSettings:_*)
  .settings(
    name := "web",
    scalaVersion := "2.11.6",
    libraryDependencies ++= Seq(
      jdbc,
      ws,
      "com.typesafe.play" %% "play-slick" % "0.8.1",
      "com.typesafe.slick" %% "slick" % "2.1.0",
      "com.github.tototoshi" %% "slick-joda-mapper" % "1.2.0",
      "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
      "jp.t2v" %% "play2-auth"      % "0.13.2",
      "jp.t2v" %% "play2-auth-test" % "0.13.2" % "test",
      "org.twitter4j" % "twitter4j-core" % "4.0.3"
    ),
    slickCodegenDatabaseUrl := databaseUrl,
    slickCodegenDatabaseUser := databaseUser,
    slickCodegenDatabasePassword := databasePassword,
    slickCodegenDriver := scala.slick.driver.PostgresDriver,
    slickCodegenJdbcDriver := "org.postgresql.Driver",
    slickCodegenOutputPackage := "models.tables",
    slickCodegenExcludedTables := Seq("schema_version"),
    slickCodegenCodeGenerator := { (model:  m.Model) =>
      new SourceCodeGenerator(model) {
        override def code =
          "import com.github.tototoshi.slick.PostgresJodaSupport._\n" + "import org.joda.time.DateTime\n" + super.code
        override def Table = new Table(_) {
          override def Column = new Column(_) {
            override def rawType = model.tpe match {
              case "java.sql.Timestamp" => "DateTime" // kill j.s.Timestamp
              case _ =>
                super.rawType
            }
          }
        }
      }
    },
    sourceGenerators in Compile <+= slickCodegen
)
