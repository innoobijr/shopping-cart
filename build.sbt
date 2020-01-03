name := "shoppingcart"

version := "0.1"

ThisBuild / scalaVersion := "2.13.0"

libraryDependencies ++= Seq(
  compilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
  "org.typelevel" %% "cats-core" % "2.0.0",
  "org.typelevel" %% "cats-effect" % "2.0.0",
  "dev.profunktor" %% "console4cats" % "0.8.0",
  "io.estatico" %% "newtype" % "0.4.3",
  "eu.timepit" %% "refined" % "0.9.10",
  "org.typelevel"  %% "squants"  % "1.6.0",
  "dev.profunktor" %% "http4s-jwt-auth" % "0.0.3"
)

scalacOptions += "-Ymacro-annotations"