package io.getquill.context

import com.typesafe.config.Config
import io.getquill.JdbcContextConfig
import io.getquill.util.{ContextLogger, LoadConfig}
import kyo.*
import kyo.Stream

import java.io.Closeable
import java.sql.{Connection, SQLException}
import javax.sql.DataSource

object KyoJdbc {
  type QIO[T] = T < (Abort[SQLException] & Env[DataSource] & kyo.IO & Async)
  type QStream[T] = Stream[T, (Abort[SQLException] & Env[DataSource] & kyo.IO & Async & Scope)]

  type QCIO[T] = T < (Abort[SQLException] & Env[Connection] & kyo.IO & Async)
  type QCStream[T] = Stream[T, (Abort[SQLException] & Env[Connection] & kyo.IO & Async & Scope)]

  object QIO {
    def apply[T](t: => T): QIO[T] =
      Abort.catching[SQLException](kyo.IO.defer(t))
  }

  object QCIO {
    def apply[T](t: => T): QCIO[T] =
      Abort.catching[SQLException](kyo.IO.defer(t))
  }

  /**
   * Kyo Layer-based equivalents of ZIO's DataSourceLayer.
   *
   * ZIO used ZLayer[R, E, A] for dependency wiring. Kyo uses Layer[Out, S].
   *
   * Mapping:
   *   ZLayer[DataSource, SQLException, Connection]  ->  Layer[Connection, Env[DataSource] & IO & Abort[SQLException]]
   *   ZLayer[Any, Throwable, DataSource]            ->  Layer[DataSource, IO & Abort[Throwable]]
   */
  object DataSourceLayer {

    /**
     * Layer that acquires a Connection from a DataSource.
     * Equivalent to ZIO's: ZLayer.scoped { ZIO.service[DataSource].flatMap(ds => acquireConnection(ds)) }
     */
    val live: Layer[Connection, Env[DataSource] & kyo.IO & Abort[SQLException]] =
      Layer.from { (ds: DataSource) =>
        Abort.catching[SQLException](kyo.IO.defer(ds.getConnection))
      }

    /** Create a Layer that provides a DataSource from a given instance */
    def fromDataSource(ds: => DataSource): Layer[DataSource, kyo.IO & Abort[Throwable]] =
      Layer(Abort.catching[Throwable](kyo.IO.defer(ds)))

    /** Create a Layer that provides a DataSource from a Config */
    def fromConfig(config: => Config): Layer[DataSource, kyo.IO & Abort[Throwable]] =
      fromJdbcConfig(JdbcContextConfig(config))

    /** Create a Layer that provides a DataSource from a config prefix */
    def fromPrefix(prefix: String): Layer[DataSource, kyo.IO & Abort[Throwable]] =
      fromJdbcConfig(JdbcContextConfig(LoadConfig(prefix)))

    /** Create a Layer that provides a DataSource from a JdbcContextConfig */
    def fromJdbcConfig(jdbcContextConfig: => JdbcContextConfig): Layer[DataSource, kyo.IO & Abort[Throwable]] =
      Layer {
        Abort.catching[Throwable] {
          kyo.IO.defer(jdbcContextConfig.dataSource)
        }
      }

    /** Create a Layer that provides a Closeable DataSource from a Config */
    def fromConfigClosable(config: => Config): Layer[DataSource with Closeable, kyo.IO & Abort[Throwable]] =
      fromJdbcConfigClosable(JdbcContextConfig(config))

    /** Create a Layer that provides a Closeable DataSource from a config prefix */
    def fromPrefixClosable(prefix: String): Layer[DataSource with Closeable, kyo.IO & Abort[Throwable]] =
      fromJdbcConfigClosable(JdbcContextConfig(LoadConfig(prefix)))

    /** Create a Layer that provides a Closeable DataSource from a JdbcContextConfig */
    def fromJdbcConfigClosable(jdbcContextConfig: => JdbcContextConfig): Layer[DataSource with Closeable, kyo.IO & Abort[Throwable]] =
      Layer {
        Abort.catching[Throwable] {
          kyo.IO.defer(jdbcContextConfig.dataSource)
        }
      }
  }

  implicit class QuillKyoDataSourceExt[T](qkyo: QIO[T]) {
    def implicitDS(implicit implicitEnv: io.getquill.context.qkyo.KyoImplicitSyntax.Implicit[DataSource]): T < (Abort[SQLException] & kyo.IO & Async) =
      Env.run(implicitEnv.env)(qkyo)
  }

  implicit class QuillKyoSomeDataSourceExt[T, R](qkyo: T < (Abort[Throwable] & Env[DataSource with R])) {
    def implicitSomeDS(implicit implicitEnv: io.getquill.context.qkyo.KyoImplicitSyntax.Implicit[DataSource]): T < (Abort[Throwable] & Env[R]) =
      Env.run(implicitEnv.env)(qkyo)
  }

  implicit class QuillKyoExtPlain[T](qkyo: T < (Abort[Throwable] & Env[Connection] & kyo.IO & Async)) {
    /**
     * Convert a Connection-level computation to a DataSource-level computation.
     * Acquires a connection from the DataSource, runs the computation, and releases it.
     */
    def onDataSource: T < (Abort[Throwable] & Env[DataSource] & kyo.IO & Async) =
      for {
        ds <- Env.get[DataSource]
        conn <- Abort.catching[Throwable](kyo.IO.defer(ds.getConnection))
        result <- Abort.run[Throwable] {
          Env.run(conn: Connection)(qkyo)
        }
        _ <- kyo.IO.defer(conn.close())
        value <- Abort.get(result)
      } yield value

    def implicitDS(implicit implicitEnv: io.getquill.context.qkyo.KyoImplicitSyntax.Implicit[DataSource]): T < (Abort[Throwable] & kyo.IO & Async) =
      Env.run(implicitEnv.env: DataSource)(onDataSource)
  }

  implicit class QuillKyoExt[T, R](qkyo: T < (Abort[Throwable] & Env[Connection with R])) {
    def onSomeDataSource: T < (Abort[Throwable] & Env[DataSource with R] & kyo.IO & Async) =
      for {
        ds <- Env.get[DataSource]
        conn <- Abort.catching[Throwable](kyo.IO.defer(ds.getConnection))
        result <- Abort.run[Throwable] {
          Env.run(conn: Connection)(qkyo.asInstanceOf[T < (Abort[Throwable] & Env[Connection])])
        }
        _ <- kyo.IO.defer(conn.close())
        value <- Abort.get(result)
      } yield value
  }

  /**
   * Acquire a resource and ensure it's closed on release, logging errors
   * rather than failing (best-effort close).
   */
  private[getquill] def scopedBestEffort[A <: AutoCloseable, S](effect: A < S): A < (S & Scope & kyo.IO) =
    effect.flatMap { resource =>
      Scope.acquireRelease(resource) { r =>
        kyo.IO.defer {
          try r.close()
          catch { case e: Exception => logger.underlying.error("close() of resource failed", e) }
        }
      }
    }

  private[getquill] val streamBlocker: Stream[Any, Any] =
    Stream.empty

  private val logger = ContextLogger(getClass)
}
