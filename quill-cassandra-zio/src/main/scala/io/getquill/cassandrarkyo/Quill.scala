package io.getquill.cassandrarkyo

import com.datastax.oss.driver.api.core.cql.{BoundStatement, Row}
import io.getquill.context.cassandra.{CassandraRowContext, CassandraStandardContext, CqlIdiom}
import io.getquill.context.qzio.KyoContext
import io.getquill.context.{Context, ExecutionInfo}
import io.getquill.util.ContextLogger
import io.getquill._
import kyo.*
import kyo.stream.Stream

import scala.annotation.targetName

object Quill {

  type CassandraKyoSession = io.getquill.CassandraKyoSession
  val CassandraKyoSession = io.getquill.CassandraKyoSession

  object Cassandra {
    def fromNamingStrategy[N <: NamingStrategy: Tag](naming: N): Env.Layer[CassandraKyoSession, Cassandra[N]] =
      Env.Layer.from((session: CassandraKyoSession) => new Cassandra[N](naming, session))
  }

  case class Cassandra[+N <: NamingStrategy](val naming: N, session: CassandraKyoSession)
    extends CassandraStandardContext[N]
    with KyoContext[CqlIdiom, N]
    with Context[CqlIdiom, N] {

    private val logger = ContextLogger(classOf[Quill.Cassandra[_]])

    override type Error = Throwable
    override type Environment = Any

    override type StreamResult[T] = Stream[T, (Abort[Throwable] & Sync & Async)]
    override type RunActionResult = Unit
    override type Result[T] = T < (Abort[Throwable] & Sync & Async)

    override type RunQueryResult[T] = List[T]
    override type RunQuerySingleResult[T] = T
    override type RunBatchActionResult = Unit

    override type PrepareRow = BoundStatement
    override type ResultRow = Row
    override type Session = CassandraKyoSession

    override type Runner = Unit
    override protected def context: Runner = ()

    val underlying: CassandraKyoContext[N] = new CassandraKyoContext[N](naming)

    @targetName("runQueryDefault")
    inline def run[T](inline quoted: Quoted[Query[T]]): Result[List[T]] = InternalApi.runQueryDefault(quoted)
    @targetName("runQuery")
    inline def run[T](inline quoted: Quoted[Query[T]], inline wrap: OuterSelectWrap): Result[List[T]] = InternalApi.runQuery(quoted, wrap)
    @targetName("runQuerySingle")
    inline def run[T](inline quoted: Quoted[T]): Result[T] = InternalApi.runQuerySingle(quoted)
    @targetName("runAction")
    inline def run[E](inline quoted: Quoted[Action[E]]): Result[Unit] = InternalApi.runAction(quoted)
    @targetName("runBatchAction")
    inline def run[I, A <: Action[I] & QAC[I, Nothing]](inline quoted: Quoted[BatchAction[A]]): Result[Unit] = InternalApi.runBatchAction(quoted, 1)

    def streamQuery[T](fetchSize: Option[Int], cql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner): StreamResult[T] =
      onSessionStream(underlying.streamQuery(fetchSize, cql, prepare, extractor)(info, dc))
    def executeQuery[T](cql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner): Result[List[T]] =
      onSession(underlying.executeQuery(cql, prepare, extractor)(info, dc))

    def executeQuerySingle[T](cql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner): Result[T] =
      onSession(underlying.executeQuerySingle(cql, prepare, extractor)(info, dc))

    def executeAction(cql: String, prepare: Prepare = identityPrepare)(info: ExecutionInfo, dc: Runner): Result[Unit] =
      onSession(underlying.executeAction(cql, prepare)(info, dc))

    def executeBatchAction(groups: List[BatchGroup])(info: ExecutionInfo, dc: Runner): Result[Unit] =
      onSession(underlying.executeBatchAction(groups.asInstanceOf[List[this.underlying.BatchGroup]])(info, dc))

    private def onSession[T](kyo: T < (Abort[Throwable] & Env[CassandraKyoSession] & Sync & Async)): Result[T] =
      Env.run(session)(kyo)

    private def onSessionStream[T](stream: Stream[T, (Abort[Throwable] & Env[CassandraKyoSession] & Sync & Async)]): StreamResult[T] =
      Stream.dropEnv[CassandraKyoSession](stream)

    override def close() = session.close()
  }
}
