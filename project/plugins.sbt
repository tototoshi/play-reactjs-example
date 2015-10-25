resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Flyway" at "http://flywaydb.org/repo"

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.3")

addSbtPlugin("org.flywaydb" % "flyway-sbt" % "3.2.1")

libraryDependencies += "org.postgresql" % "postgresql" % "9.4-1201-jdbc41"

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")

addSbtPlugin("org.scalikejdbc" %% "scalikejdbc-mapper-generator" % "2.2.9")

libraryDependencies += "org.postgresql" % "postgresql" % "9.4-1201-jdbc41"
