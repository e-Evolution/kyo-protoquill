package io.getquill

import com.datastax.oss.driver.api.core.CqlSession
import com.typesafe.config.Config
import io.getquill.context.{AsyncFutureCache, CassandraSession, SyncCache}
import io.getquill.util.LoadConfig

case class CassandraZioSession(
  override val session:                    CqlSession,
  override val preparedStatementCacheSize: Long
) extends CassandraSession with SyncCache with AsyncFutureCache with AutoCloseable

object CassandraZioSession {
  def fromContextConfig(config: CassandraContextConfig): CassandraZioSession =
    CassandraZioSession(config.session, config.preparedStatementCacheSize)

  def fromConfig(config: Config) = fromContextConfig(CassandraContextConfig(config))
  def fromPrefix(configPrefix: String) = fromContextConfig(CassandraContextConfig(LoadConfig(configPrefix)))
}
