name := "HopScraper"

version := "0.1"

scalaVersion := "2.11.12"

libraryDependencies ++= Seq(
  "com.github.seratch" %% "awscala" % "0.8.+",
  "org.apache.spark" %% "spark-sql" % "2.4.3",
  "com.amazonaws" % "aws-java-sdk" % "1.7.4",
  //"org.mongodb.spark" %% "mongo-spark-connector" % "2.4.0",
  "org.apache.spark" %% "spark-core" % "2.4.0",
  //"com.amazon.redshift" % "redshift-jdbc42" % "1.2.37.1061",
  //"io.github.spark-redshift-community" %% "spark-redshift" % "4.0.1",
  "org.apache.logging.log4j" % "log4j-core" % "2.12.1",
  "org.jsoup" % "jsoup" % "1.12.1",
  "com.crealytics" %% "spark-excel" % "0.12.3"
)

mainClass in (Compile, run) := Some("com.github.samwallis.HopScraper.ScrapeHops")
mainClass in (Compile, packageBin) := Some("com.github.samwallis.HopScraper.ScrapeHops")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}