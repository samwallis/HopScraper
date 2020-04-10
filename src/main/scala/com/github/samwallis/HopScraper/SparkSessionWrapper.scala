package com.github.samwallis.HopScraper

import org.apache.spark.sql.SparkSession

trait SparkSessionWrapper {

  lazy val spark: SparkSession = {
    SparkSession
      .builder()
      .master("local[*]")
      .appName("HopScraper")
      .config("spark.sql.session.timeZone", "UTC")
      .getOrCreate()
  }

}
