package com.github.samwallis.HopScraper

import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.sql.functions._
import com.github.samwallis.HopScraper.OutputSchema.hopSchema

class OutputWriter(hops: Array[Hop]) extends SparkSessionWrapper {
  import spark.implicits._
  var allRows: Seq[Row] = (for (hop <- hops) yield hop.hop_rows).toSeq.flatten

  val allHopsDF = spark.createDataFrame(
    spark.sparkContext.parallelize(allRows),
    hopSchema
  )

  val parsedHops = allHopsDF
    .filter($"Parsed" === lit("parsed"))
    .orderBy($"Price".asc_nulls_last)
  val unparsedHops = allHopsDF
    .filter($"Parsed" =!= lit("parsed"))
    .orderBy($"Product".asc_nulls_last)

  def write(path: String): Unit = {
    println("Writing to: " + path)
    parsedHops.write
      .format("com.crealytics.spark.excel")
      .option("dataAddress", "'Hop Prices'!A1")
      .option("useHeader", "true")
      .mode("append")
      .save(path + ".xlsx")
    unparsedHops.write
      .format("com.crealytics.spark.excel")
      .option("dataAddress", "'Unparsed Hops'!A1")
      .option("useHeader", "true")
      .mode("append")
      .save(path + ".xlsx")
    println("Success!")
  }
}
