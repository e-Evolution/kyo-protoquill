package io.getquill

import io.getquill.NamingStrategy
import io.getquill.{PostgresDialect, SQLServerDialect, H2Dialect, MySQLDialect, SqliteDialect, OracleDialect, JdbcContextConfig}
import io.getquill.jdbckyo.{Quill, QuillBaseContext}
import io.getquill.context.jdbc.{H2JdbcTypes, MysqlJdbcTypes, OracleJdbcTypes, PostgresJdbcTypes, SqlServerJdbcTypes, SqliteJdbcTypes}
import io.getquill.context.json.PostgresJsonExtensions

class PostgresKyoJdbcContext[+N <: NamingStrategy](val naming: N, val ds: javax.sql.DataSource)
  extends Quill[PostgresDialect, N] with PostgresJdbcTypes[PostgresDialect, N] with PostgresJsonExtensions {
  val idiom: PostgresDialect = PostgresDialect
}

class SqlServerKyoJdbcContext[+N <: NamingStrategy](val naming: N, val ds: javax.sql.DataSource)
  extends Quill[SQLServerDialect, N] with SqlServerJdbcTypes[SQLServerDialect, N] {
  val idiom: SQLServerDialect = SQLServerDialect
}

class H2KyoJdbcContext[+N <: NamingStrategy](val naming: N, val ds: javax.sql.DataSource)
  extends Quill[H2Dialect, N] with H2JdbcTypes[H2Dialect, N] {
  val idiom: H2Dialect = H2Dialect
}

class MysqlKyoJdbcContext[+N <: NamingStrategy](val naming: N, val ds: javax.sql.DataSource)
  extends Quill[MySQLDialect, N] with MysqlJdbcTypes[MySQLDialect, N] {
  val idiom: MySQLDialect = MySQLDialect
}

class SqliteKyoJdbcContext[+N <: NamingStrategy](val naming: N, val ds: javax.sql.DataSource)
  extends Quill[SqliteDialect, N] with SqliteJdbcTypes[SqliteDialect, N] {
  val idiom: SqliteDialect = SqliteDialect
}

class OracleKyoJdbcContext[+N <: NamingStrategy](val naming: N, val ds: javax.sql.DataSource)
  extends Quill[OracleDialect, N] with OracleJdbcTypes[OracleDialect, N] {
  val idiom: OracleDialect = OracleDialect
}
