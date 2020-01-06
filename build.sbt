import Dependencies._

ThisBuild / scalaVersion := "2.13.0"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "com.uzo"
ThisBuild / organizationName := "Uzo"

resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.sonatypeRepo("releases")


lazy val root = (project in file("."))
  .settings(
    name := "shoppingcart"
  )
  .aggregate(core, tests)

lazy val tests = (project in file("modules/tests"))
  .configs(IntegratonsTest)
  .settings(
    name := "shoppingcart-test-suite",
    scalacOptions += "-Ymacro-annotations",
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      compilerPlugin(Libraries.kindProjector cross CrossVersion.full),
          compilerPlugin(Libraries.betterMonadicFor),
          Libraries.scalaCheck,
          Libraries.scalaTest,
          Libraries.scalaTestPlus
    )
  )
  .dependsOn(core)

lazy val core = (project in file("modules/core"))
  .settings(
    name := "shopping-cart-core",
    scalacOptions += "-Ymacro-annotations",
    resolvers += Resolver.sonatypeRepo("snapshots"),
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      compilerPlugin(Libraries.kindProjector cross CrossVersion.full),
      //compilerPlugin(Libraries.paradise cross CrossVersion.full),
      compilerPlugin(Libraries.betterMonadicFor),
      Libraries.cats,
      Libraries.catsEffect,
      Libraries.catsMeowMtl,
      Libraries.catsRetry,
      Libraries.circeCore,
      Libraries.circeGeneric,
      Libraries.circeParser,
      Libraries.circeRefined,
      Libraries.cirisCore,
      Libraries.cirisEnum,
      Libraries.cirisRefined,
      Libraries.fs2,
      Libraries.http4sDsl,
      Libraries.http4sServer,
      Libraries.http4sClient,
      Libraries.http4sCirce,
      Libraries.http4sJwtAuth,
      Libraries.javaxCrypto,
      Libraries.log4cats,
      Libraries.logback % Runtime,
      Libraries.newtype,
      Libraries.redis4catsEffects,
      Libraries.redis4catsLog4cats,
      Libraries.refinedCore,
      Libraries.refinedCats,
      Libraries.skunkCore,
      Libraries.skunkCirce,
      Libraries.squants
    )
  )