package com.gu.mysterybird

import com.gu.openplatform.contentapi.model.Content
import com.gu.openplatform.contentapi.Api
import net.liftweb.http.{S, RequestVar}
import java.util.Properties
import appenginehelpers.HybridCache

//SearchResponse cannot be cached as it is not serializable
//so just pop response in this case class
case class SearchResult(totalPages: Int, content: List[Content])

object Repository extends HybridCache {
  //designed to match a url that looks like...
  //http://farm6.static.flickr.com/5243/5229400272_3140317214.jpg
  val ImageRegex = """.*<img src="(http://[\w\d/\.-_]*)"\s*width=.*""".r

  lazy val apiKey = {
    /*
    if you are reading this you probably need to...
    create the following file...
    mkdir -p src/main/resources/com/gu/mysterybird
    touch src/main/resources/com/gu/mysterybird/settings.properties
    and then set apiKey=PARTNER-LEVEL-KEY in it
    this file is ignored by git
     */
    val props = new Properties()
    props.load(getClass.getResourceAsStream("settings.properties"))
    Some(props.getProperty("apiKey"))
  }

  object currentPage extends RequestVar[Int]({
    Integer.parseInt(S.param("page").getOrElse("1"))
  })

  object searchResult extends RequestVar[SearchResult]({

    Option(cache.get(currentPage.get)) match {
      case Some(result) => result.asInstanceOf[SearchResult]
      case None => {
        Api.apiKey = apiKey
        val result = Api.search.page(currentPage.get).pageSize(12)
          .tag("profile/grrlscientist").q("\"today's mystery bird\"").showFields("body,trailText")

        val cacheableResult = SearchResult(result.pages, result.results)
        cache.put(currentPage.get, cacheableResult, 300 seconds)
        cacheableResult
      }
    }
  })

  object fetch extends RequestVar[List[Content]] ({
      searchResult.content.filter(c => c.fields.get.get("body").getOrElse("") match {
        case ImageRegex(a) => true
        case _ => false
    })
  })
}