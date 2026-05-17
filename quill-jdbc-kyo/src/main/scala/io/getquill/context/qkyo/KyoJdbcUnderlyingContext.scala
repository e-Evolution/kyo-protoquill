package io.getquill.context.qkyo

import io.getquill.*
import io.getquill.context.*
import io.getquill.context.KyoJdbc.*
import io.getquill.context.jdbc.{JdbcContextTypes, JdbcContextVerbExecute}
import io.getquill.context.sql.idiom.SqlIdiom
import io.getquill.util.ContextLogger
import kyo.*

import java.sql.{Array as _, *}
import javax.sql.DataSource
import scala.annotation.targetName
import scala.reflect.ClassTag

/**
 * Kyo equivalent of ZioJdbcUnderlyingContext.
 *
 * This is the Connection-level effectful context. All operations return
 * QCIO[T] = T < (Abort[SQLException] & Env[Connection] & IO & Async),
 * meaning they require a Connection to be provided via Env.
 *
 * The connection is obtained from the environment (Env[Connection]) rather
 * than being managed directly. Connection lifecycle is handled by the
 * outer KyoJdbcContext which provides the connection via Env.run.
 */
abstract class KyoJdbcUnderlyingContext[+Dialect <: SqlIdiom, +Naming <: NamingStrategy] extends KyoContext[Dialect, Naming]
  with JdbcContextVerbExecute[Dialect, Naming]
  with ContextVerbStream[Dialect, Naming]
  with KyoPrepareContext[Dialect, Naming]
  with KyoTranslateContext[Dialect, Naming] {

  override private[getquill] val logger = ContextLogger(classOf[KyoJdbcUnderlyingContext[_, _]])

  override type Error = SQLException
  override type Environment = Connection
  override type PrepareRow = PreparedStatement
  override type ResultRow = ResultSet
  override type RunActionResult = Long
  override type RunActionReturningResult[T] = T
  override type RunBatchActionResult = List[Long]
  override type RunBatchActionReturningResult[T] = List[T]
  override type Runner = Unit
  override type TranslateRunner = Unit
  override protected def context: Runner = ()
  def translateContext: TranslateRunner = ()

  @targetName("runQueryDefault")
  inline def run[T](inline quoted: Quoted[Query[T]]): QCIO[List[T]] = InternalApi.runQueryDefault(quoted)
  @targetName("runQuery")
  inline def run[T](inline quoted: Quoted[Query[T]], inline wrap: OuterSelectWrap): QCIO[List[T]] = InternalApi.runQuery(quoted, wrap)
  @targetName("runQuerySingle")
  inline def run[T](inline quoted: Quoted[T]): QCIO[T] = InternalApi.runQuerySingle(quoted)
  @targetName("runAction")
  inline def run[E](inline quoted: Quoted[Action[E]]): QCIO[Long] = InternalApi.runAction(quoted)
  @targetName("runActionReturning")
  inline def run[E, T](inline quoted: Quoted[ActionReturning[E, T]]): QCIO[T] = InternalApi.runActionReturning[E, T](quoted)
  @targetName("runActionReturningMany")
  inline def run[E, T](inline quoted: Quoted[ActionReturning[E, List[T]]]): QCIO[List[T]] = InternalApi.runActionReturningMany[E, T](quoted)
  @targetName("runBatchAction")
  inline def run[I, A <: Action[I] & QAC[I, Nothing]](inline quoted: Quoted[BatchAction[A]], rowsPerBatch: Int): QCIO[List[Long]] = InternalApi.runBatchAction(quoted, rowsPerBatch)
  @targetName("runBatchActionDefault")
  inline def run[I, A <: Action[I] & QAC[I, Nothing]](inline quoted: Quoted[BatchAction[A]]): QCIO[List[Long]] = InternalApi.runBatchAction(quoted, 1)
  @targetName("runBatchActionReturning")
  inline def run[I, T, A <: Action[I] & QAC[I, T]](inline quoted: Quoted[BatchAction[A]], rowsPerBatch: Int): QCIO[List[T]] = InternalApi.runBatchActionReturning(quoted, rowsPerBatch)
  @targetName("runBatchActionReturningDefault")
  inline def run[I, T, A <: Action[I] & QAC[I, T]](inline quoted: Quoted[BatchAction[A]]): QCIO[List[T]] = InternalApi.runBatchActionReturning(quoted, 1)

  private def annotate[A, S](kyo: A < S, sql: => String, info: => ExecutionInfo): A < S =
    KyoQuillLog.withExecutionInfo(info)(KyoQuillLog.withSqlQuery(sql)(kyo))

  // Override execute methods to add annotation (logging)
  override def executeAction(sql: String, prepare: Prepare = identityPrepare)(info: ExecutionInfo, dc: Runner): QCIO[Long] =
    annotate(super.executeAction(sql, prepare)(info, dc), sql, info)
  override def executeQuery[T](sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner): QCIO[List[T]] =
    annotate(super.executeQuery(sql, prepare, extractor)(info, dc), sql, info)
  override def executeQuerySingle[T](sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner): QCIO[T] =
    annotate(super.executeQuerySingle(sql, prepare, extractor)(info, dc), sql, info)
  override def executeActionReturning[O](sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[O], returningBehavior: ReturnAction)(info: ExecutionInfo, dc: Runner): QCIO[O] =
    annotate(super.executeActionReturning(sql, prepare, extractor, returningBehavior)(info, dc), sql, info)
  override def executeActionReturningMany[O](sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[O], returningBehavior: ReturnAction)(info: ExecutionInfo, dc: Runner): QCIO[List[O]] =
    annotate(super.executeActionReturningMany(sql, prepare, extractor, returningBehavior)(info, dc), sql, info)
  override def executeBatchAction(groups: List[BatchGroup])(info: ExecutionInfo, dc: Runner): QCIO[List[Long]] =
    annotate(super.executeBatchAction(groups)(info, dc), concatQueries(groups), info)
  override def executeBatchActionReturning[T](groups: List[BatchGroupReturning], extractor: Extractor[T])(info: ExecutionInfo, dc: Runner): QCIO[List[T]] =
    annotate(super.executeBatchActionReturning(groups, extractor)(info, dc), concatQueriesRet(groups), info)
  override def prepareQuery(sql: String, prepare: Prepare)(info: ExecutionInfo, dc: Runner): QCIO[PreparedStatement] =
    annotate(super.prepareQuery(sql, prepare)(info, dc), sql, info)
  override def prepareAction(sql: String, prepare: Prepare)(info: ExecutionInfo, dc: Runner): QCIO[PreparedStatement] =
    annotate(super.prepareAction(sql, prepare)(info, dc), sql, info)
  override def prepareBatchAction(groups: List[BatchGroup])(info: ExecutionInfo, dc: Runner): QCIO[List[PreparedStatement]] =
    annotate(super.prepareBatchAction(groups)(info, dc), concatQueries(groups), info)
  override def translateQueryEndpoint[T](statement: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor, prettyPrint: Boolean = false)(info: ExecutionInfo, dc: Runner): QCIO[String] =
    annotate(super.translateQueryEndpoint(statement, prepare, extractor, prettyPrint)(info, dc), statement, info)
  override def translateBatchQueryEndpoint(groups: List[BatchGroup], prettyPrint: Boolean = false)(info: ExecutionInfo, dc: Runner): QCIO[List[String]] =
    annotate(super.translateBatchQueryEndpoint(groups, prettyPrint)(info, dc), concatQueries(groups), info)

  protected def concatQueries(groups: List[BatchGroup]): String = groups.map(_.string).distinct.mkString(",")
  protected def concatQueriesRet(groups: List[BatchGroupReturning]): String = groups.map(_.string).distinct.mkString(",")

  /** Kyo contexts do not manage DB connections so this is a no-op */
  override def close(): Unit = ()

  protected def withConnection[T](f: Connection => Result[T]): Result[T] = throw new IllegalArgumentException("Not Used")

  // Primary method used to actually run Quill context commands
  override protected def withConnectionWrapped[T](f: Connection => T): QCIO[T] =
    for {
      conn <- Env.get[Connection]
      result <- Abort.catching[SQLException](kyo.IO.defer(f(conn)))
    } yield result

  /** Transaction at Connection level: disable autoCommit, execute, commit/rollback */
  def transaction[A](f: A < (Abort[Throwable] & Env[Connection] & IO & Async)): A < (Abort[Throwable] & Env[Connection] & IO & Async) = {
    for {
      conn <- Env.get[Connection]
      prevAutoCommit <- kyo.IO.defer(conn.getAutoCommit)
      _ <- kyo.IO.defer(conn.setAutoCommit(false))
      result <- Abort.run[Throwable](f)
      _ <- result match {
        case kyo.Result.Success(_) =>
          kyo.IO.defer {
            conn.commit()
            conn.setAutoCommit(prevAutoCommit)
          }
        case _ =>
          kyo.IO.defer {
            conn.rollback()
            conn.setAutoCommit(prevAutoCommit)
          }
      }
      value <- Abort.get(result)
    } yield value
  }

  def probingDataSource: Option[DataSource] = None

  /**
   * Override to enable specific vendor options needed for streaming
   */
  protected def prepareStatementForStreaming(sql: String, conn: Connection, fetchSize: Option[Int]) = {
    val stmt = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
    fetchSize.foreach { size =>
      stmt.setFetchSize(size)
    }
    stmt
  }

  /**
   * Streaming query implementation using Kyo Stream.
   *
   * Kyo's Stream.init requires Tag[V] at compile time, but the parent trait
   * (ProtoStreamContext) does not provide it in streamQuery's signature.
   * We work around this by materializing results as Chunk[Any] (Tag[Any] is
   * always derivable) and casting the resulting Stream[Any, S] to Stream[T, S].
   * This is runtime-safe since the actual elements are of type T.
   *
   * The result is a fully functional Kyo Stream that can be composed with
   * map, filter, fold, run, etc.
   */
  def streamQuery[T](fetchSize: Option[Int], sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner): QCStream[T] = {
    val chunkEffect: Chunk[Any] < (Abort[SQLException] & Env[Connection] & kyo.IO & Async) =
      for {
        conn <- Env.get[Connection]
        ps <- Abort.catching[SQLException] {
          kyo.IO.defer(prepareStatementForStreaming(sql, conn, fetchSize))
        }
        prepResult <- Abort.catching[SQLException] {
          kyo.IO.defer {
            val (params, preparedPs) = prepare(ps, conn)
            logger.logQuery(sql, params)
            preparedPs
          }
        }
        rs <- Abort.catching[SQLException](kyo.IO.defer(prepResult.executeQuery()))
      } yield {
        val iter = new ResultSetIterator(rs, conn, extractor)
        Chunk.from(iter.toIndexedSeq).asInstanceOf[Chunk[Any]]
      }
    Stream.init[Any, Abort[SQLException] & Env[Connection] & kyo.IO & Async](chunkEffect)
      .asInstanceOf[QCStream[T]]
  }

  override private[getquill] def prepareParams(statement: String, prepare: Prepare): QCIO[Seq[String]] =
    withConnectionWrapped { conn =>
      prepare(conn.prepareStatement(statement), conn)._1.reverse.map(prepareParam)
    }

  // For JdbcContextVerbExecute
  override def wrap[T](t: => T): QCIO[T] = Abort.catching[SQLException](kyo.IO.defer(t))
  override def push[A, B](result: QCIO[A])(f: A => B): QCIO[B] = result.map(f)
  override def seq[A](f: List[QCIO[A]]): QCIO[List[A]] =
    f.foldRight(wrap(List.empty[A])) { (elem, acc) =>
      for {
        a  <- elem
        as <- acc
      } yield a :: as
    }
}
