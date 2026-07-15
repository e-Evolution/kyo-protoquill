package io.getquill.jdbckyo

import io.getquill.*
import io.getquill.context.*
import io.getquill.context.KyoJdbc.*
import io.getquill.context.jdbc.JdbcContextTypes
import io.getquill.context.qkyo.{KyoContext, KyoJdbcContext, KyoTranslateContext}
import io.getquill.context.sql.idiom.SqlIdiom
import kyo.*

import io.getquill.context.jdbc.{H2JdbcTypes, MysqlJdbcTypes, OracleJdbcTypes, PostgresJdbcTypes, SqlServerJdbcTypes, SqliteJdbcTypes}
import io.getquill.context.json.PostgresJsonExtensions
import io.getquill.util.LoadConfig
import com.typesafe.config.Config
import java.io.Closeable
import java.sql.{Connection, PreparedStatement, ResultSet, SQLException}
import javax.sql.DataSource
import scala.annotation.targetName

/**
 * Kyo equivalent of jdbczio.QuillBaseContext.
 *
 * This is the convenience layer where the DataSource is baked in (not an effect dependency).
 * All operations return results without Env[DataSource] since the DataSource is already provided.
 *
 * Usage:
 * {{{
 *   val ctx = new EffectfulQuill.Postgres(Literal, myDataSource)
 *   val result: List[Person] < (Abort[SQLException] & Sync & Async) = ctx.run(query[Person])
 * }}}
 */
