package io.getquill.context.qkyo

import io.getquill.*
import io.getquill.context.*
import io.getquill.context.KyoJdbc.*
import io.getquill.context.jdbc.{JdbcContextTypes, JdbcContextVerbExecute}
import io.getquill.context.sql.idiom.SqlIdiom
import kyo.*

import java.sql.{Array as _, *}
import javax.sql.DataSource
import scala.annotation.targetName

/**
 * Kyo equivalent of ZioJdbcContext.
 *
 * This is the DataSource-level effectful context. All operations return
 * QIO[T] = T < (Abort[SQLException] & Env[DataSource] & Sync & Async),
 * meaning they require a DataSource to be provided via Env.
 *
 * It delegates to a KyoJdbcUnderlyingContext (Connection-level) via
 * onConnection(), which handles the Connection acquisition:
 * - If inside a transaction, reuses the connection from Local
 * - Otherwise, acquires a new connection from the DataSource
 *
 * Transaction support uses Local[Option[Connection]] (Kyo equivalent
 * of ZIO's FiberRef) for connection propagation across nested calls.
 */
abstract class KyoJdbcContext[+Dialect <: SqlIdiom, +Naming <: NamingStrategy] extends KyoContext[Dialect, Naming]
  with JdbcContextTypes[Dialect, Naming]
  with ProtoContextSecundus[Dialect, Naming]
  with ContextVerbStream[Dialect, Naming]
  with KyoPrepareContext[Dialect, Naming]
  with KyoTranslateContext[Dialect, Naming] {

  override type StreamResult[T] = QStream[T]
  override type Result[T] = QIO[T]
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

  override type Error = SQLException
  override type Environment = DataSource
  override type PrepareRow = PreparedStatement
  override type ResultRow = ResultSet

  override type TranslateResult[T] = QIO[T]
  override type PrepareQueryResult = QCIO[PrepareRow]
  override type PrepareActionResult = QCIO[PrepareRow]
  override type PrepareBatchActionResult = QCIO[List[PrepareRow]]
  override type Session = Connection

  /**
   * Local for transaction connection propagation.
   * Equivalent to ZIO's FiberRef[Option[Connection]].
   * When inside a transaction, this holds the transaction's connection
   * so all nested operations reuse the same connection.
   */
  val currentConnection: Local[Option[Connection]] = Local.init(None)

  final lazy val underlying: KyoJdbcUnderlyingContext[Dialect, Naming] = connDelegate
  private[getquill] val connDelegate: KyoJdbcUnderlyingContext[Dialect, Naming]

  override def close() = ()

  @targetName("runQueryDefault")
  inline def run[T](inline quoted: Quoted[Query[T]]): QIO[List[T]] = InternalApi.runQueryDefault(quoted)
  @targetName("runQuery")
  inline def run[T](inline quoted: Quoted[Query[T]], inline wrap: OuterSelectWrap): QIO[List[T]] = InternalApi.runQuery(quoted, wrap)
  @targetName("runQuerySingle")
  inline def run[T](inline quoted: Quoted[T]): QIO[T] = InternalApi.runQuerySingle(quoted)
  @targetName("runAction")
  inline def run[E](inline quoted: Quoted[Action[E]]): QIO[Long] = InternalApi.runAction(quoted)
  @targetName("runActionReturning")
  inline def run[E, T](inline quoted: Quoted[ActionReturning[E, T]]): QIO[T] = InternalApi.runActionReturning[E, T](quoted)
  @targetName("runActionReturningMany")
  inline def run[E, T](inline quoted: Quoted[ActionReturning[E, List[T]]]): QIO[List[T]] = InternalApi.runActionReturningMany[E, T](quoted)
  @targetName("runBatchAction")
  inline def run[I, A <: Action[I] & QAC[I, Nothing]](inline quoted: Quoted[BatchAction[A]], rowsPerBatch: Int): QIO[List[Long]] = InternalApi.runBatchAction(quoted, rowsPerBatch)
  @targetName("runBatchActionDefault")
  inline def run[I, A <: Action[I] & QAC[I, Nothing]](inline quoted: Quoted[BatchAction[A]]): QIO[List[Long]] = InternalApi.runBatchAction(quoted, 1)
  @targetName("runBatchActionReturning")
  inline def run[I, T, A <: Action[I] & QAC[I, T]](inline quoted: Quoted[BatchAction[A]], rowsPerBatch: Int): QIO[List[T]] = InternalApi.runBatchActionReturning(quoted, rowsPerBatch)
  @targetName("runBatchActionReturningDefault")
  inline def run[I, T, A <: Action[I] & QAC[I, T]](inline quoted: Quoted[BatchAction[A]]): QIO[List[T]] = InternalApi.runBatchActionReturning(quoted, 1)

  // Delegate execute* calls to connDelegate via onConnection
  def executeAction(sql: String, prepare: Prepare = identityPrepare)(info: ExecutionInfo, dc: Runner): QIO[Long] =
    onConnection(connDelegate.executeAction(sql, prepare)(info, dc))

  def executeQuery[T](sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner): QIO[List[T]] =
    onConnection(connDelegate.executeQuery[T](sql, prepare, extractor)(info, dc))

  override def executeQuerySingle[T](sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner): QIO[T] =
    onConnection(connDelegate.executeQuerySingle[T](sql, prepare, extractor)(info, dc))

  override def translateQueryEndpoint[T](statement: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor, prettyPrint: Boolean = false)(executionInfo: ExecutionInfo, dc: Runner): TranslateResult[String] =
    onConnection(connDelegate.translateQueryEndpoint[T](statement, prepare, extractor, prettyPrint)(executionInfo, dc))

  override def translateBatchQueryEndpoint(groups: List[BatchGroup], prettyPrint: Boolean = false)(executionInfo: ExecutionInfo, dc: Runner): TranslateResult[List[String]] =
    onConnection(connDelegate.translateBatchQueryEndpoint(groups.asInstanceOf[List[KyoJdbcContext.this.connDelegate.BatchGroup]], prettyPrint)(executionInfo, dc))

  def streamQuery[T](fetchSize: Option[Int], sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner): QStream[T] = {
    // Execute query via onConnection (handles Connection lifecycle) and wrap in Stream
    val chunkEffect: Chunk[Any] < (Abort[SQLException] & Env[DataSource] & kyo.Sync & Async) =
      onConnection(connDelegate.executeQuery[T](sql, prepare, extractor)(info, dc))
        .map(list => Chunk.from(list).asInstanceOf[Chunk[Any]])
    Stream.init[Any, Abort[SQLException] & Env[DataSource] & kyo.Sync & Async](chunkEffect)
      .asInstanceOf[QStream[T]]
  }

  def executeActionReturning[O](sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[O], returningBehavior: ReturnAction)(info: ExecutionInfo, dc: Runner): QIO[O] =
    onConnection(connDelegate.executeActionReturning[O](sql, prepare, extractor, returningBehavior)(info, dc))

  def executeActionReturningMany[O](sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[O], returningBehavior: ReturnAction)(info: ExecutionInfo, dc: Runner): QIO[List[O]] =
    onConnection(connDelegate.executeActionReturningMany[O](sql, prepare, extractor, returningBehavior)(info, dc))

  def executeBatchAction(groups: List[BatchGroup])(info: ExecutionInfo, dc: Runner): QIO[List[Long]] =
    onConnection(connDelegate.executeBatchAction(groups.asInstanceOf[List[KyoJdbcContext.this.connDelegate.BatchGroup]])(info, dc))

  def executeBatchActionReturning[T](groups: List[BatchGroupReturning], extractor: Extractor[T])(info: ExecutionInfo, dc: Runner): QIO[List[T]] =
    onConnection(connDelegate.executeBatchActionReturning[T](groups.asInstanceOf[List[KyoJdbcContext.this.connDelegate.BatchGroupReturning]], extractor)(info, dc))

  override def prepareQuery(sql: String, prepare: Prepare)(info: ExecutionInfo, dc: Runner): QCIO[PreparedStatement] =
    connDelegate.prepareQuery(sql, prepare)(info, dc)

  override def prepareAction(sql: String, prepare: Prepare)(info: ExecutionInfo, dc: Runner): QCIO[PreparedStatement] =
    connDelegate.prepareAction(sql, prepare)(info, dc)

  override def prepareBatchAction(groups: List[BatchGroup])(info: ExecutionInfo, dc: Runner): QCIO[List[PreparedStatement]] =
    connDelegate.prepareBatchAction(groups.asInstanceOf[List[KyoJdbcContext.this.connDelegate.BatchGroup]])(info, dc)

  private[getquill] def prepareParams(statement: String, prepare: Prepare): QIO[Seq[String]] =
    onConnection(connDelegate.prepareParams(statement, prepare))

  /**
   * Execute instructions in a transaction. For example:
   * {{{
   *   val a = ctx.run(query[Person].insert(Person(...))
   *   val b = ctx.run(query[Person])
   *   ctx.transaction(a *> b)
   * }}}
   *
   * Transaction connection propagation:
   * - If already inside a transaction (connection in Local), reuse it (nested transaction)
   * - Otherwise, acquire a new connection from DataSource, set autoCommit=false,
   *   put in Local, commit on success, rollback on failure
   */
  def transaction[A](op: A < (Abort[Throwable] & Env[DataSource] & Sync & Async)): A < (Abort[Throwable] & Env[DataSource] & Sync & Async) = {
    currentConnection.get.flatMap {
      // Nested transaction: reuse existing connection
      case Some(_) => op
      case None =>
        for {
          ds <- Env.get[DataSource]
          conn <- kyo.Sync.defer(ds.getConnection)
          prevAutoCommit <- kyo.Sync.defer(conn.getAutoCommit)
          _ <- kyo.Sync.defer(conn.setAutoCommit(false))
          result <- currentConnection.let(Some(conn)) {
            Abort.run[Throwable](op)
          }
          _ <- result match {
            case kyo.Result.Success(_) =>
              kyo.Sync.defer {
                conn.commit()
                conn.setAutoCommit(prevAutoCommit)
                conn.close()
              }
            case _ =>
              kyo.Sync.defer {
                conn.rollback()
                conn.setAutoCommit(prevAutoCommit)
                conn.close()
              }
          }
          value <- Abort.get(result)
        } yield value
    }
  }

  /**
   * Convert a Connection-level computation (QCIO) to a DataSource-level computation (QIO).
   *
   * If inside a transaction, uses the connection from Local.
   * Otherwise, acquires a new connection from the DataSource,
   * executes the computation, and closes the connection.
   */
  private[getquill] def onConnection[T](qcio: T < (Abort[SQLException] & Env[Connection] & Sync & Async)): QIO[T] =
    currentConnection.get.flatMap {
      case Some(conn) =>
        // Inside transaction: use the existing connection
        Env.run(conn)(qcio)
      case None =>
        // Outside transaction: acquire, use, release
        for {
          ds <- Env.get[DataSource]
          conn <- Abort.catching[SQLException](kyo.Sync.defer(ds.getConnection))
          result <- Abort.run[SQLException] {
            Env.run(conn)(qcio)
          }
          _ <- kyo.Sync.defer(conn.close())
          value <- Abort.get(result)
        } yield value
    }

  // For translate context
  override def wrap[T](t: => T): QIO[T] = Abort.catching[SQLException](kyo.Sync.defer(t))
  override def push[A, B](result: QIO[A])(f: A => B): QIO[B] = result.map(f)
  override def seq[A](f: List[QIO[A]]): QIO[List[A]] =
    f.foldRight(wrap(List.empty[A])) { (elem, acc) =>
      for {
        a  <- elem
        as <- acc
      } yield a :: as
    }
}
