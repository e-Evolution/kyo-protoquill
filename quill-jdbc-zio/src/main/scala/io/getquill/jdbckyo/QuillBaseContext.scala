package io.getquill.jdbckyo

import io.getquill._
import io.getquill.context.{ContextVerbStream, ExecutionInfo, ProtoContextSecundus}
import io.getquill.context.jdbc.JdbcContextTypes
import io.getquill.context.qzio.{KyoContext, KyoJdbcContext, KyoTranslateContext}
import io.getquill.context.sql.idiom.SqlIdiom
import kyo.*
import kyo.stream.Stream

import java.sql.{Connection, PreparedStatement, ResultSet, SQLException}
import javax.sql.DataSource
import scala.util.Try
import scala.annotation.targetName

trait QuillBaseContext[+Dialect <: SqlIdiom, +Naming <: NamingStrategy] extends KyoContext[Dialect, Naming]
  with JdbcContextTypes[Dialect, Naming]
  with ProtoContextSecundus[Dialect, Naming]
  with ContextVerbStream[Dialect, Naming]
  with KyoTranslateContext[Dialect, Naming] {

  def ds: DataSource

  override type StreamResult[T] = Stream[T, (Abort[Error] & Env[Any] & Sync & Async)]
  override type Result[T] = T < (Abort[Error] & Env[Any] & Sync & Async)
  override type RunQueryResult[T] = List[T]
  override type RunQuerySingleResult[T] = T
  override type RunActionResult = Long
  override type RunActionReturningResult[T] = T
  override type RunBatchActionResult = List[Long]
  override type RunBatchActionReturningResult[T] = List[T]

  override type Runner = Unit
  override type TranslateRunner = Unit
  override protected def context: Runner = ()
  def translateContext: TranslateRunner = ()

  override type Error = SQLException
  override type Environment = Any
  override type PrepareRow = PreparedStatement
  override type ResultRow = ResultSet

  override type TranslateResult[T] = T < (Abort[Error] & Env[Any] & Sync)
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

  def executeAction(sql: String, prepare: Prepare = identityPrepare)(info: ExecutionInfo, dc: Runner): Result[Long] =
    onDS(dsDelegate.executeAction(sql, prepare)(info, dc))

  def executeQuery[T](sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner): Result[List[T]] =
    onDS(dsDelegate.executeQuery[T](sql, prepare, extractor)(info, dc))

  override def executeQuerySingle[T](sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner): Result[T] =
    onDS(dsDelegate.executeQuerySingle[T](sql, prepare, extractor)(info, dc))

  override def translateQueryEndpoint[T](statement: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor, prettyPrint: Boolean = false)(executionInfo: ExecutionInfo, dc: Runner): TranslateResult[String] =
    onDS(dsDelegate.translateQueryEndpoint[T](statement, prepare, extractor, prettyPrint)(executionInfo, dc))

  override def translateBatchQueryEndpoint(groups: List[BatchGroup], prettyPrint: Boolean = false)(executionInfo: ExecutionInfo, dc: Runner): TranslateResult[List[String]] =
    onDS(dsDelegate.translateBatchQueryEndpoint(groups.asInstanceOf[List[QuillBaseContext.this.dsDelegate.BatchGroup]], prettyPrint)(executionInfo, dc))

  def streamQuery[T](fetchSize: Option[Int], sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner): StreamResult[T] =
    onDSStream(dsDelegate.streamQuery[T](fetchSize, sql, prepare, extractor)(info, dc))

  def executeActionReturning[O](sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[O], returningBehavior: ReturnAction)(info: ExecutionInfo, dc: Runner): Result[O] =
    onDS(dsDelegate.executeActionReturning[O](sql, prepare, extractor, returningBehavior)(info, dc))

  def executeActionReturningMany[O](sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[O], returningBehavior: ReturnAction)(info: ExecutionInfo, dc: Runner): Result[List[O]] =
    onDS(dsDelegate.executeActionReturningMany[O](sql, prepare, extractor, returningBehavior)(info, dc))

  def executeBatchAction(groups: List[BatchGroup])(info: ExecutionInfo, dc: Runner): Result[List[Long]] =
    onDS(dsDelegate.executeBatchAction(groups.asInstanceOf[List[QuillBaseContext.this.dsDelegate.BatchGroup]])(info, dc))

  def executeBatchActionReturning[T](groups: List[BatchGroupReturning], extractor: Extractor[T])(info: ExecutionInfo, dc: Runner): Result[List[T]] =
    onDS(dsDelegate.executeBatchActionReturning[T](groups.asInstanceOf[List[QuillBaseContext.this.dsDelegate.BatchGroupReturning]], extractor)(info, dc))

  private[getquill] def prepareParams(statement: String, prepare: Prepare): Result[Seq[String]] =
    onDS(dsDelegate.prepareParams(statement, prepare))

  def transaction[R, A](op: A < (Abort[Throwable] & Env[R] & S)): A < (Abort[Throwable] & Env[R] & S) =
    dsDelegate.transaction(op).map(v => v)

  private def onDS[T](qio: T < (Abort[SQLException] & Env[DataSource] & S)): Result[T] =
    Env.run(ds)(qio)

  private def onDSStream[T](qstream: Stream[T, (Abort[SQLException] & Env[DataSource] & S)]): StreamResult[T] =
    Stream.dropEnv[DataSource](qstream)
}
