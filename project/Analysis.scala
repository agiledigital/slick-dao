import de.johoop.jacoco4sbt.JacocoPlugin.jacoco
import org.danielnixon.playwarts.PlayWart
import org.scalastyle.sbt.ScalastylePlugin._
import sbt.Keys._
import sbt._
import wartremover.WartRemover.autoImport._

object Analysis {

  lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

  lazy val scalaStyleSettings = Seq(
    scalastyleFailOnError := true,
    compileScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value,
    (test in Test) <<= (test in Test) dependsOn compileScalastyle
  )

  lazy val coverageSettings = Seq(
  )  ++ jacoco.settings

  lazy val wartsSettings = Seq(
    wartremoverErrors in(Compile, compile) ++= Seq(
      Wart.FinalCaseClass,
      Wart.Null,
      Wart.TryPartial,
      Wart.Var,
      Wart.OptionPartial,
      Wart.ListOps,
      Wart.EitherProjectionPartial,
      Wart.Any2StringAdd,
      Wart.AsInstanceOf,
      Wart.ExplicitImplicitTypes,
      Wart.MutableDataStructures,
      Wart.Return,
      Wart.AsInstanceOf,
      Wart.IsInstanceOf)
  )

  lazy val playWartsSettings = Seq(
    // Bonus Warts
    wartremoverWarnings in (Compile, compile) ++= Seq(
      PlayWart.DateFormatPartial,
      PlayWart.FutureObject,
      PlayWart.GenMapLikePartial,
      PlayWart.GenTraversableLikeOps,
      PlayWart.GenTraversableOnceOps,
      PlayWart.OptionPartial,
      PlayWart.ScalaGlobalExecutionContext,
      PlayWart.StringOpsPartial,
      PlayWart.TraversableOnceOps,
      PlayWart.UntypedEquality)

  )

  lazy val scalacSettings = Seq(
    scalacOptions ++= Seq(
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-feature", // Emit warning and location for usages of features that should be imported explicitly.
      "-unchecked", // Enable additional warnings where generated code depends on assumptions.
      "-Xfatal-warnings", // Fail the compilation if there are any warnings.
      "-Xlint", // Enable recommended additional warnings.
      "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
      "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
      "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
      "-Ywarn-numeric-widen" // Warn when numerics are widened.
    )
  )

  lazy val analysisSettings = scalaStyleSettings ++ coverageSettings ++
    wartsSettings ++ playWartsSettings ++ scalacSettings
}
