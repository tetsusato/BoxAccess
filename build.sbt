val scala3Version = "3.2.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "BoxAccess",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "com.box" % "box-java-sdk" % "4.0.0",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.10" % Test,
    libraryDependencies += "org.tinylog" % "tinylog-api" % "2.5.0",
    libraryDependencies += "org.tinylog" % "tinylog-impl" % "2.5.0",
    libraryDependencies += "com.github.pathikrit" % "better-files_2.13" % "3.9.1",
    libraryDependencies += "com.electronwill.night-config" %  "toml" % "3.6.6",
    libraryDependencies += "org.apache.httpcomponents.client5" % "httpclient5" % "5.2.1",
    libraryDependencies += "org.apache.httpcomponents.client5" % "httpclient5-fluent" % "5.2.1",

  )
assembly / assemblyMergeStrategy := {
  case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
  case PathList("javax", "xml", xs @ _*)         => MergeStrategy.first
  case PathList("com", "fasterxml", xs @ _*)         => MergeStrategy.first
  case PathList("org", "slf4j", xs @ _*)         => MergeStrategy.first
  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
  case PathList("com", "google", xs @ _*) => MergeStrategy.last
  case PathList("META-INF", "MANIFEST.MF", xs @ _*) => MergeStrategy.discard

  case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".xml" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".json" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".types" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "epoll_x86_64.so" => MergeStrategy.first
  case "UnusedStubClass.class"  => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "module-info.class"  => MergeStrategy.first
  case "module-info.class"  => MergeStrategy.first
  case "mozilla/public-suffix-list.txt"  => MergeStrategy.first
  case "jetty-dir.css"                            => MergeStrategy.first

  case "application.conf"                            => MergeStrategy.concat
  case "unwanted.txt"                                => MergeStrategy.discard
  // Failed
  case "org.apache.hadoop.fs.FileSystem"                                => MergeStrategy.discard
    // Great!
  case PathList(p @ _*) if p.last == "org.apache.hadoop.fs.FileSystem" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}
assemblyPackageDependency / assemblyMergeStrategy := {

  case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
  case PathList("javax", "xml", xs @ _*)         => MergeStrategy.first
  case PathList("com", "fasterxml", xs @ _*)         => MergeStrategy.first
  case PathList("org", "slf4j", xs @ _*)         => MergeStrategy.first
  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
  case PathList("com", "google", xs @ _*) => MergeStrategy.last
  case PathList("META-INF", "MANIFEST.MF", xs @ _*) => MergeStrategy.discard

  case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".xml" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".json" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".types" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "epoll_x86_64.so" => MergeStrategy.first
  case "UnusedStubClass.class"  => MergeStrategy.first
//  case PathList(ps @ _*) if ps.last endsWith "module-info.class"  => MergeStrategy.first
  case "module-info.class"  => MergeStrategy.first
  case "mozilla/public-suffix-list.txt"  => MergeStrategy.first
  case "jetty-dir.css"                            => MergeStrategy.first

  case "application.conf"                            => MergeStrategy.concat
  case "unwanted.txt"                                => MergeStrategy.discard
  // Failed
  case "org.apache.hadoop.fs.FileSystem"                                => MergeStrategy.discard
    // Great!
  case PathList(p @ _*) if p.last == "org.apache.hadoop.fs.FileSystem" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}