trait EffectfulQuill[+Dialect <: SqlIdiom, +Naming <: NamingStrategy]
  extends KyoContext[Dialect, Naming]
  with JdbcContextTypes[Dialect, Naming]
  with ProtoContextSecundus[Dialect, Naming]
  with ContextVerbStream[Dialect, Naming]
  with KyoTranslateContext[Dialect, Naming] {

  def ds: DataSource

  // Environment = Any because DataSource is already provided (baked in)
  // Result[T] resolves to T < (Abort[SQLException] & Env[Any] & Sync & Async) via KyoContext
  // Env[Any] is trivially satisfiable and adds no practical overhead
  override type Error = SQLException
  override type Environment = Any

  override type RunQueryResult[T] = List[T]
  override type RunQuerySingleResult[T] = T
  override type RunActionResult = Long
  override type RunActionReturningResult[T] = T
  override type RunBatchActionResult = List[Long]
  override type RunBatchActionReturningResult[T] = List[T]

  // Needed for TranslateContext in Kyo Quill
  override type Runner = Unit
  override type TranslateRunner = Unit
  override protected def context: Runner = ()
  def translateContext: TranslateRunner = ()

  override type PrepareRow = PreparedStatement
  override type ResultRow = ResultSet

  override type Session = Connection

  final lazy val underlying: KyoJdbcContext[Dialect, Naming] = dsDelegate
  private[getquill] val dsDelegate: KyoJdbcContext[Dialect, Naming]

  override def close() = ()

  @targetName("runQueryDefault")
  inline def run[T](inline quoted: Quoted[Query[T]]): Result[List[T]] = InternalApi.runQueryDefault(quoted)
  @targetName("runQuery")
  inline def run[T](inline quoted: Quoted[Query[T]], inline wrap: OuterSelectWrap): Result[List[T]] = InternalApi.runQuery(quoted, wrap)
  @targetName("runQuerySingle")
  inline def run[T](inline quoted: Quoted[T]): Result[T] = InternalApi.runQuerySingle(quoted)
  @targetName("runAction")
  inline def run[E](inline quoted: Quoted[Action[E]]): Result[Long] = InternalApi.runAction(quoted)
  @targetName("runActionReturning")
  inline def run[E, T](inline quoted: Quoted[ActionReturning[E, T]]): Result[T] = InternalApi.runActionReturning[E, T](quoted)
  @targetName("runActionReturningMany")
  inline def run[E, T](inline quoted: Quoted[ActionReturning[E, List[T]]]): Result[List[T]] = InternalApi.runActionReturningMany[E, T](quoted)
  @targetName("runBatchAction")
  inline def run[I, A <: Action[I] & QAC[I, Nothing]](inline quoted: Quoted[BatchAction[A]], rowsPerBatch: Int): Result[List[Long]] = InternalApi.runBatchAction(quoted, rowsPerBatch)
  @targetName("runBatchActionDefault")
  inline def run[I, A <: Action[I] & QAC[I, Nothing]](inline quoted: Quoted[BatchAction[A]]): Result[List[Long]] = InternalApi.runBatchAction(quoted, 1)
  @targetName("runBatchActionReturning")
  inline def run[I, T, A <: Action[I] & QAC[I, T]](inline quoted: Quoted[BatchAction[A]], rowsPerBatch: Int): Result[List[T]] = InternalApi.runBatchActionReturning(quoted, rowsPerBatch)
  @targetName("runBatchActionReturningDefault")
  inline def run[I, T, A <: Action[I] & QAC[I, T]](inline quoted: Quoted[BatchAction[A]]): Result[List[T]] = InternalApi.runBatchActionReturning(quoted, 1)

  // Delegate execute* calls to dsDelegate, providing DataSource
  def executeAction(sql: String, prepare: Prepare = identityPrepare)(info: ExecutionInfo, dc: Runner): Result[Long] =
    onDS(dsDelegate.executeAction(sql, prepare)(info, dc))

  def executeQuery[T](sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner): Result[List[T]] =
    onDS(dsDelegate.executeQuery[T](sql, prepare, extractor)(info, dc))

  override def executeQuerySingle[T](sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner): Result[T] =
    onDS(dsDelegate.executeQuerySingle[T](sql, prepare, extractor)(info, dc))

  override def translateQueryEndpoint[T](statement: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor, prettyPrint: Boolean = false)(executionInfo: ExecutionInfo, dc: Runner): TranslateResult[String] =
    onDS(dsDelegate.translateQueryEndpoint[T](statement, prepare, extractor, prettyPrint)(executionInfo, dc))

  override def translateBatchQueryEndpoint(groups: List[BatchGroup], prettyPrint: Boolean = false)(executionInfo: ExecutionInfo, dc: Runner): TranslateResult[List[String]] =
    onDS(dsDelegate.translateBatchQueryEndpoint(groups.asInstanceOf[List[EffectfulQuill.this.dsDelegate.BatchGroup]], prettyPrint)(executionInfo, dc))

  def streamQuery[T](fetchSize: Option[Int], sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner): StreamResult[T] = {
    // Execute query via onDS (provides DataSource) and wrap result in Stream
    val chunkEffect: Chunk[Any] < (Abort[SQLException] & Env[Any] & kyo.Sync & Async) =
      onDS(dsDelegate.executeQuery[T](sql, prepare, extractor)(info, dc))
        .map(list => Chunk.from(list).asInstanceOf[Chunk[Any]])
    Stream.init[Any, Abort[SQLException] & Env[Any] & kyo.Sync & Async](chunkEffect)
      .asInstanceOf[StreamResult[T]]
  }

  def executeActionReturning[O](sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[O], returningBehavior: ReturnAction)(info: ExecutionInfo, dc: Runner): Result[O] =
    onDS(dsDelegate.executeActionReturning[O](sql, prepare, extractor, returningBehavior)(info, dc))

  def executeActionReturningMany[O](sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[O], returningBehavior: ReturnAction)(info: ExecutionInfo, dc: Runner): Result[List[O]] =
    onDS(dsDelegate.executeActionReturningMany[O](sql, prepare, extractor, returningBehavior)(info, dc))

  def executeBatchAction(groups: List[BatchGroup])(info: ExecutionInfo, dc: Runner): Result[List[Long]] =
    onDS(dsDelegate.executeBatchAction(groups.asInstanceOf[List[EffectfulQuill.this.dsDelegate.BatchGroup]])(info, dc))

  def executeBatchActionReturning[T](groups: List[BatchGroupReturning], extractor: Extractor[T])(info: ExecutionInfo, dc: Runner): Result[List[T]] =
    onDS(dsDelegate.executeBatchActionReturning[T](groups.asInstanceOf[List[EffectfulQuill.this.dsDelegate.BatchGroupReturning]], extractor)(info, dc))

  // Used in translation functions
  private[getquill] def prepareParams(statement: String, prepare: Prepare): Result[Seq[String]] =
    onDS(dsDelegate.prepareParams(statement, prepare))

  /**
   * Execute instructions in a transaction.
   * Delegates to dsDelegate.transaction and provides the DataSource.
   */
  def transaction[A](op: A < (Abort[Throwable] & Sync & Async)): A < (Abort[Throwable] & Sync & Async) =
    Env.run(ds: DataSource)(
      dsDelegate.transaction(
        op.asInstanceOf[A < (Abort[Throwable] & Env[DataSource] & Sync & Async)]
      )
    )

  /** Provide DataSource to a QIO computation, removing Env[DataSource] from effects */
  private def onDS[T](qio: QIO[T]): Result[T] =
    Env.run(ds: DataSource)(qio)

  // For translate context
  override def wrap[T](t: => T): Result[T] = Abort.catching[SQLException](kyo.Sync.defer(t))
  override def push[A, B](result: Result[A])(f: A => B): Result[B] = result.map(f)
  override def seq[A](f: List[Result[A]]): Result[List[A]] =
    f.foldRight(wrap(List.empty[A])) { (elem, acc) =>
      for {
        a  <- elem
        as <- acc
      } yield a :: as
    }
}

