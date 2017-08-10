import Dependencies._
import BuildSettings._
import wartremover.WartRemover.autoImport._

scalaVersion in ThisBuild := "2.11.8"

ivyScala := ivyScala.value map {
  _.copy(overrideScalaVersion = true)
}

organization in ThisBuild := "au.com.agiledigital"

crossScalaVersions := Seq("2.10.5", "2.11.8")

parallelExecution in Test := false

scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-language:reflectiveCalls", "-language:postfixOps", "-checkinit")

lazy val root = Project(
  id = "dao-slick-root",
  base = file("."),
  settings = projSettings ++ Seq(
    publishArtifact := false
  )
) aggregate(slickDao)


// Core ==========================================
lazy val slickDao = Project(
  id = "dao-slick",
  base = file("modules/core"),
  settings = projSettings ++ mainDeps ++ testDeps
)
//================================================

lazy val slick = TaskKey[Seq[File]]("gen-tables")


