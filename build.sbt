scalaVersion := "2.13.10"
scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-language:reflectiveCalls"
)
val chiselVersion = "5.0.0"
addCompilerPlugin("org.chipsalliance" % "chisel-plugin" % chiselVersion cross CrossVersion.full)
libraryDependencies += "org.chipsalliance" %% "chisel" % chiselVersion
