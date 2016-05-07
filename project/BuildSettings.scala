import sbt._
import sbt.Keys._

object BuildSettings {

	val projSettings = Seq(
		credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
	) ++ Analysis.analysisSettings ++ Formatting.formattingSettings

}
