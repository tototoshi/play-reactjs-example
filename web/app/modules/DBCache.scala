package modules

import java.sql.Connection
import javax.inject.{ Inject, Provider }

import com.github.tototoshi.dbcache.postgresql.PostgreSQLCache
import com.github.tototoshi.dbcache.{ ConnectionFactory, DBCache }
import com.google.inject.Singleton
import play.api.Logger
import play.api.cache.CacheApi
import scalikejdbc.ConnectionPool

import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

@Singleton
class DBCacheApi(cache: DBCache) extends CacheApi {
  private val logger = Logger(classOf[DBCacheApi])
  def set(key: String, value: Any, expiration: Duration): Unit = try {
    cache.set(key, value, expiration)
  } catch {
    case e: Throwable => logger.error(e.getMessage, e)
  }

  def get[A](key: String)(implicit ct: ClassTag[A]): Option[A] = {
    cache.get[A](key)
  }
  def getOrElse[A: ClassTag](key: String, expiration: Duration)(orElse: => A) =
    cache.getOrElse(key, expiration)(orElse)
  def remove(key: String) = cache.remove(key)
}

class CacheApiProvider @Inject() (connectionPool: ConnectionPool) extends Provider[CacheApi] {
  val connectionFactory = new ConnectionFactory {
    override def get(): Connection = {
      val connection = connectionPool.borrow()
      connection.setReadOnly(false)
      connection
    }
  }
  override def get(): CacheApi = {
    val postgresqlCache = new PostgreSQLCache(connectionFactory)
    new DBCacheApi(postgresqlCache)
  }
}

