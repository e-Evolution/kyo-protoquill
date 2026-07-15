package io.getquill

import com.datastax.oss.driver.api.core.cql.{AsyncResultSet, BoundStatement, Row}
import io.getquill.context.ExecutionInfo
import io.getquill.context.cassandra.{CassandraRowContext, CqlIdiom}
import io.getquill.util.Messages.fail
import io.getquill.util.ContextLogger
import kyo.{Chunk, Async, Env, Scope, Sync, <, Abort}
import kyo.Stream

import scala.jdk.CollectionConverters._
import scala.util.Try
import io.getquill.context.cassandra.CassandraStandardContext
import io.getquill.context.Context
import io.getquill.context.cassandra.CassandraPrepareContext
import io.getquill.context.AsyncFutureCache
import scala.annotation.targetName

class CassandraKyoContext[+N <: NamingStrategy](val naming: N)
  extends CassandraStandardContext[N]
  with Context[CqlIdiom, N] {

  private val logger = ContextLogger(classOf[CassandraKyoContext[_]])

  override type Result[T] = T
  override type RunQueryResult[T] = List[T]
  override type RunQuerySingleResult[T] = T
  override type RunActionResult = Unit
  override type RunBatchActionResult = Unit

  override type PrepareRow = BoundStatement
  override type ResultRow = Row
  override type Session = CassandraKyoSession

  override type Runner = Unit
  override protected def context: Runner = ()

  @targetName("runQueryDefault")
  inline def run[T](inline quoted: Quoted[Query[T]]): Result[RunQueryResult[T]] = InternalApi.runQueryDefault(quoted)
  @targetName("runQuery")
  inline def run[T](inline quoted: Quoted[Query[T]], inline wrap: OuterSelectWrap): Result[RunQueryResult[T]] = InternalApi.runQuery(quoted, wrap)
  @targetName("runQuerySingle")
  inline def run[T](inline quoted: Quoted[T]): Result[RunQuerySingleResult[T]] = InternalApi.runQuerySingle(quoted)
  @targetName("runAction")
  inline def run[E](inline quoted: Quoted[Action[E]]): Result[RunActionResult] = InternalApi.runAction(quoted)
  @targetName("runBatchAction")
  inline def run[I, A <: Action[I] & QAC[I, Nothing]](inline quoted: Quoted[BatchAction[A]]): Result[RunBatchActionResult] = InternalApi.runBatchAction(quoted, 1)

  def streamQuery[T](fetchSize: Option[Int], cql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner): Stream[T, Any] =
    Stream.empty

  def executeQuery[T](cql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner): Result[RunQueryResult[T]] =
    List.empty

  def executeQuerySingle[T](cql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner): Result[RunQuerySingleResult[T]] =
    throw new NotImplementedError("executeQuerySingle not implemented")

  def executeAction(cql: String, prepare: Prepare = identityPrepare)(info: ExecutionInfo, dc: Runner): Result[RunActionResult] =
    ()

  def executeBatchAction(groups: List[BatchGroup])(info: ExecutionInfo, dc: Runner): Result[RunBatchActionResult] =
    ()

  private[getquill] def prepareRowAndLog(cql: String, prepare: Prepare = identityPrepare): PrepareRow =
    throw new NotImplementedError("prepareRowAndLog not implemented")

  def probingSession: Option[CassandraKyoSession] = None

  def probe(statement: String): scala.util.Try[_] = {
    probingSession match {
      case Some(csession) =>
        Try(csession.prepare(statement))
      case None =>
        Try(())
    }
  }

  override def close(): Unit = fail("Kyo Cassandra Session does not need to be closed because it does not keep internal state.")
}
