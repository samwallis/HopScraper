package com.github.samwallis.HopScraper

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import org.jsoup.{Connection, Jsoup}
import org.jsoup.nodes.{Document}
import org.jsoup.select.Elements

import scala.collection.JavaConversions._
import scala.collection.mutable

object ScrapingFunctions {
  //TODO clean up naming conventions

  //function to get pages 1-4+ of products
  def getAllProductPages(main_page: String, base_url: String): Array[String] = {
    var product_pages: Array[String] = Array()
    var unseen_pages: Array[String] = Array(main_page)
    while (unseen_pages.length != 0) {
      val page = unseen_pages(0)
      //println("Getting " + page)
      val page_connection: Connection = Jsoup.connect(page).timeout(0)
      val page_num_elements: Elements =
        page_connection.get().body().select(".pagenumbering")
      val all_page_num_links: mutable.Buffer[String] =
        for (elem <- page_num_elements) yield {
          elem.attr("href")
        }
      val page_num_links: Array[String] = all_page_num_links.toArray
        .filter(x => x != "")
        .map(x => base_url + x)
        .distinct
      product_pages = (product_pages :+ page).distinct
      unseen_pages = page_num_links.diff(product_pages)
    }
    product_pages
  }

  //get all product links from product page
  def getPageProductLinks(page: String, base_url: String): Array[String] = {
    //println("Getting page " + page)
    val page_connection: Connection = Jsoup.connect(page).timeout(0)
    val product_link_elements: Elements =
      page_connection.get().body().select(".col-xs-12 .row .text-center a")
    val all_product_urls: mutable.Buffer[String] =
      for (elem <- product_link_elements) yield { elem.attr("href") }
    all_product_urls.toArray
      .filter(x => x != "#")
      .map(x => base_url + x)
      .distinct
  }

  def getAllPageProductLinks(product_pages: Array[String],
                             base_url: String): Array[String] = {
    var all_product_page_links: Array[String] = Array()
    for (product_page <- product_pages) {
      all_product_page_links = all_product_page_links ++ getPageProductLinks(
        product_page,
        base_url)
    }
    all_product_page_links.distinct
  }

  //get all the static elements from a hop page
  def getPreAjaxHopPage(url: String): Hop = {
    //println("Getting page " + url)
    val conn = Jsoup.connect(url).timeout(0)
    val hop_page = conn.get()
    val hop_title =
      hop_page.body().select("h1.title[itemprop=name]").first().text()
    val attribute_item_id_elem =
      hop_page.body().select("input[name=attribute_item_id]")
    val attribute_item_id =
      if (attribute_item_id_elem.toArray.length > 0)
        attribute_item_id_elem.first().attr("value")
      else null
    val all_weight_elems =
      hop_page.body().select("form[id=attform] div label[for] input")
    val weight_metadata: Array[(String, String)] =
      if (all_weight_elems.toArray.length > 0) {
        (for (elem <- all_weight_elems) yield {
          (elem.attr("name"), elem.attr("value"))
        }).toArray.asInstanceOf[Array[(String, String)]]
      } else { null }
    new Hop(url, hop_title, attribute_item_id, weight_metadata)
  }

  //get the price for a given hop page
  def getHopPrice(url: String,
                  label: String,
                  xajax: String,
                  xajaxargs: String): (String, String, String) = {
    val hop_page_connection: Connection = Jsoup.connect(url).timeout(0)
    val hop_page_connection_with_params: Connection = hop_page_connection
      .data("xajax", xajax)
      .data("xajaxargs[]", xajaxargs)
    val hopPageDoc: Document = hop_page_connection_with_params.post()
    val parsed = parseHopXML(hopPageDoc)
    (label, parsed._1, parsed._2)
  }

  def parseHopXML(doc: Document): (String, String) = {
    val cdata_string: String = Jsoup
      .parse(doc.outerHtml())
      .body()
      .children()
      .first()
      .children()
      .first()
      .text()
    val innnerDoc = Jsoup.parse(cdata_string)
    val offers: Elements = innnerDoc.body().select("div[itemprop=offers]")
    val price: String = offers.select("span[itemprop='price']").text()
    val cartform: Elements = innnerDoc.body().select("#cartform")
    val availability: String =
      if (cartform.select("button[id=add-to-cart-button]").hasText()) "In Stock"
      else "Out of Stock"
    (price, availability)
  }

  def urlEncodeWtMetadata(wt_meta: (String, String),
                          attribute_item_id: String): (String, String) = {
    val attribute_id: String = wt_meta._1
    val encodedWeightVal =
      URLEncoder.encode(wt_meta._2, StandardCharsets.UTF_8.toString)
    val ajax_query_string =
      raw"<xjxquery><q>attribute_item_id=$attribute_item_id&$attribute_id=$encodedWeightVal</q></xjxquery>"
    val tags_regex = "\\<.*?\\>".r
    val clean_wt_label =
      tags_regex.replaceAllIn(wt_meta._2, "").replace("+", " ")
    (clean_wt_label, ajax_query_string)
  }

}
