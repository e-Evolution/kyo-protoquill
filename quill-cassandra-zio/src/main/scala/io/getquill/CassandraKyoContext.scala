package io.getquill

import com.datastax.oss.driver.api.core.cql.{AsyncResultSet, BoundStatement, Row}
import io.getquill.CassandraKyoContext._
import io.getquill.context.ExecutionInfo
import io.getquill.context.cassandra.{CassandraRowContext, CqlIdiom}
import io.getquill.context.qzio.KyoContext
import io.getquill.util.Messages.fail
import io.getquill.util.ContextLogger
import kyo.*
import kyo.stream.Stream

import scala.jdk.CollectionConverters._
import scala.util.Try
import io.getquill.context.cassandra.CassandraStandardContext
import io.getquill.context.Context
import io.getquill.context.cassandra.CassandraPrepareContext
import io.getquill.context.AsyncFutureCache
import scala.annotation.targetName

object CassandraKyoContext {
  type CIO[T] = T < (Abort[Throwable] & Env[CassandraKyoSession] & Sync & Async)
  type CStream[T] = Stream[T, (Abort[Throwable] & Env[CassandraKyoSession] & Sync & Async)]
}

class CassandraKyoContext[+N <: NamingStrategy](val naming: N)
  extends CassandraStandardContext[N]
  with KyoContext[CqlIdiom, N]
  with Context[CqlIdiom, N] {

  private val logger = ContextLogger(classOf[CassandraKyoContext[_]])

  override type Error = Throwable
  override type Environment = CassandraKyoSession

  override type StreamResult[T] = CStream[T]
  override type RunActionResult = Unit
  override type Result[T] = CIO[T]

  override type RunQueryResult[T] = List[T]
  override type RunQuerySingleResult[T] = T
  override type RunBatchActionResult = Unit

  override type PrepareRow = BoundStatement
  override type ResultRow = Row
  override type Session = CassandraKyoSession

  override type Runner = Unit
  override protected def context: Runner = ()

  @targetName("runQueryDefault")
  inline def run[T](inline quoted: Quoted[Query[T]]): CIO[List[T]] = InternalApi.runQueryDefault(quoted)
  @targetName("runQuery")
  inline def run[T](inline quoted: Quoted[Query[T]], inline wrap: OuterSelectWrap): CIO[List[T]] = InternalApi.runQuery(quoted, wrap)
  @targetName("runQuerySingle")
  inline def run[T](inline quoted: Quoted[T]): CIO[T] = InternalApi.runQuerySingle(quoted)
  @targetName("runAction")
  inline def run[E](inline quoted: Quoted[Action[E]]): CIO[Unit] = InternalApi.runAction(quoted)
  @targetName("runBatchAction")
  inline def run[I, A <: Action[I] & QAC[I, Nothing]](inline quoted: Quoted[BatchAction[A]]): CIO[Unit] = InternalApi.runBatchAction(quoted, 1)

  protected def page(rs: AsyncResultSet): CIO[Chunk[Row]] = Chunk.fromIterator(rs.currentPage().asScala).pure

  private[getquill] def execute(cql: String, prepare: Prepare, csession: CassandraKyoSession, fetchSize: Option[Int]) =
    simpleBlocking {
      prepareRowAndLog(cql, prepare)
        .map { p =>
          fetchSize match {
            case Some(value) => p.setPageSize(value)
            case None        => p
          }
        }
        .flatMap(p => {
          for {
            resultSet <- IO.fromFuture(IO(csession.session.executeAsync(p)))
          } yield resultSet
        })
    }

  val streamBlocker: Stream[Any, Any] = Stream.empty

  def streamQuery[T](fetchSize: Option[Int], cql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner) = {
    val stream =
      for {
        csession <- Stream.eval(Env.get[CassandraKyoSession])
        rs <- Stream.eval(execute(cql, prepare, csession, fetchSize))
        chunk <- Stream.unfold(rs) { rs =>
          for {
            pageChunk <- page(rs)
            next <- if (chunk.nonEmpty && rs.hasMorePages) {
              for {
                nextRs <- IO.fromFuture(IO(rs.fetchNextPage()))
              } yield Some((pageChunk, nextRs))
            } else if (chunk.nonEmpty) {
              Some((pageChunk, rs)).pure
            } else {
              None.pure
            }
          } yield next
        }
        row <- Stream.fromIterator(chunk.iterator)
      } yield extractor(row, csession)

    streamBlocker *> stream
  }

  private[getquill] def simpleBlocking[R, E, A](kyo: A < (Abort[E] & Env[R] & Sync & Async)): A < (Abort[E] & Env[R] & Sync & Async) =
    kyo

  def executeQuery[T](cql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner): CIO[List[T]] = simpleBlocking {
    streamQuery[T](None, cql, prepare, extractor)(info, dc).runCollect.map(_.toList)
  }

  def executeQuerySingle[T](cql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(info: ExecutionInfo, dc: Runner): CIO[T] = simpleBlocking {
    for {
      csession <- Env.get[CassandraKyoSession]
      rs <- execute(cql, prepare, csession, Some(1))
      rows <- Chunk.fromIterator(rs.currentPage().asScala).pure
      singleRow <- IO(handleSingleResult(cql, rows.map(row => extractor(row, csession)).toList))
    } yield singleRow
  }

  def executeAction(cql: String, prepare: Prepare = identityPrepare)(info: ExecutionInfo, dc: Runner): CIO[Unit] = simpleBlocking {
    for {
      csession <- Env.get[CassandraKyoSession]
      prepared <- prepareRowAndLog(cql, prepare)
      _ <- IO.fromFuture(IO(csession.session.executeAsync(prepared)))
    } yield ()
  }

  def executeBatchAction(groups: List[BatchGroup])(info: ExecutionInfo, dc: Runner): CIO[Unit] = simpleBlocking {
    for {
      env <- Env.get[CassandraKyoSession]
      _ <- {
        val batchGroups =
          groups.flatMap {
            case BatchGroup(cql, prepare) =>
              prepare
                .map(prep => executeAction(cql, prep)(info, dc))
          }
        Async.collectAll(batchGroups).map(_ => ())
      }
    } yield ()
  }

  private[getquill] def prepareRowAndLog(cql: String, prepare: Prepare = identityPrepare): CIO[PrepareRow] =
    for {
      csession <- Env.get[CassandraKyoSession]
      boundStatement <- {
        for {
          prepared <- IO.fromFuture(IO(csession.prepareAsync(cql)))
          row <- IO(prepare(prepared, csession))
        } yield row._2
      }
    } yield boundStatement

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
