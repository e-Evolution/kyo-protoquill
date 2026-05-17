package io.getquill

import io.getquill.context.qkyo.{KyoJdbcContext, KyoJdbcUnderlyingContext}
import io.getquill.context.jdbc.{H2JdbcTypes, MysqlJdbcTypes, OracleJdbcTypes, PostgresJdbcTypes, SqlServerJdbcTypes, SqliteJdbcTypes}
import io.getquill.context.json.PostgresJsonExtensions

/**
 * Effectful JDBC context classes for each dialect.
 * These extend KyoJdbcContext and return Kyo effects (QIO/QCIO).
 *
 * For the synchronous equivalents, see KyoJdbcContexts.scala
 * (PostgresKyoJdbcContext, H2KyoJdbcContext, etc.)
 */

// ─── Postgres ────────────────────────────────────────────

class PostgresKyoJdbcEffectContext[+N <: NamingStrategy](val naming: N)
  extends KyoJdbcContext[PostgresDialect, N]
  with PostgresJdbcTypes[PostgresDialect, N]
  with PostgresJsonExtensions {
  val idiom: PostgresDialect = PostgresDialect
  val connDelegate: PostgresKyoJdbcEffectContext.Underlying[N] = new PostgresKyoJdbcEffectContext.Underlying[N](naming)
}
object PostgresKyoJdbcEffectContext {
  class Underlying[+N <: NamingStrategy](val naming: N)
    extends KyoJdbcUnderlyingContext[PostgresDialect, N]
    with PostgresJdbcTypes[PostgresDialect, N]
    with PostgresJsonExtensions {
    val idiom: PostgresDialect = PostgresDialect
  }
}

// ─── SqlServer ───────────────────────────────────────────

class SqlServerKyoJdbcEffectContext[+N <: NamingStrategy](val naming: N)
  extends KyoJdbcContext[SQLServerDialect, N]
  with SqlServerJdbcTypes[SQLServerDialect, N] {
  val idiom: SQLServerDialect = SQLServerDialect
  val connDelegate: SqlServerKyoJdbcEffectContext.Underlying[N] = new SqlServerKyoJdbcEffectContext.Underlying[N](naming)
}
object SqlServerKyoJdbcEffectContext {
  class Underlying[+N <: NamingStrategy](val naming: N)
    extends KyoJdbcUnderlyingContext[SQLServerDialect, N]
    with SqlServerJdbcTypes[SQLServerDialect, N] {
    val idiom: SQLServerDialect = SQLServerDialect
  }
}

// ─── H2 ──────────────────────────────────────────────────

class H2KyoJdbcEffectContext[+N <: NamingStrategy](val naming: N)
  extends KyoJdbcContext[H2Dialect, N]
  with H2JdbcTypes[H2Dialect, N] {
  val idiom: H2Dialect = H2Dialect
  val connDelegate: H2KyoJdbcEffectContext.Underlying[N] = new H2KyoJdbcEffectContext.Underlying[N](naming)
}
object H2KyoJdbcEffectContext {
  class Underlying[+N <: NamingStrategy](val naming: N)
    extends KyoJdbcUnderlyingContext[H2Dialect, N]
    with H2JdbcTypes[H2Dialect, N] {
    val idiom: H2Dialect = H2Dialect
  }
}

// ─── MySQL ───────────────────────────────────────────────

class MysqlKyoJdbcEffectContext[+N <: NamingStrategy](val naming: N)
  extends KyoJdbcContext[MySQLDialect, N]
  with MysqlJdbcTypes[MySQLDialect, N] {
  val idiom: MySQLDialect = MySQLDialect
  val connDelegate: MysqlKyoJdbcEffectContext.Underlying[N] = new MysqlKyoJdbcEffectContext.Underlying[N](naming)
}
object MysqlKyoJdbcEffectContext {
  class Underlying[+N <: NamingStrategy](val naming: N)
    extends KyoJdbcUnderlyingContext[MySQLDialect, N]
    with MysqlJdbcTypes[MySQLDialect, N] {
    val idiom: MySQLDialect = MySQLDialect
  }
}

// ─── SQLite ──────────────────────────────────────────────

class SqliteKyoJdbcEffectContext[+N <: NamingStrategy](val naming: N)
  extends KyoJdbcContext[SqliteDialect, N]
  with SqliteJdbcTypes[SqliteDialect, N] {
  val idiom: SqliteDialect = SqliteDialect
  val connDelegate: SqliteKyoJdbcEffectContext.Underlying[N] = new SqliteKyoJdbcEffectContext.Underlying[N](naming)
}
object SqliteKyoJdbcEffectContext {
  class Underlying[+N <: NamingStrategy](val naming: N)
    extends KyoJdbcUnderlyingContext[SqliteDialect, N]
    with SqliteJdbcTypes[SqliteDialect, N] {
    val idiom: SqliteDialect = SqliteDialect
  }
}

// ─── Oracle ──────────────────────────────────────────────

class OracleKyoJdbcEffectContext[+N <: NamingStrategy](val naming: N)
  extends KyoJdbcContext[OracleDialect, N]
  with OracleJdbcTypes[OracleDialect, N] {
  val idiom: OracleDialect = OracleDialect
  val connDelegate: OracleKyoJdbcEffectContext.Underlying[N] = new OracleKyoJdbcEffectContext.Underlying[N](naming)
}
object OracleKyoJdbcEffectContext {
  class Underlying[+N <: NamingStrategy](val naming: N)
    extends KyoJdbcUnderlyingContext[OracleDialect, N]
    with OracleJdbcTypes[OracleDialect, N] {
    val idiom: OracleDialect = OracleDialect
  }
}
