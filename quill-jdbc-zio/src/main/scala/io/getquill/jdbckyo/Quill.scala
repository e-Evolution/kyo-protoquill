package io.getquill.jdbckyo

import com.typesafe.config.Config
import io.getquill._
import io.getquill.context.KyoJdbc
import io.getquill.context.KyoJdbc.scopedBestEffort
import io.getquill.context.jdbc._
import io.getquill.context.sql.idiom.SqlIdiom
import io.getquill.util.LoadConfig
import kyo.*

import java.io.Closeable
import java.sql.{Connection, SQLException}
import javax.sql.DataSource
import io.getquill.context.json.PostgresJsonExtensions

object Quill {
  class Postgres[+N <: NamingStrategy](val naming: N, override val ds: DataSource)
    extends Quill[PostgresDialect, N] with PostgresJdbcTypes[PostgresDialect, N] with PostgresJsonExtensions {
    val idiom: PostgresDialect = PostgresDialect
    val dsDelegate = new PostgresKyoJdbcContext[N](naming)
  }

  class PostgresLite[+N <: NamingStrategy](val naming: N, override val ds: DataSource)
    extends Quill[PostgresDialect, N] with PostgresJdbcTypes[PostgresDialect, N] {
    val idiom: PostgresDialect = PostgresDialect
    val dsDelegate = new PostgresKyoJdbcContext[N](naming)
  }

  object Postgres {
    def apply[N <: NamingStrategy](naming: N, ds: DataSource) = new PostgresLite[N](naming, ds)
    def fromNamingStrategy[N <: NamingStrategy: Tag](naming: N): Env.Layer[javax.sql.DataSource, Postgres[N]] =
      Env.Layer.from((ds: javax.sql.DataSource) => new Postgres[N](naming, ds))
  }

  class SqlServer[+N <: NamingStrategy](val naming: N, override val ds: DataSource)
    extends Quill[SQLServerDialect, N] with SqlServerJdbcTypes[SQLServerDialect, N] {
    val idiom: SQLServerDialect = SQLServerDialect
    val dsDelegate = new SqlServerKyoJdbcContext[N](naming)
  }
  object SqlServer {
    def apply[N <: NamingStrategy](naming: N, ds: DataSource) = new SqlServer[N](naming, ds)
    def fromNamingStrategy[N <: NamingStrategy: Tag](naming: N): Env.Layer[javax.sql.DataSource, SqlServer[N]] =
      Env.Layer.from((ds: javax.sql.DataSource) => new SqlServer[N](naming, ds))
  }

  class H2[+N <: NamingStrategy](val naming: N, override val ds: DataSource)
    extends Quill[H2Dialect, N] with H2JdbcTypes[H2Dialect, N] {
    val idiom: H2Dialect = H2Dialect
    val dsDelegate = new H2KyoJdbcContext[N](naming)
  }
  object H2 {
    def apply[N <: NamingStrategy](naming: N, ds: DataSource) = new H2[N](naming, ds)
    def fromNamingStrategy[N <: NamingStrategy: Tag](naming: N): Env.Layer[javax.sql.DataSource, H2[N]] =
      Env.Layer.from((ds: javax.sql.DataSource) => new H2[N](naming, ds))
  }

  class Mysql[+N <: NamingStrategy](val naming: N, override val ds: DataSource)
    extends Quill[MySQLDialect, N] with MysqlJdbcTypes[MySQLDialect, N] {
    val idiom: MySQLDialect = MySQLDialect
    val dsDelegate = new MysqlKyoJdbcContext[N](naming)
  }
  object Mysql {
    def apply[N <: NamingStrategy](naming: N, ds: DataSource) = new Mysql[N](naming, ds)
    def fromNamingStrategy[N <: NamingStrategy: Tag](naming: N): Env.Layer[javax.sql.DataSource, Mysql[N]] =
      Env.Layer.from((ds: javax.sql.DataSource) => new Mysql[N](naming, ds))
  }

  class Sqlite[+N <: NamingStrategy](val naming: N, override val ds: DataSource)
    extends Quill[SqliteDialect, N] with SqliteJdbcTypes[SqliteDialect, N] {
    val idiom: SqliteDialect = SqliteDialect
    val dsDelegate = new SqliteKyoJdbcContext[N](naming)
  }
  object Sqlite {
    def apply[N <: NamingStrategy](naming: N, ds: DataSource) = new Sqlite[N](naming, ds)
    def fromNamingStrategy[N <: NamingStrategy: Tag](naming: N): Env.Layer[javax.sql.DataSource, Sqlite[N]] =
      Env.Layer.from((ds: javax.sql.DataSource) => new Sqlite[N](naming, ds))
  }

  class Oracle[+N <: NamingStrategy](val naming: N, override val ds: DataSource)
    extends Quill[OracleDialect, N] with OracleJdbcTypes[OracleDialect, N] {
    val idiom: OracleDialect = OracleDialect
    val dsDelegate = new OracleKyoJdbcContext[N](naming)
  }
  object Oracle {
    def apply[N <: NamingStrategy](naming: N, ds: DataSource) = new Oracle[N](naming, ds)
    def fromNamingStrategy[N <: NamingStrategy: Tag](naming: N): Env.Layer[javax.sql.DataSource, Oracle[N]] =
      Env.Layer.from((ds: javax.sql.DataSource) => new Oracle[N](naming, ds))
  }

  object Connection {
    def acquireScoped: Env.Layer[DataSource, Connection] =
      Env.Layer.from {
        for {
          ds <- Env.get[DataSource]
          r <- Scope.acquireRelease(IO(ds.getConnection))(c => IO(c.close()))
        } yield r
      }
  }

  object DataSource {
    def fromDataSource(ds: => DataSource): Env.Layer[Any, DataSource] =
      Env.Layer.succeed(ds)

    def fromConfig(config: => Config): Env.Layer[Any, Throwable, DataSource] =
      fromConfigClosable(config)

    def fromPrefix(prefix: String): Env.Layer[Any, Throwable, DataSource] =
      fromPrefixClosable(prefix)

    def fromJdbcConfig(jdbcContextConfig: => JdbcContextConfig): Env.Layer[Any, Throwable, DataSource] =
      fromJdbcConfigClosable(jdbcContextConfig)

    def fromConfigClosable(config: => Config): Env.Layer[Any, Throwable, DataSource with Closeable] =
      fromJdbcConfigClosable(JdbcContextConfig(config))

    def fromPrefixClosable(prefix: String): Env.Layer[Any, Throwable, DataSource with Closeable] =
      fromJdbcConfigClosable(JdbcContextConfig(LoadConfig(prefix)))

    def fromJdbcConfigClosable(jdbcContextConfig: => JdbcContextConfig): Env.Layer[Any, Throwable, DataSource with Closeable] =
      ???
  }
}

trait Quill[+Dialect <: SqlIdiom, +Naming <: NamingStrategy] extends QuillBaseContext[Dialect, Naming]
