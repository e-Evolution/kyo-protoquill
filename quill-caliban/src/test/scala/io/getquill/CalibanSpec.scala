package io.getquill

import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

import caliban.GraphQL
import io.getquill.util.LoadConfig
import kyo.*

object CalibanSpec {
  lazy val ds: javax.sql.DataSource = JdbcContextConfig(LoadConfig("testPostgresDB")).dataSource
}

trait CalibanSpec extends AnyFreeSpec with Matchers with BeforeAndAfterAll {
  object Ctx extends PostgresKyoJdbcContext(Literal, CalibanSpec.ds)
  import Ctx._

  override def beforeAll() = {
    import FlatSchema._
    Ctx.run(sql"TRUNCATE TABLE AddressT, PersonT RESTART IDENTITY".as[Delete[PersonT]])
    Ctx.run(liftQuery(ExampleData.people).foreach(row => query[PersonT].insertValue(row)))
    Ctx.run(liftQuery(ExampleData.addresses).foreach(row => query[AddressT].insertValue(row)))
  }

  def api: GraphQL[Any]

  // kyo-caliban's own tests use ZIO's unsafe runner for interpreter execution
  // because Caliban's api.interpreter returns ZIO natively
  def unsafeRunQuery(queryString: String) = {
    import zio.{Unsafe, Runtime}

    val output = Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.run(
        api.interpreter.flatMap(_.execute(queryString))
      ).getOrThrowFiberFailure()
    }

    if (output.errors.length != 0)
      fail(s"GraphQL Validation Failures: ${output.errors}")
    else
      output.data.toString
  }
}
