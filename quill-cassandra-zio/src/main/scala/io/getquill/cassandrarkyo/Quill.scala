package io.getquill.cassandrarkyo

import com.datastax.oss.driver.api.core.cql.{BoundStatement, Row}
import io.getquill.context.cassandra.{CassandraRowContext, CassandraStandardContext, CqlIdiom}
import io.getquill.context.{Context, ExecutionInfo}
import io.getquill.util.ContextLogger
import io.getquill._
import kyo.*
import kyo.Stream

import scala.annotation.targetName

object Quill {

  type CassandraKyoSession = io.getquill.CassandraKyoSession
  val CassandraKyoSession = io.getquill.CassandraKyoSession

  case class Cassandra[+N <: NamingStrategy](val naming: N, session: CassandraKyoSession)
    extends CassandraStandardContext[N]
    with Context[CqlIdiom, N] {

    private val logger = ContextLogger(classOf[Quill.Cassandra[_]])

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

    override def close() = session.close()
  }
}
