import sbt._

class Project(info: ProjectInfo) extends DefaultWebProject(info) {

  val liftSnapshots = "Scala tools" at "http://scala-tools.org/repo-snapshots"
  val liftVersion = "2.2-SNAPSHOT"

  val githubMvn = "Github repository" at "http://mvn.github.com/repository"

  val lift = "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default" withSources()
  val liftCommon = "net.liftweb" %% "lift-common" % liftVersion % "compile->default" withSources()
  val liftMapper = "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default" withSources()

  val guardianGithub = "Guardian Github Releases" at "http://guardian.github.com/maven/repo-releases"
  val contentApiClient = "com.gu.openplatform" %% "content-api-client" % "1.6" withSources()

  val jettyServer = "org.mortbay.jetty" % "jetty" % "6.1.25" % "test->default"
}