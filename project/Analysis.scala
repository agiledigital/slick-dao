import de.johoop.jacoco4sbt.JacocoPlugin.jacoco
import org.danielnixon.extrawarts.ExtraWart
import org.scalastyle.sbt.ScalastylePlugin._
import sbt.Keys._
import sbt._
import wartremover.WartRemover.autoImport._

object Analysis {

  lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

  lazy val scalaStyleSettings = Seq(
    scalastyleFailOnError := true,
    compileScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value,
    (test in Test) := (test in Test) dependsOn compileScalastyle
  )

  lazy val coverageSettings = Seq(
  )  ++ jacoco.settings

  lazy val wartsSettings = Seq(
    wartremoverErrors in(Compile, compile) ++= Seq(
      Wart.ArrayEquals,
      Wart.Any,
      Wart.AsInstanceOf,
      Wart.IsInstanceOf,
      Wart.EitherProjectionPartial,
      Wart.Enumeration,
      Wart.ExplicitImplicitTypes,
      Wart.FinalCaseClass,
      Wart.JavaConversions,
      Wart.LeakingSealed,
      Wart.MutableDataStructures,
      Wart.Null,
      Wart.OptionPartial,
      Wart.Product,
      Wart.Return,
      Wart.Serializable,
      Wart.StringPlusAny,
      Wart.Throw,
      Wart.ToString,
      Wart.TryPartial,
      Wart.Var,
      Wart.While)
  )

  lazy val extraWartsSettings = Seq(
    wartremoverWarnings in (Compile, compile) ++= Seq(
      ExtraWart.FutureObject,
      ExtraWart.GenMapLikePartial,
      ExtraWart.GenTraversableOnceOps,
      ExtraWart.ScalaGlobalExecutionContext,
      ExtraWart.StringOpsPartial,
      ExtraWart.TraversableOnceOps)

  )

  lazy val scalacSettings = Seq(
    scalacOptions ++= Seq(
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-feature", // Emit warning and location for usages of features that should be imported explicitly.
      "-unchecked", // Enable additional warnings where generated code depends on assumptions.
      "-Xfatal-warnings", // Fail the compilation if there are any warnings.
      "-Xlint", // Enable recommended additional warnings.
      "-Xlint:-unused,_",
      "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
      "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
      "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
      "-Ywarn-numeric-widen" // Warn when numerics are widened.
    )
  )

  lazy val analysisSettings = scalaStyleSettings ++ coverageSettings ++
    wartsSettings ++ extraWartsSettings ++ scalacSettings
}
