package com.gu.mysterybird.snippet

import com.gu.openplatform.contentapi.Api
import net.liftweb.util._
import net.liftweb.http.{S, RequestVar}
import com.gu.openplatform.contentapi.model.{SearchResponse, Content}
import java.util.Properties
import Helpers._
import appenginehelpers.HybridCache
import com.google.appengine.api.memcache.Expiration

//SearchResponse cannot be cached as it is not serializable
//so just pop response in this case class
case class SearchResult(totalPages: Int, content: List[Content])

class Birds extends HybridCache {

  //http://farm6.static.flickr.com/5243/5229400272_3140317214.jpg
  val ImageRegex = """.*<img src="(http://[\w\d/\.-_]*)"\s*width=.*""".r

  object currentPage extends RequestVar[Int]({
    Integer.parseInt(S.param("page").getOrElse("1"))
  })

  object searchResult extends RequestVar[SearchResult]({

    Option(cache.get(currentPage.get)) match {
      case Some(result) => result.asInstanceOf[SearchResult]
      case None => {
        Api.apiKey = Birds.apiKey
        val result = Api.search.page(currentPage.get).pageSize(12)
          .tag("profile/grrlscientist").q("\"today's mystery bird\"").showFields("body,trailText")

        val cacheableResult = SearchResult(result.pages, result.results)
        cache.put(currentPage.get, cacheableResult, Expiration.byDeltaSeconds(300))
        cacheableResult
      }
    }
  })

  object repository extends RequestVar[List[Content]] ({
      searchResult.content.filter(c => c.fields.get.get("body").getOrElse("") match {
        case ImageRegex(a) => true
        case _ => false
    })
  })

  def navigation = ((currentPage.get match {
    case p if p < searchResult.totalPages => ".next [href]" #> ("?page=" + (p + 1).toString)
    case _ => ".next [class]" #> "disabled"
  })
  & (currentPage.get match {
    case p if p > 1 => ".previous [href]" #> ("?page=" + (p - 1).toString)
    case _ => ".previous [class]" #> "disabled"
  }))

  def content = "*" #> repository.get.map(c =>
    ".link [href]" #> c.webUrl
    & ".date *" #> c.webPublicationDate.toString("dd MMM yyyy")
    & ".bird [style]" #> (c.fields.get.get("body").get match {
      case ImageRegex(imgUrl) => "background-image:url(" + imgUrl + ")"
      case _ => ""
    })
    & ".link [title]" #> c.fields.get.get("trailText").get
  )
}

object Birds {
  lazy val apiKey = {
    val props = new Properties()
    props.load(getClass.getResourceAsStream("settings.properties"))
    Some(props.getProperty("apiKey"))
  }
}