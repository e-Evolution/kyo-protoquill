package io.getquill

import com.datastax.oss.driver.api.core.CqlSession
import com.typesafe.config.Config
import io.getquill.context.{AsyncFutureCache, CassandraSession, SyncCache}
import io.getquill.util.LoadConfig
import kyo.*

case class CassandraKyoSession(
  override val session:                    CqlSession,
  override val preparedStatementCacheSize: Long
) extends CassandraSession with SyncCache with AsyncFutureCache with AutoCloseable

object CassandraKyoSession {
  def fromContextConfig(config: CassandraContextConfig): CassandraKyoSession =
    CassandraKyoSession(config.session, config.preparedStatementCacheSize)

  def fromConfig(config: Config) = fromContextConfig(CassandraContextConfig(config))
  def fromPrefix(configPrefix: String) = fromContextConfig(CassandraContextConfig(LoadConfig(configPrefix)))
}
