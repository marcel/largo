name := "largo"

version := "1.0"

scalaVersion := "2.11.7"

scalacOptions in Test ++= Seq("-Yrangepos")

initialCommands := """
import com.andbutso.largo._
"""