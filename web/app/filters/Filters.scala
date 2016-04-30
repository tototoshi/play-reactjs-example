package filters

import javax.inject.Inject

import play.api.http.HttpFilters
import play.api.mvc.Filter

class Filters @Inject() (log: RequestLoggingFilter) extends HttpFilters {
  val filters: Seq[Filter] = Seq(log)
}
