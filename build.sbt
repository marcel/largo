name := "largo"

version := "1.0"

scalaVersion := "2.11.7"
dependencyOverrides += "org.scala-lang" % "scala-compiler" % scalaVersion.value

libraryDependencies ++= Seq(
	"org.specs2" %% "specs2-core" % "3.6.2" % "test",
	"org.specs2" %% "specs2-mock" % "3.6.2" % "test",
	"org.specs2" %% "specs2-junit" % "3.6.2" % "test"
)

scalacOptions in Test ++= Seq("-Yrangepos")

initialCommands := """
import com.andbutso.largo._
"""