object EffectfulQuill {

  class Postgres[+N <: NamingStrategy](val naming: N, val ds: DataSource)
    extends EffectfulQuill[PostgresDialect, N] with PostgresJdbcTypes[PostgresDialect, N] with PostgresJsonExtensions {
    val idiom: PostgresDialect = PostgresDialect
    val dsDelegate: KyoJdbcContext[PostgresDialect, N] = new PostgresKyoJdbcEffectContext[N](naming)
  }
  object Postgres {
    def apply[N <: NamingStrategy](naming: N, ds: DataSource) = new Postgres[N](naming, ds)
  }

  class SqlServer[+N <: NamingStrategy](val naming: N, val ds: DataSource)
    extends EffectfulQuill[SQLServerDialect, N] with SqlServerJdbcTypes[SQLServerDialect, N] {
    val idiom: SQLServerDialect = SQLServerDialect
    val dsDelegate: KyoJdbcContext[SQLServerDialect, N] = new SqlServerKyoJdbcEffectContext[N](naming)
  }
  object SqlServer {
    def apply[N <: NamingStrategy](naming: N, ds: DataSource) = new SqlServer[N](naming, ds)
  }

  class H2[+N <: NamingStrategy](val naming: N, val ds: DataSource)
    extends EffectfulQuill[H2Dialect, N] with H2JdbcTypes[H2Dialect, N] {
    val idiom: H2Dialect = H2Dialect
    val dsDelegate: KyoJdbcContext[H2Dialect, N] = new H2KyoJdbcEffectContext[N](naming)
  }
  object H2 {
    def apply[N <: NamingStrategy](naming: N, ds: DataSource) = new H2[N](naming, ds)
  }

  class Mysql[+N <: NamingStrategy](val naming: N, val ds: DataSource)
    extends EffectfulQuill[MySQLDialect, N] with MysqlJdbcTypes[MySQLDialect, N] {
    val idiom: MySQLDialect = MySQLDialect
    val dsDelegate: KyoJdbcContext[MySQLDialect, N] = new MysqlKyoJdbcEffectContext[N](naming)
  }
  object Mysql {
    def apply[N <: NamingStrategy](naming: N, ds: DataSource) = new Mysql[N](naming, ds)
  }

  class Sqlite[+N <: NamingStrategy](val naming: N, val ds: DataSource)
    extends EffectfulQuill[SqliteDialect, N] with SqliteJdbcTypes[SqliteDialect, N] {
    val idiom: SqliteDialect = SqliteDialect
    val dsDelegate: KyoJdbcContext[SqliteDialect, N] = new SqliteKyoJdbcEffectContext[N](naming)
  }
  object Sqlite {
    def apply[N <: NamingStrategy](naming: N, ds: DataSource) = new Sqlite[N](naming, ds)
  }

  class Oracle[+N <: NamingStrategy](val naming: N, val ds: DataSource)
    extends EffectfulQuill[OracleDialect, N] with OracleJdbcTypes[OracleDialect, N] {
    val idiom: OracleDialect = OracleDialect
    val dsDelegate: KyoJdbcContext[OracleDialect, N] = new OracleKyoJdbcEffectContext[N](naming)
  }
  object Oracle {
    def apply[N <: NamingStrategy](naming: N, ds: DataSource) = new Oracle[N](naming, ds)
  }

  /** Factory methods for creating DataSource instances */
  object DataSource {
    def fromDataSource(ds: => DataSource): DataSource < Sync =
      kyo.Sync.defer(ds)

    def fromConfig(config: => Config): DataSource < (Sync & Scope) =
      fromJdbcConfigClosable(JdbcContextConfig(config))

    def fromPrefix(prefix: String): DataSource < (Sync & Scope) =
      fromJdbcConfigClosable(JdbcContextConfig(LoadConfig(prefix)))

    def fromJdbcConfig(jdbcContextConfig: => JdbcContextConfig): DataSource < (Sync & Scope) =
      fromJdbcConfigClosable(jdbcContextConfig)

    def fromJdbcConfigClosable(jdbcContextConfig: => JdbcContextConfig): DataSource < (Sync & Scope) =
      for {
        conf <- kyo.Sync.defer(jdbcContextConfig)
        ds <- KyoJdbc.scopedBestEffort(kyo.Sync.defer(conf.dataSource))
      } yield ds
  }

  /** Factory methods for acquiring scoped connections from a DataSource */
  object Connection {
    def acquireScoped(ds: DataSource): Connection < (Sync & Scope & Abort[SQLException]) =
      KyoJdbc.scopedBestEffort(Abort.catching[SQLException](kyo.Sync.defer(ds.getConnection)))
  }
}
