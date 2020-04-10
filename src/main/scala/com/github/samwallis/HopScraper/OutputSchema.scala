package com.github.samwallis.HopScraper

import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}

object OutputSchema {

  val hopSchema = StructType(
    Seq(
      StructField("Product",
        StringType,
        false),
      StructField("Weight",
        StringType,
        true),
      StructField("Price",
        DoubleType,
        true),
      StructField("Availability",
        StringType,
        true),
      StructField("Date",
        StringType,
        false),
      StructField("URL",
        StringType,
        false),
      StructField("Parsed",
        StringType,
        false)
    ))
}
