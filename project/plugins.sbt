// For formatting of the source code.
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")

// The scala style plugin
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")

// Wart removers
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.1.1")

// Extra wart removers
addSbtPlugin("org.danielnixon" % "sbt-extrawarts" % "0.3.0")

// For performing releases
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.3")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "1.1")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

// Jacoco coverage plugin.
addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.1.6")
