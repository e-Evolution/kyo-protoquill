package io.getquill.context

import com.typesafe.config.Config
import io.getquill.JdbcContextConfig
import io.getquill.jdbckyo.Quill
import io.getquill.util.{ContextLogger, LoadConfig}
import kyo.*
import io.getquill.util.{ContextLogger, LoadConfig}

import java.io.Closeable
import java.sql.{Connection, SQLException}
import javax.sql.DataSource

object KyoJdbc {
  type QIO[T] = T < (Abort[SQLException] & Env[DataSource] & Sync & Async)
  type QStream[T] = Stream[T, (Abort[SQLException] & Env[DataSource] & Sync & Async)]

  type QCIO[T] = T < (Abort[SQLException] & Env[Connection] & Sync & Async)
  type QCStream[T] = Stream[T, (Abort[SQLException] & Env[Connection] & Sync & Async)]

  object QIO {
    def apply[T](t: => T): QIO[T] = Sync.defer(t).mapError(e => e match {
      case sq: SQLException => sq
      case other => throw other
    })
  }

  object QCIO {
    def apply[T](t: => T): QCIO[T] = Sync.defer(t).mapError(e => e match {
      case sq: SQLException => sq
      case other => throw other
    })
  }

  object DataSourceLayer {
    @deprecated("Use Quill.Connection.acquireScoped instead", "3.3.0")
    val live: Env.Layer[DataSource, Connection] = ???

    @deprecated("Use Env.succeed(dataSource:DataSource) instead", "3.3.0")
    def fromDataSource(ds: => DataSource): Env.Layer[Any, DataSource] =
      Env.Layer.succeed(ds)

    @deprecated("Use Quill.DataSource.fromConfig instead", "3.3.0")
    def fromConfig(config: => Config): Env.Layer[Any, Throwable, DataSource] =
      fromConfigClosable(config)

    @deprecated("Use Quill.DataSource.fromPrefix instead", "3.3.0")
    def fromPrefix(prefix: String): Env.Layer[Any, Throwable, DataSource] =
      fromPrefixClosable(prefix)

    @deprecated("Use Quill.DataSource.fromJdbcConfig instead", "3.3.0")
    def fromJdbcConfig(jdbcContextConfig: => JdbcContextConfig): Env.Layer[Any, Throwable, DataSource] =
      fromJdbcConfigClosable(jdbcContextConfig)

    @deprecated("Use Quill.DataSource.fromConfigClosable instead", "3.3.0")
    def fromConfigClosable(config: => Config): Env.Layer[Any, Throwable, DataSource with Closeable] =
      fromJdbcConfigClosable(JdbcContextConfig(config))

    @deprecated("Use Quill.DataSource.fromPrefixClosable instead", "3.3.0")
    def fromPrefixClosable(prefix: String): Env.Layer[Any, Throwable, DataSource with Closeable] =
      fromJdbcConfigClosable(JdbcContextConfig(LoadConfig(prefix)))

    @deprecated("Use Quill.DataSource.fromJdbcConfigClosable instead", "3.3.0")
    def fromJdbcConfigClosable(jdbcContextConfig: => JdbcContextConfig): Env.Layer[Any, Throwable, DataSource with Closeable] =
      ???
  }

  implicit class QuillKyoDataSourceExt[T](qkyo: QIO[T]) {

    import io.getquill.context.qzio.KyoImplicitSyntax._

    def implicitDS(implicit implicitEnv: KyoImplicitSyntax.Implicit[DataSource]): QCIO[T] =
      Env.run(implicitEnv.env)(qkyo).mapError {
        case sq: SQLException => sq
        case other => throw other
      }
  }

  implicit class QuillKyoSomeDataSourceExt[T, R](qkyo: T < (Abort[Throwable] & Env[DataSource with R] & S)) {
    import io.getquill.context.qzio.KyoImplicitSyntax._
    def implicitSomeDS(implicit implicitEnv: KyoImplicitSyntax.Implicit[DataSource]): T < (Abort[Throwable] & Env[R] & S) =
      ???
  }

  implicit class QuillKyoExtPlain[T](qkyo: QCIO[T]) {
    import io.getquill.context.qzio.KyoImplicitSyntax._
    def onDataSource: QIO[T] =
      Scope.run {
        for {
          ds <- Env.get[DataSource]
          conn <- Scope.acquireRelease(IO(ds.getConnection))(c => IO(c.close()))
          result <- Env.run(conn)(qkyo)
        } yield result
      }.mapError {
        case sq: SQLException => sq
        case other => throw other
      }

    def implicitDS(implicit implicitEnv: KyoImplicitSyntax.Implicit[DataSource]): QIO[T] =
      onDataSource
  }

  implicit class QuillKyoExt[T, R](qkyo: T < (Abort[Throwable] & Env[Connection with R] & S)) {
    def onSomeDataSource: T < (Abort[Throwable] & Env[DataSource with R] & S) = ???
  }

  private[getquill] def scopedBestEffort[R, E, A <: AutoCloseable](effect: A < (Abort[E] & R & S)): A < (Abort[E] & Scope & R & S) =
    Scope.acquireRelease(effect)(resource =>
      IO(resource.close()).catchAll(e => IO(logger.underlying.error(s"close() of resource failed", e))).unit
    )

  private[getquill] val streamBlocker: Stream[Any, Any] =
    Stream.empty

  private[getquill] val logger = ContextLogger(classOf[KyoJdbc].getClass)
}
