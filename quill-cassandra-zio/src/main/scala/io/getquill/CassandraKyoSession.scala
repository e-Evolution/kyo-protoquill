package io.getquill

import com.datastax.oss.driver.api.core.CqlSession
import com.typesafe.config.Config
import io.getquill.context.{AsyncFutureCache, CassandraSession, SyncCache}
import io.getquill.util.LoadConfig
import kyo.*
import scala.tools.nsc.interpreter.Naming.sessionNames

case class CassandraKyoSession(
  override val session:                    CqlSession,
  override val preparedStatementCacheSize: Long
) extends CassandraSession with SyncCache with AsyncFutureCache with AutoCloseable

object CassandraKyoSession {
  val live: Env.Layer[CassandraContextConfig, CassandraKyoSession] =
    Env.Layer.from {
      for {
        config <- Env.get[CassandraContextConfig]
        session <- Scope.acquireRelease {
          IO(CassandraKyoSession(config.session, config.preparedStatementCacheSize))
        } { s => IO(s.session.close()) }
      } yield session
    }

  def fromContextConfig(config: CassandraContextConfig): Env.Layer[Any, CassandraKyoSession] =
    Env.Layer.succeed(config).chain(live)

  def fromConfig(config: Config) = fromContextConfig(CassandraContextConfig(config))
  def fromPrefix(configPrefix: String) = fromContextConfig(CassandraContextConfig(LoadConfig(configPrefix)))
}
