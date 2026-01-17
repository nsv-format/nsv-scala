scalaVersion := "3.3.3"
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
