name := """GestionCompetitionsAPI"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.14",
  "org.reactivemongo" %% "reactivemongo-play-json" % "0.11.14",
  "com.typesafe.play" %% "play-mailer" % "5.0.0",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.webjars" % "bootstrap" % "3.3.7-1",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

routesImport += "v1.http.QueryStringBinders._"

fork in run := true