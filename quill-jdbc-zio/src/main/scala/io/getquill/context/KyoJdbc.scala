package io.getquill.context

import com.typesafe.config.Config
import io.getquill.JdbcContextConfig
import io.getquill.jdbckyo.Quill
import io.getquill.util.{ContextLogger, LoadConfig}
import kyo.*
import kyo.Stream

import java.io.Closeable
import java.sql.{Connection, SQLException}
import javax.sql.DataSource

object KyoJdbc {
  type QIO[T] = T < (Abort[SQLException] & Env[DataSource] & kyo.IO & Async)
  type QStream[T] = Stream[T, (Abort[SQLException] & Env[DataSource] & kyo.IO & Async & Scope & Local[Option[Connection]])]

  type QCIO[T] = T < (Abort[SQLException] & Env[Connection] & kyo.IO & Async)
  type QCStream[T] = Stream[T, (Abort[SQLException] & Env[Connection] & kyo.IO & Async & Scope & Local[Option[Connection]])]

  object QIO {
    def apply[T](t: => T): QIO[T] =
      Sync.defer(t)
  }

  object QCIO {
    def apply[T](t: => T): QCIO[T] =
      Sync.defer(t)
  }

  object DataSourceLayer {
    @deprecated("Use Quill.DataSource.fromConfig instead", "3.3.0")
    val live: Env[Connection] = ???

    @deprecated("Use Quill.DataSource.fromConfig instead", "3.3.0")
    def fromDataSource(ds: => DataSource): Env[DataSource] = ???

    @deprecated("Use Quill.DataSource.fromConfig instead", "3.3.0")
    def fromConfig(config: => Config): Env[DataSource] = ???

    @deprecated("Use Quill.DataSource.fromPrefix instead", "3.3.0")
    def fromPrefix(prefix: String): Env[DataSource] = ???

    @deprecated("Use Quill.DataSource.fromJdbcConfig instead", "3.3.0")
    def fromJdbcConfig(jdbcContextConfig: => JdbcContextConfig): Env[DataSource] = ???

    @deprecated("Use Quill.DataSource.fromConfigClosable instead", "3.3.0")
    def fromConfigClosable(config: => Config): Env[DataSource with Closeable] = ???

    @deprecated("Use Quill.DataSource.fromPrefixClosable instead", "3.3.0")
    def fromPrefixClosable(prefix: String): Env[DataSource with Closeable] = ???

    @deprecated("Use Quill.DataSource.fromJdbcConfigClosable instead", "3.3.0")
    def fromJdbcConfigClosable(jdbcContextConfig: => JdbcContextConfig): Env[DataSource with Closeable] = ???
  }

  implicit class QuillKyoDataSourceExt[T](qkyo: QIO[T]) {
    def implicitDS(implicit implicitEnv: io.getquill.context.qzio.KyoImplicitSyntax.Implicit[DataSource]): QCIO[T] =
      ???
  }

  implicit class QuillKyoSomeDataSourceExt[T, R](qkyo: T < (Abort[Throwable] & Env[DataSource with R])) {
    def implicitSomeDS(implicit implicitEnv: io.getquill.context.qzio.KyoImplicitSyntax.Implicit[DataSource]): T < (Abort[Throwable] & Env[R]) =
      ???
  }

  implicit class QuillKyoExtPlain[T, R](qkyo: T < (Abort[Throwable] & Env[Connection with R])) {
    def onDataSource: QIO[T] = ???

    def implicitDS(implicit implicitEnv: io.getquill.context.qzio.KyoImplicitSyntax.Implicit[DataSource]): QIO[T] =
      onDataSource
  }

  implicit class QuillKyoExt[T, R](qkyo: T < (Abort[Throwable] & Env[Connection with R])) {
    def onSomeDataSource: T < (Abort[Throwable] & Env[DataSource with R]) = ???
  }

  private[getquill] def scopedBestEffort[R, E, A <: AutoCloseable](effect: A < (Abort[E] & R)): A < (Abort[E] & R & Scope & Sync) =
    Scope.acquireRelease(effect)(resource =>
      Sync.defer(resource.close())
    )

  private[getquill] val streamBlocker: Stream[Any, Any] =
    Stream.empty

  private val logger = ContextLogger(getClass)
}
