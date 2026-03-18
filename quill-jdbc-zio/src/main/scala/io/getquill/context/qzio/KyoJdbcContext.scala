package io.getquill.context.qzio

import io.getquill.context.KyoJdbc.*
import io.getquill.context.jdbc.JdbcContextTypes
import io.getquill.context.sql.idiom.SqlIdiom
import io.getquill.context.{ContextVerbStream, ExecutionInfo, ProtoContextSecundus}
import kyo.*
import kyo.stream.Stream

import java.sql.{Array as _, *}
import javax.sql.DataSource
import scala.util.Try
import scala.annotation.targetName
import io.getquill.*
import io.getquill.jdbckyo.Quill

abstract class KyoJdbcContext[+Dialect <: SqlIdiom, +Naming <: NamingStrategy] extends KyoContext[Dialect, Naming]
  with JdbcContextTypes[Dialect, Naming]
  with ProtoContextSecundus[Dialect, Naming]
  with ContextVerbStream[Dialect, Naming]
  with KyoPrepareContext[Dialect, Naming]
  with KyoTranslateContext[Dialect, Naming] {

  override type StreamResult[T] = Stream[T, (Abort[Error] & Env[Environment] & Sync & Async)]
  override type Result[T] = T < (Abort[Error] & Env[Environment] & Sync & Async)
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
  override type Environment = DataSource
  override type PrepareRow = PreparedStatement
  override type ResultRow = ResultSet

  override type TranslateResult[T] = T < (Abort[Error] & Env[Environment] & Sync)
  override type PrepareQueryResult = QCIO[PrepareRow]
  override type PrepareActionResult = QCIO[PrepareRow]
  override type PrepareBatchActionResult = QCIO[List[PrepareRow]]
  override type Session = Connection

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

  val currentConnection: Local[Option[Connection]] = Local.init(None)

  final lazy val underlying: KyoJdbcUnderlyingContext[Dialect, Naming] = connDelegate
  private[getquill] val connDelegate: KyoJdbcUnderlyingContext[Dialect, Naming]

  override def close() = ()

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

  def streamQuery[T](fetchSize: Option[Int], sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner): QStream[T] =
    onConnectionStream(connDelegate.streamQuery[T](fetchSize, sql, prepare, extractor)(info, dc))

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

  def transaction[R <: DataSource, A](op: A < (Abort[Throwable] & Env[R] & S)): A < (Abort[Throwable] & Env[R] & S) = {
    for {
      connOpt <- currentConnection.get
      result <- connOpt match {
        case Some(connection) => op
        case None =>
          for {
            ds <- Env.get[DataSource]
            result <- Scope.run {
              for {
                conn <- Scope.acquireRelease(IO(ds.getConnection))(c => IO(c.close()))
                prevAutoCommit <- IO(conn.getAutoCommit)
                _ <- Scope.ensure(IO(conn.setAutoCommit(prevAutoCommit)))
                _ <- currentConnection.let(Some(conn)) {
                  Scope.acquireRelease(IO(conn.setAutoCommit(false)))(_ => 
                    IO(conn.commit()).catchAll(_ => IO(conn.rollback())).unit
                  )
                }
                result <- Env.run(conn)(op)
              } yield result
            }
          } yield result
      }
    } yield result
  }

  private def onConnection[T](qlio: QCIO[T]): QIO[T] =
    for {
      connOpt <- currentConnection.get
      result <- connOpt match {
        case Some(connection) => Env.run(connection)(qlio)
        case None => 
          for {
            ds <- Env.get[DataSource]
            result <- Scope.run {
              for {
                conn <- Scope.acquireRelease(IO(ds.getConnection))(c => IO(c.close()))
                result <- Env.run(conn)(qlio)
              } yield result
            }
          } yield result
      }
    } yield result

  private def onConnectionStream[T](qstream: Stream[T, (Abort[SQLException] & Env[Connection] & Sync & Async)]): QStream[T] =
    streamBlocker *> Stream.flatten {
      for {
        connOpt <- Stream.eval(currentConnection.get)
        result <- connOpt match {
          case Some(connection) => Stream.flatten(Stream.eval(Env.run(connection)(qstream.map(Stream.emit)))).mapError(e => e)
          case None => 
            for {
              ds <- Stream.eval(Env.get[DataSource])
              stream <- Stream.flatten {
                Stream.eval(Scope.run {
                  for {
                    conn <- Scope.acquireRelease(IO(ds.getConnection))(c => IO(c.close()))
                    result <- Env.run(conn)(qstream.map(v => Stream.emit(v)))
                  } yield result
                })
              }
            } yield stream
        }
      } yield result
    }
}
