package modules

import play.api.cache.CacheApi
import play.api.{ Configuration, Environment }
import play.api.inject.{ Binding, Module }
import scalikejdbc.ConnectionPool

class ApplicationModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind[ConnectionPool].toProvider[ConnectionPoolProvider].eagerly(),
    bind[ConnectionPoolShutdown].toSelf.eagerly(),

    bind[CacheApi].toProvider[CacheApiProvider].eagerly()
  )
}
