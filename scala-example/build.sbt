name := "example-streaming"

version := "0.1"

scalaVersion := "2.11.8"
val sparkVersion = "2.4.0"


// Spark compile-time dependencies
libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion % "provided"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided"
libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion % "provided"
libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion % "provided"


/**
  * ScalaPB - protobuf compiler for Scala and JSON formatter for protobuf objects.
  * https://scalapb.github.io/
  */
libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
libraryDependencies += "com.thesamet.scalapb" %% "scalapb-json4s" % "0.7.0"

// Generate code from protbuf
PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)
