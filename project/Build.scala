import sbt._
import Keys._

object Build extends sbt.Build {
  object Common {
    lazy val settings = Defaults.defaultSettings ++ Seq(
      organization := "com.github.philcali",
      version := "0.0.1",
      scalaVersion := "2.10.3",
      libraryDependencies += "org.scalatest" %% "scalatest" % "2.0" % "test"
    )
  }

  lazy val root = Project(
    "argonaut",
    file("."),
    settings = Common.settings
  ) aggregate (core, help, reflect)

  lazy val core = Project(
    "argonaut-core",
    file("core"),
    settings = Common.settings
  )

  lazy val help = Project(
    "argonaut-help",
    file("help"),
    settings = Common.settings
  ) dependsOn core

  lazy val reflect = Project(
    "argonaut-reflect",
    file("reflection"),
    settings = Common.settings ++ Seq(
      libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
    )
  ) dependsOn core
}
