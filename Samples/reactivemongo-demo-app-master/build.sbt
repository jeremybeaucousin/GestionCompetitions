name := "reactivemongo-demo-app"

val buildVersion = "0.12.0"

version := buildVersion

resolvers += "Sonatype Staging" at "https://oss.sonatype.org/content/repositories/staging/"

scalaVersion := "2.11.8"

libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % buildVersion

routesGenerator := InjectedRoutesGenerator

fork in run := true

lazy val root = (project in file(".")).enablePlugins(PlayScala)
