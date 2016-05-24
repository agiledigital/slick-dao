import sbt._
import sbt.Keys._

object BuildSettings {

	val projSettings = Analysis.analysisSettings ++ Formatting.formattingSettings ++ Publishing.settings

}
