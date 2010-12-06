package com.gu.mysterybird.snippet

import com.gu.openplatform.contentapi.Api
import net.liftweb.util._
import Helpers._
import xml.Unparsed
import net.liftweb.http.{S, RequestVar}
import com.gu.openplatform.contentapi.model.{SearchResponse, Content}
import java.util.Properties
import java.lang.String


class Birds {

  Birds.apiKey

   //http://farm6.static.flickr.com/5243/5229400272_3140317214.jpg
  val ImageRegex = """.*<img src="(http://[\w\d/\.-_]*)"\s*width=.*""".r

  object currentPage extends RequestVar[Int]({
    Integer.parseInt(S.param("page").getOrElse("1"))
  })

  object searchResult extends RequestVar[SearchResponse]({
    Api.apiKey = Birds.apiKey
    Api.search.page(currentPage.get).pageSize(12)
      .tag("profile/grrlscientist").q("\"today's mystery bird\"").showFields("body,trailText")
  })

  object repository extends RequestVar[List[Content]] ({
      searchResult.results
      .filter(c => c.fields.get.get("body").isDefined)
      .filter(c => c.fields.get.get("body").get match {
        case ImageRegex(a) => true
        case _ => false
    })

  })


  def navigation = ((currentPage.get match {
    case p if p < searchResult.get.pages => ".next [href]" #> ("?page=" + (p + 1).toString)
    case _ => ".next [class]" #> "disabled"
  })
  & (currentPage.get match {
    case p if p > 1 => ".previous [href]" #> ("?page=" + (p - 1).toString)
    case _ => ".previous [class]" #> "disabled"
  }))

  def content = "#article" #> repository.get.map(c =>
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