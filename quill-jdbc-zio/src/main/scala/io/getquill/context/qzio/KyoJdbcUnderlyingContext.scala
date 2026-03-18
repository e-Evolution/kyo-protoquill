package io.getquill.context.qzio

import io.getquill.context.KyoJdbc.*
import io.getquill.context.jdbc.JdbcContextVerbExecute
import io.getquill.context.sql.idiom.SqlIdiom
import io.getquill.context.{ContextVerbStream, ExecutionInfo, KyoQuillLog}
import io.getquill.util.ContextLogger
import io.getquill.*
import kyo.*
import kyo.stream.Stream

import java.sql.{Array as _, *}
import javax.sql.DataSource
import scala.reflect.ClassTag
import scala.util.Try
import scala.annotation.targetName

abstract class KyoJdbcUnderlyingContext[+Dialect <: SqlIdiom, +Naming <: NamingStrategy] extends KyoContext[Dialect, Naming]
  with JdbcContextVerbExecute[Dialect, Naming]
  with ContextVerbStream[Dialect, Naming]
  with KyoPrepareContext[Dialect, Naming]
  with KyoTranslateContext[Dialect, Naming] {

  override private[getquill] val logger = ContextLogger(classOf[KyoJdbcUnderlyingContext[_, _]])

  override type Error = SQLException
  override type Environment = Session
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

  protected def annotate[R, E, A](kyo: A < (Abort[E] & Env[R] & S), sql: => String, info: => ExecutionInfo): A < (Abort[E] & Env[R] & S) =
    kyo

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

  override def close(): Unit = ()

  protected def withConnection[T](f: Connection => Result[T]): Result[T] = throw new IllegalArgumentException("Not Used")

  override protected def withConnectionWrapped[T](f: Connection => T): QCIO[T] =
    for {
      conn <- Env.get[Connection]
      result <- Sync.defer(f(conn)).mapError {
        case sq: SQLException => sq
        case other => throw other
      }
    } yield result

  private def sqlEffect[T](t: => T): QCIO[T] = Sync.defer(t).mapError {
    case sq: SQLException => sq
    case other => throw other
  }

  private[getquill] def withoutAutoCommit[R <: Connection, A, E <: Throwable: ClassTag](f: A < (Abort[E] & Env[R] & S)): A < (Abort[E] & Env[R] & Scope & S) = ???

  private[getquill] def streamWithoutAutoCommit[A](f: Stream[A, (Abort[Throwable] & Env[Connection] & Sync & Async)]): Stream[A, (Abort[Throwable] & Env[Connection] & Scope & Sync & Async)] = ???

  def transaction[R <: Connection, A](f: A < (Abort[Throwable] & Env[R] & S)): A < (Abort[Throwable] & Env[R] & S) = ???

  def probingDataSource: Option[DataSource] = None

  protected def prepareStatementForStreaming(sql: String, conn: Connection, fetchSize: Option[Int]) = {
    val stmt = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
    fetchSize.foreach { size => stmt.setFetchSize(size) }
    stmt
  }

  def streamQuery[T](fetchSize: Option[Int], sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner): QCStream[T] = {
    def prepareStatement(conn: Connection) = {
      val stmt = prepareStatementForStreaming(sql, conn, fetchSize)
      val (params, ps) = prepare(stmt, conn)
      logger.logQuery(sql, params)
      ps
    }

    val scopedEnv: Stream[(Connection, PrepareRow, ResultSet), (Abort[SQLException] & Env[Connection] & Scope & Sync & Async)] =
      Stream.flatten {
        Stream.eval {
          for {
            conn <- Env.get[Connection]
            ps <- Scope.acquireRelease(sqlEffect(prepareStatement(conn)))(ps => Sync.defer(ps.close()).unit)
            rs <- Scope.acquireRelease(sqlEffect(ps.executeQuery()))(r => Sync.defer(r.close()).unit)
          } yield Stream.emit((conn, ps, rs))
        }
      }

    val outStream: Stream[T, (Abort[SQLException] & Env[Connection] & Scope & Sync & Async)] =
      scopedEnv.flatMap {
        case (conn, ps, rs) =>
          val iter = new ResultSetIterator(rs, conn, extractor)
          fetchSize match {
            case Some(size) => Stream.fromIterator(iter, size)
            case None => Stream.fromIterator(new ResultSetIterator(rs, conn, extractor))
          }
      }

    streamBlocker *> outStream.mapError {
      case sq: SQLException => sq
      case other => throw other
    }
  }

  override private[getquill] def prepareParams(statement: String, prepare: Prepare): QCIO[Seq[String]] = {
    withConnectionWrapped { conn =>
      prepare(conn.prepareStatement(statement), conn)._1.reverse.map(prepareParam)
    }
  }

  override def wrap[T](t: => T): QCIO[T] = QCIO(t)
  override def push[A, B](result: QCIO[A])(f: A => B): QCIO[B] = result.map(f)
  override def seq[A](f: List[QCIO[A]]): QCIO[List[A]] = Async.collectAll(f).map(_.toList)
}
