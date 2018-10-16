name := "scala-spark-anaylsis"

version := "0.1"

scalaVersion := "2.11.12"

// https://mvnrepository.com/artifact/org.apache.spark/spark-core
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.3.2"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.3.2"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "2.3.2"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "2.3.2"
libraryDependencies += "org.apache.spark" %% "spark-hive" % "2.3.2"
libraryDependencies += "org.apache.spark" %% "spark-graphx" % "2.3.2"
libraryDependencies += "org.apache.spark" %% "spark-yarn" % "2.3.2"
libraryDependencies += "org.apache.spark" %% "spark-network-shuffle" % "2.3.2"
libraryDependencies += "org.apache.spark" %% "spark-streaming-flume" % "2.3.2"