name := "MTAS1410X8Probe"

version := "1.0"

scalaVersion := "2.11.5"

unmanagedBase := baseDirectory.value / "lib"

unmanagedJars in Compile ++= {
  val base = baseDirectory.value
  val baseDirectories = base / "lib"
  val customJars = baseDirectories ** "*.jar"
  customJars.classpath
}

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
  "org.scalafx" % "scalafx_2.11" % "8.0.0-R4",
  "org.scream3r" % "jssc" % "2.8.0",
  "org.controlsfx" % "controlsfx" % "8.0.6_20",
  "org.spire-math" % "spire_2.11" % "0.9.1",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.3-1"
)