package io.getquill.jdbckyo

import io.getquill._
import io.getquill.context.jdbc.{H2JdbcTypes, MysqlJdbcTypes, OracleJdbcTypes, PostgresJdbcTypes, SqlServerJdbcTypes, SqliteJdbcTypes}
import java.sql.Connection
import scala.annotation.targetName

class Postgres[+N <: NamingStrategy](val naming: N, override val ds: javax.sql.DataSource)
    extends Quill[PostgresDialect, N] with PostgresJdbcTypes[PostgresDialect, N] {
  val idiom: PostgresDialect = PostgresDialect
}

class SqlServer[+N <: NamingStrategy](val naming: N, override val ds: javax.sql.DataSource)
    extends Quill[SQLServerDialect, N] with SqlServerJdbcTypes[SQLServerDialect, N] {
  val idiom: SQLServerDialect = SQLServerDialect
}

class H2[+N <: NamingStrategy](val naming: N, override val ds: javax.sql.DataSource)
    extends Quill[H2Dialect, N] with H2JdbcTypes[H2Dialect, N] {
  val idiom: H2Dialect = H2Dialect
}

class Mysql[+N <: NamingStrategy](val naming: N, override val ds: javax.sql.DataSource)
    extends Quill[MySQLDialect, N] with MysqlJdbcTypes[MySQLDialect, N] {
  val idiom: MySQLDialect = MySQLDialect
}

class Sqlite[+N <: NamingStrategy](val naming: N, override val ds: javax.sql.DataSource)
    extends Quill[SqliteDialect, N] with SqliteJdbcTypes[SqliteDialect, N] {
  val idiom: SqliteDialect = SqliteDialect
}

class Oracle[+N <: NamingStrategy](val naming: N, override val ds: javax.sql.DataSource)
    extends Quill[OracleDialect, N] with OracleJdbcTypes[OracleDialect, N] {
  val idiom: OracleDialect = OracleDialect
}

trait Quill[+Dialect <: io.getquill.context.sql.idiom.SqlIdiom, +Naming <: NamingStrategy]
  extends QuillBaseContext[Dialect, Naming]
  with io.getquill.context.jdbc.JdbcContextTypes[Dialect, Naming]
  with io.getquill.context.jdbc.JdbcContextVerbExecute[Dialect, Naming] {

  override type Result[T] = T
  override type RunQueryResult[T] = List[T]
  override type RunQuerySingleResult[T] = T
  override type RunActionResult = Long
  override type RunActionReturningResult[T] = T
  override type RunBatchActionResult = List[Long]
  override type RunBatchActionReturningResult[T] = List[T]
  override type Runner = Unit
  override protected def context: Runner = ()

  @targetName("runQueryDefault")
  inline def run[T](inline quoted: Quoted[Query[T]]): List[T] = InternalApi.runQueryDefault(quoted)
  @targetName("runQuery")
  inline def run[T](inline quoted: Quoted[Query[T]], inline wrap: OuterSelectWrap): List[T] = InternalApi.runQuery(quoted, wrap)
  @targetName("runQuerySingle")
  inline def run[T](inline quoted: Quoted[T]): T = InternalApi.runQuerySingle(quoted)
  @targetName("runAction")
  inline def run[E](inline quoted: Quoted[Action[E]]): Long = InternalApi.runAction(quoted)
  @targetName("runActionReturning")
  inline def run[E, T](inline quoted: Quoted[ActionReturning[E, T]]): T = InternalApi.runActionReturning[E, T](quoted)
  @targetName("runActionReturningMany")
  inline def run[E, T](inline quoted: Quoted[ActionReturning[E, List[T]]]): List[T] = InternalApi.runActionReturningMany[E, T](quoted)
  @targetName("runBatchAction")
  inline def run[I, A <: Action[I] & QAC[I, Nothing]](inline quoted: Quoted[BatchAction[A]], rowsPerBatch: Int): List[Long] = InternalApi.runBatchAction(quoted, rowsPerBatch)
  @targetName("runBatchActionDefault")
  inline def run[I, A <: Action[I] & QAC[I, Nothing]](inline quoted: Quoted[BatchAction[A]]): List[Long] = InternalApi.runBatchAction(quoted, 1)
  @targetName("runBatchActionReturning")
  inline def run[I, T, A <: Action[I] & QAC[I, T]](inline quoted: Quoted[BatchAction[A]], rowsPerBatch: Int): List[T] = InternalApi.runBatchActionReturning(quoted, rowsPerBatch)
  @targetName("runBatchActionReturningDefault")
  inline def run[I, T, A <: Action[I] & QAC[I, T]](inline quoted: Quoted[BatchAction[A]]): List[T] = InternalApi.runBatchActionReturning(quoted, 1)

  override def wrap[T](t: => T): T = t
  override def push[A, B](result: A)(f: A => B): B = f(result)
  override def seq[A](list: List[A]): List[A] = list

  protected def withConnection[T](f: Connection => Result[T]): Result[T] = {
    val conn = ds.getConnection
    try f(conn)
    finally conn.close()
  }
}
