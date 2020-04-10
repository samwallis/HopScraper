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


  def write(path: String, filename : String, format: String): Unit = {
    if (format == "parquet") {
      println("Writing to: " + path + filename)
      allHopsDF.write.mode("append").parquet(path+filename)
      println("Success!")
    } else if (format == "xlsx") {
      println("Writing to: " + path + filename + ".xlsx")
      val parsedHopsAvailabvle = allHopsDF
        .filter($"Parsed" === lit("parsed") && $"Availability" === lit("In Stock"))
        .orderBy($"Price".asc_nulls_last)
      val parsedHopsUnavailabvle = allHopsDF
        .filter($"Parsed" === lit("parsed") && $"Availability" === lit("Out of Stock"))
        .orderBy($"Price".asc_nulls_last)
      val unparsedHops = allHopsDF
        .filter($"Parsed" =!= lit("parsed"))
        .orderBy($"Product".asc_nulls_last)
      parsedHopsAvailabvle.write
        .format("com.crealytics.spark.excel")
        .option("dataAddress", "'Available Hop Prices'!A1")
        .option("useHeader", "true")
        .mode("append")
        .save(path + "hop_comparison.xlsx")
      parsedHopsUnavailabvle.write
        .format("com.crealytics.spark.excel")
        .option("dataAddress", "'Unavailable Hop Prices'!A1")
        .option("useHeader", "true")
        .mode("append")
        .save(path + "hop_comparison.xlsx")
      unparsedHops.write
        .format("com.crealytics.spark.excel")
        .option("dataAddress", "'Unparsed Hops'!A1")
        .option("useHeader", "true")
        .mode("append")
        .save(path + "hop_comparison.xlsx")
      println("Success!")
    } else {
      println(raw"Unrecognized write format $format, writing parquet to: " + path + filename)
      allHopsDF.write.mode("append").parquet(path+filename)
      println("Success!")
    }
  }
}
