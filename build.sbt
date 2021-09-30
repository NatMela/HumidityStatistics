name := "HumidityStatistics"

version := "0.1"

scalaVersion := "2.13.6"

libraryDependencies ++= Seq("org.slf4j" % "slf4j-api" % "1.7.5", "org.slf4j" % "slf4j-simple" % "1.7.5")
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.16"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.10" % Test
libraryDependencies += "org.mockito" %% "mockito-scala" % "1.16.42" % Test
