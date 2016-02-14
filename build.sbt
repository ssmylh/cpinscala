lazy val root = (project in file(".")).
  settings(
    name := "cpinscala",
    version := "0.1",
    scalaVersion := "2.11.7",
    resolvers ++= Seq(
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/releases"
    ),
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-async" % "0.9.5",
      "com.storm-enroute" %% "scalameter" % "0.7",
      "org.scalafx" %% "scalafx" % "8.0.60-R9",
      "io.reactivex" %% "rxscala" % "0.26.0"
    ),
    unmanagedSourceDirectories in Compile <<= Seq(scalaSource in Compile).join,
    unmanagedSourceDirectories in Test <<= Seq(scalaSource in Test).join
  )