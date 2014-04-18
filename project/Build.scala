import sbt._
import Keys._
import scala.Some

object CriteoSummingbirdBuild extends Build {
  def withCross(dep: ModuleID) =
    dep cross CrossVersion.binaryMapped {
      case "2.9.3" => "2.9.2" // TODO: hack because twitter hasn't built things against 2.9.3
      case version if version startsWith "2.10" => "2.10" // TODO: hack because sbt is broken
      case x => x
    }

  def specs2Import(scalaVersion: String) = scalaVersion match {
      case version if version startsWith "2.9" => "org.specs2" %% "specs2" % "1.12.4.1" % "test"
      case version if version startsWith "2.10" => "org.specs2" %% "specs2" % "1.13" % "test"
  }

  val dfsDatastoresVersion = "1.3.4"
  val bijectionVersion = "0.6.2"
  val storehausVersion = "0.8.0"// TO FIX: "0.9.0rc2"
  val tormentaVersion = "0.7.0"
  val summingbirdStormVersion = "0.4.1"
  val summingbirdCoreVersion = "0.4.1"
  val summingbirdClientVersion = "0.4.1"
  val summingbirdScaldingVersion = "0.4.1"
  val jacksonVersion = "2.2.3"
  val scaldingVersion = "0.9.0rc15"

  lazy val slf4jVersion = "1.6.6"

  val extraSettings = Project.defaultSettings

  val sharedSettings = extraSettings ++ Seq(
    organization := "com.criteo",
    version := "0.1",
    scalaVersion := "2.9.3",
    //logLevel := Level.Debug,
    crossScalaVersions := Seq("2.9.3", "2.10.0"),
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
      // These satisify's scaldings log4j needs when in test mode
      "log4j" % "log4j" % "1.2.16" % "test",
      "org.slf4j" % "slf4j-log4j12" % slf4jVersion % "test",
      "com.typesafe" % "config" % "1.2.0"
    ),

    libraryDependencies <+= scalaVersion(specs2Import(_)),

    libraryDependencies ++= Seq(
      //"log4j" % "log4j" % "1.2.16",
      "org.slf4j" % "slf4j-log4j12" % slf4jVersion,
      "storm" % "storm" % "0.9.0-wip15" exclude("org.slf4j", "log4j-over-slf4j") exclude("ch.qos.logback", "logback-classic"),
      //"storm" % "storm-kafka" % "0.9.0-wip15-scala292" exclude("com.twitter", "kafka_2.9.2"),
      "com.twitter" %% "bijection-netty" % bijectionVersion,
      "com.twitter" %% "tormenta-kafka" % tormentaVersion,// exclude("storm", "storm-kafka"),
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
      "com.twitter" %% "storehaus-memcache" % storehausVersion,
      "com.twitter" %% "summingbird-storm" % summingbirdStormVersion,
      "com.twitter" %% "summingbird-core" % summingbirdCoreVersion,
      "com.twitter" %% "summingbird-client" % summingbirdClientVersion,
      "com.twitter" %% "summingbird-scalding" % summingbirdScaldingVersion,
      "com.twitter" %% "summingbird-builder" % summingbirdScaldingVersion,
      "com.twitter" %% "scalding-args" % scaldingVersion,
      "com.twitter" %% "bijection-core" % bijectionVersion,

      "com.twitter" %% "summingbird-scalding-test" % summingbirdScaldingVersion,
      "org.scalatest" %% "scalatest" % "1.9.2" withSources(),
      "com.twitter" %% "summingbird-storm-test" % summingbirdStormVersion,
      "junit" % "junit" % "4.4"
    ),

    resolvers ++= Seq(
      Opts.resolver.sonatypeSnapshots,
      Opts.resolver.sonatypeReleases,
      "Conjars Repository" at "http://conjars.org/repo",
      "Twitter Maven" at "http://maven.twttr.com",
      "Clojars Repository" at "http://clojars.org/repo"
    ),

    parallelExecution in Test := true,

    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-Yresolve-term-conflict:package"/*,
      "-Xlog-implicits"*/
    ),

    // Publishing options:
    publishMavenStyle := true,

    publishArtifact in Test := false,

    pomIncludeRepository := { x => false },

    publishTo <<= version { v =>
      Some(
        if (v.trim.toUpperCase.endsWith("SNAPSHOT"))
          Opts.resolver.sonatypeSnapshots
        else
          Opts.resolver.sonatypeStaging
          //"twttr" at "http://artifactory.local.twitter.com/libs-releases-local"
      )
    }

  )

  lazy val criteoSummingbird = Project(
    id = "criteo-summingbird",
    base = file("."),
    settings = sharedSettings
  ).settings(
    test := { },
    publish := { }, // skip publishing for this root project.
    publishLocal := { }
  )
}
