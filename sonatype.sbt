import ReleaseTransformations._

// Your profile name of the sonatype account. The default is the same with the organization value
sonatypeProfileName := "au.com.agiledigital"

// To sync with Maven central, you need to supply the following information:
pomExtra in Global := {
  <url>https://github.com/agiledigital/play-rest-support</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      </license>
    </licenses>
    <scm>
      <connection>scm:git:github.com/agiledigital/slick-dao</connection>
      <developerConnection>scm:git:git@github.com:agiledigital/slick-dao</developerConnection>
      <url>github.com/agiledigital/slick-dao</url>
    </scm>
    <developers>
      <developer>
        <id>daniel.spasojevic@gmail.com</id>
        <name>Daniel Spasojevic</name>
      </developer>
    </developers>
}

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = Command.process("publishSigned", _)),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
  pushChanges
)
