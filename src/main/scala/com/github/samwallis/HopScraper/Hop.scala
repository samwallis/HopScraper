package com.github.samwallis.HopScraper

import com.github.samwallis.HopScraper.ScrapingFunctions._
import org.apache.spark.sql.Row

class Hop(
    hop_url: String,
    hop_name: String,
    attribute_item_id: String,
    availiable_weights: Array[(String, String)]
) extends SparkSessionWrapper {
  val url: String = hop_url
  val name: String = hop_name
  val ajax_attr_item_id: String = attribute_item_id
  var ajax_function: String = "updateAttributes"
  val raw_wt_meta: Array[(String, String)] = availiable_weights
  var wt_meta: Array[(String, String)] = null
  //this is wt, price, availability:
  var wt_data: Array[(String, String, String)] = null
  var hop_rows: Seq[Row] = Seq()

  def getWtMeta(): Unit = {
    wt_meta = raw_wt_meta.map(x => urlEncodeWtMetadata(x, ajax_attr_item_id))
  }

  def getWtData(): Unit = {
    wt_data = wt_meta.map(x => getHopPrice(url, x._1, ajax_function, x._2))
  }

  def getHopRows(): Unit = {
    val dt = java.time.LocalDate.now().toString()
    if (wt_data != null) {
      for (wt <- wt_data) {
        val new_row = Row(name, wt._1, wt._2.toDouble, wt._3, dt, url, "parsed")
        hop_rows = hop_rows :+ new_row
      }
    } else {
      hop_rows = Seq(Row(name, null, null, null, dt, url, "unparsed"))
    }
  }
}
