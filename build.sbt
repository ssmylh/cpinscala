lazy val root = (project in file(".")).
  settings(
    name := "cpinscala",
    version := "0.1",
    scalaVersion := "2.11.6",
    unmanagedSourceDirectories in Compile <<= Seq(scalaSource in Compile).join,
    unmanagedSourceDirectories in Test <<= Seq(scalaSource in Test).join
  )