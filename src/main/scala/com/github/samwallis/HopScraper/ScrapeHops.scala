package com.github.samwallis.HopScraper

import com.github.samwallis.HopScraper.ScrapingFunctions._

object ScrapeHops extends App with SparkSessionWrapper {
  val arglist = args.toList
  type OptionMap = Map[Symbol, Any]

  def nextOption(map: OptionMap, list: List[String]): OptionMap = {
    def isSwitch(s: String) = (s(0) == '-')
    list match {
      case Nil => map
      case "-outdir" :: value :: tail =>
        nextOption(map ++ Map('outdir -> value.toString), tail)
      case option :: tail => {
        println("Unknown option " + option)
        System.exit(1)
        Map()
      }
    }
  }
  val options = nextOption(Map(), arglist)
  val outputPath: String =
    if (options.contains('outdir)) options('outdir).toString else "./"

  //get all product pages
  println("Getting all product page URLs...")
  val base_url: String = "https://www.morebeer.com"
  val main_page: String = "https://www.morebeer.com/category/hops-pellet.html"
  val product_pages: Array[String] = getAllProductPages(main_page, base_url)
  println("Found: " + product_pages.length)

  //get all product items from those pages
  println("Getting all product item page URLs...")
  val all_products: Array[String] =
    getAllPageProductLinks(product_pages, base_url)
  println("Found: " + all_products.length)

  //for each product item, get static attributes
  println("Getting all product item static vals...")
  val all_hops_static_vals = for (product <- all_products) yield {
    getPreAjaxHopPage(product)
  }
  val good_hops = all_hops_static_vals.filter(x => x.raw_wt_meta != null)
  for (hop <- good_hops) hop.getWtMeta()
  for (hop <- good_hops) hop.getWtData()
  val bad_hops = all_hops_static_vals.filter(x => x.raw_wt_meta == null)
  println("Parsed item count: " + good_hops.length)
  println("Unparsed item count: " + bad_hops.length)

  println("Creating row data...")
  for (hop <- good_hops) hop.getHopRows()
  for (hop <- bad_hops) hop.getHopRows()

  println("Building output...")
  val writer = new OutputWriter(good_hops ++ bad_hops)
  writer.write(outputPath)
}
