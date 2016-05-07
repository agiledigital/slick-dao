import Dependencies._
import BuildSettings._
import wartremover.WartRemover.autoImport._

scalaVersion in ThisBuild := "2.11.6"

ivyScala := ivyScala.value map {
  _.copy(overrideScalaVersion = true)
}

organization in ThisBuild := "au.com.agiledigital"

crossScalaVersions := Seq("2.10.5", "2.11.6")

parallelExecution in Test := false

lazy val root = Project(
  id = "dao-slick-root",
  base = file("."),
  settings = projSettings ++ Seq(
    publishArtifact := false
  )
) aggregate(slickDao, samples)


// Core ==========================================
lazy val slickDao = Project(
  id = "dao-slick",
  base = file("modules/core"),
  settings = projSettings ++ mainDeps ++ testDeps
)
//================================================


// Samples =======================================
// contains examples used on the docs, not intended to be released
lazy val samples = Project(
  id = "dao-slick-samples",
  base = file("modules/samples"),
  settings = projSettings ++ Seq(
    publishArtifact := false,
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick-codegen" % slickVersion,
      "com.h2database" % "h2" % "1.4.187",
      scalaTest
    ),
    wartremoverExcluded += sourceManaged.value / "au" / "com" / "agiledigital" / "dao" / "slick" / "docexamples" / "codegen" / "Tables.scala",
    slick <<= slickCodeGenTask,
    sourceGenerators in Compile <+= slickCodeGenTask
  ) ++ mainDeps
).dependsOn(slickDao % "compile->compile;test->test")
//======+=========================================

lazy val slick = TaskKey[Seq[File]]("gen-tables")

lazy val slickCodeGenTask = (sourceManaged, dependencyClasspath in Compile, runner in Compile, streams) map { (srcManaged, cp, r, s) =>
  val pkg = "au.com.agiledigital.dao.slick.docexamples.codegen"
  val url = "jdbc:h2:mem:test;INIT=runscript from 'modules/samples/src/main/resources/codegen_schema.sql'" // connection info for a pre-populated throw-away, in-memory db for this demo, which is freshly initialized on every run
  val jdbcDriver = "org.h2.Driver"
  val slickDriver = "slick.driver.H2Driver"
  toError(r.run("slick.codegen.SourceCodeGenerator", cp.files, Array(slickDriver, jdbcDriver, url, srcManaged.getPath, pkg), s.log))
  val outputDir = srcManaged / pkg.replace(".", "/")
  Seq(outputDir / "Tables.scala")
}
