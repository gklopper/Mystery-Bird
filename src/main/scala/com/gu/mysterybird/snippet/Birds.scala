package com.gu.mysterybird.snippet

import net.liftweb.util._
import Helpers._
import appenginehelpers.HybridCache
import com.gu.mysterybird.Repository


class Birds extends HybridCache {

  def navigation = ((Repository.currentPage.get match {
      case p if p < Repository.searchResult.totalPages => ".next [href]" #> ("?page=" + (p + 1).toString)
      case _ => ".next [class]" #> "disabled"
    })
    & (Repository.currentPage.get match {
      case p if p > 1 => ".previous [href]" #> ("?page=" + (p - 1).toString)
      case _ => ".previous [class]" #> "disabled"
    })
  )

  def content = "*" #> Repository.fetch.get.map(c =>
    ".link [href]" #> c.webUrl
    & ".date *" #> c.webPublicationDate.toString("dd MMM yyyy")
    & ".bird [style]" #> (c.fields.get.get("body").get match {
      case Repository.ImageRegex(imgUrl) => "background-image:url(" + imgUrl + ")"
      case _ => ""
    })
    & ".link [title]" #> c.fields.get.get("trailText").getOrElse("").replace("<p>", "").replace("</p>","")
  )
}