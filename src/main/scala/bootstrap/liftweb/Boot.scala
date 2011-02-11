package bootstrap.liftweb

import net.liftweb.http._
import net.liftweb.util.Helpers
import net.liftweb.common.Empty

class Boot {

  def boot {
    LiftRules.addToPackages("com.gu.mysterybird")

    LiftRules.early.append(r => r.setCharacterEncoding("UTF-8"))

    LiftRules.defaultHeaders = {
     case _ =>
        List("Date" -> Helpers.nowAsInternetDate, "Cache-Control" -> "public, max-age=300")
    }

    LiftRules.autoIncludeAjax = _ => false

    LiftRules.autoIncludeComet = _ => false

    //these are needed to work in Appengine
    LiftRules.enableContainerSessions = false
    LiftRules.getLiftSession = req => new LiftSession(req.contextPath, "dummySession", Empty)
    LiftRules.sessionCreator = (i1, i2) => error("no sessions here please")

    //all paths are stateless (removes lift_page JS on bottom of page
    LiftRules.statelessTest.prepend({case _ => true})

  }
}