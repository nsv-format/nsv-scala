scalaVersion := "3.3.3"
crossScalaVersions := Seq("2.13.14", "3.3.3")
version := "0.3.0"  // still pre-publish
organization := "org.nsv-format"
name := "nsv-scala"

Compile / managedSourceDirectories := Nil
Compile / scalaSource := baseDirectory.value / "src"
Compile / javaSource := baseDirectory.value / "src"  // if omitted, it'd add src/main/java to paths it checks

Compile / managedResourceDirectories := Nil
Compile / resourceDirectory := baseDirectory.value / "resources"

Test / managedSourceDirectories := Nil
Test / scalaSource := baseDirectory.value / "test"

libraryDependencies += "org.scalameta" %% "munit" % "1.2.1" % Test

idePackagePrefix := Some("org.nsvformat")

// Publishing
publishMavenStyle := true
publishTo := Some("github" at "https://maven.pkg.github.com/nsv-format/nsv-scala")
credentials += Credentials(
  "GitHub Package Registry",
  "maven.pkg.github.com",
  "_",
  sys.env.getOrElse("GITHUB_TOKEN", "")
)

// Central-ready metadata
homepage := Some(url("https://github.com/nsv-format/nsv-scala"))
licenses := List("MIT" -> url("https://opensource.org/licenses/MIT"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/nsv-format/nsv-scala"),
    "scm:git:git://github.com/nsv-format/nsv-scala.git",
    Some("scm:git:ssh://github.com:nsv-format/nsv-scala.git")
  )
)
// TODO: update developers with real maintainer info before publishing to Central
developers := List(
  Developer("nsv-format", "nsv-format", "", url("https://github.com/nsv-format"))
)
