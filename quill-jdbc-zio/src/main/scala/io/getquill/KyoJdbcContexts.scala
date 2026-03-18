package io.getquill

import com.typesafe.config.Config
import io.getquill.context.jdbc.{H2JdbcTypes, MysqlJdbcTypes, OracleJdbcTypes, PostgresJdbcTypes, SqlServerExecuteOverride, SqlServerJdbcTypes, SqliteJdbcTypes}
import io.getquill.context.sql.idiom.SqlIdiom
import io.getquill.context.qzio.{KyoJdbcContext, KyoJdbcUnderlyingContext}
import io.getquill.util.LoadConfig

import javax.sql.DataSource
import io.getquill.context.json.PostgresJsonExtensions

class PostgresKyoJdbcContext[+N <: NamingStrategy](val naming: N)
  extends KyoJdbcContext[PostgresDialect, N]
  with PostgresJdbcTypes[PostgresDialect, N]
  with PostgresJsonExtensions {
  val idiom: PostgresDialect = PostgresDialect

  val connDelegate: KyoJdbcUnderlyingContext[PostgresDialect, N] = new PostgresKyoJdbcContext.Underlying[N](naming)
}
object PostgresKyoJdbcContext {
  class Underlying[+N <: NamingStrategy](val naming: N)
    extends KyoJdbcUnderlyingContext[PostgresDialect, N]
    with PostgresJdbcTypes[PostgresDialect, N]
    with PostgresJsonExtensions {
    val idiom: PostgresDialect = PostgresDialect
  }
}

class SqlServerKyoJdbcContext[+N <: NamingStrategy](val naming: N)
  extends KyoJdbcContext[SQLServerDialect, N]
  with SqlServerJdbcTypes[SQLServerDialect, N] {
  val idiom: SQLServerDialect = SQLServerDialect

  val connDelegate: KyoJdbcUnderlyingContext[SQLServerDialect, N] = new SqlServerKyoJdbcContext.Underlying[N](naming)
}

object SqlServerKyoJdbcContext {
  class Underlying[+N <: NamingStrategy](val naming: N)
    extends KyoJdbcUnderlyingContext[SQLServerDialect, N]
    with SqlServerJdbcTypes[SQLServerDialect, N]
    with SqlServerExecuteOverride[N] {
    val idiom: SQLServerDialect = SQLServerDialect
  }
}

class H2KyoJdbcContext[+N <: NamingStrategy](val naming: N)
  extends KyoJdbcContext[H2Dialect, N]
  with H2JdbcTypes[H2Dialect, N] {
  val idiom: H2Dialect = H2Dialect

  val connDelegate: KyoJdbcUnderlyingContext[H2Dialect, N] = new H2KyoJdbcContext.Underlying[N](naming)
}
object H2KyoJdbcContext {
  class Underlying[+N <: NamingStrategy](val naming: N)
    extends KyoJdbcUnderlyingContext[H2Dialect, N]
    with H2JdbcTypes[H2Dialect, N] {
    val idiom: H2Dialect = H2Dialect
  }
}

class MysqlKyoJdbcContext[+N <: NamingStrategy](val naming: N)
  extends KyoJdbcContext[MySQLDialect, N]
  with MysqlJdbcTypes[MySQLDialect, N] {
  val idiom: MySQLDialect = MySQLDialect

  val connDelegate: KyoJdbcUnderlyingContext[MySQLDialect, N] = new MysqlKyoJdbcContext.Underlying[N](naming)
}
object MysqlKyoJdbcContext {
  class Underlying[+N <: NamingStrategy](val naming: N)
    extends KyoJdbcUnderlyingContext[MySQLDialect, N]
    with MysqlJdbcTypes[MySQLDialect, N] {
    val idiom: MySQLDialect = MySQLDialect
  }
}

class SqliteKyoJdbcContext[+N <: NamingStrategy](val naming: N)
  extends KyoJdbcContext[SqliteDialect, N]
  with SqliteJdbcTypes[SqliteDialect, N] {
  val idiom: SqliteDialect = SqliteDialect

  val connDelegate: KyoJdbcUnderlyingContext[SqliteDialect, N] = new SqliteKyoJdbcContext.Underlying[N](naming)
}
object SqliteKyoJdbcContext {
  class Underlying[+N <: NamingStrategy](val naming: N)
    extends KyoJdbcUnderlyingContext[SqliteDialect, N]
    with SqliteJdbcTypes[SqliteDialect, N] {
    val idiom: SqliteDialect = SqliteDialect
  }
}

class OracleKyoJdbcContext[+N <: NamingStrategy](val naming: N)
  extends KyoJdbcContext[OracleDialect, N]
  with OracleJdbcTypes[OracleDialect, N] {
  val idiom: OracleDialect = OracleDialect

  val connDelegate: KyoJdbcUnderlyingContext[OracleDialect, N] = new OracleKyoJdbcContext.Underlying[N](naming)
}
object OracleKyoJdbcContext {
  class Underlying[+N <: NamingStrategy](val naming: N)
    extends KyoJdbcUnderlyingContext[OracleDialect, N]
    with OracleJdbcTypes[OracleDialect, N] {
    val idiom: OracleDialect = OracleDialect
  }
}

trait WithProbing[D <: SqlIdiom, +N <: NamingStrategy] extends KyoJdbcUnderlyingContext[D, N] {
  def probingConfig: Config
  override def probingDataSource: Option[DataSource] = Some(JdbcContextConfig(probingConfig).dataSource)
}

trait WithProbingPrefix[D <: SqlIdiom, +N <: NamingStrategy] extends WithProbing[D, N] {
  def configPrefix: String
  def probingConfig: Config = LoadConfig(configPrefix)
}
