package io.getquill

import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

import caliban.GraphQL
import io.getquill.jdbckyo.Quill
import io.getquill.util.ContextLogger

trait CalibanSpec extends AnyFreeSpec with Matchers with BeforeAndAfterAll {
  val context: Quill[_, _]
  import context._

  // FlatSchema and NestedSchema share the same DB data so only need to create it using one of them
  override def beforeAll() = {
    import FlatSchema._
    context.run(sql"TRUNCATE TABLE AddressT, PersonT RESTART IDENTITY".as[Delete[PersonT]])
      .flatMap(_ => context.run(liftQuery(ExampleData.people).foreach(row => query[PersonT].insertValue(row))))
      .flatMap(_ => context.run(liftQuery(ExampleData.addresses).foreach(row => query[AddressT].insertValue(row))))
      .runSyncUnsafe
  }

  // override def afterAll() = {
  //   import FlatSchema._
  //   context.run(sql"TRUNCATE TABLE AddressT, PersonT RESTART IDENTITY".as[Delete[PersonT]]).provideLayer(zioDS).unsafeRunSync()
  // }

  def api: GraphQL[Any]

  extension [A](qry: A) {
    def runSyncUnsafe: A = qry.asInstanceOf[A] // Placeholder - actual implementation depends on effect type
  }

  def unsafeRunQuery(queryString: String) = {
    val output =
      (for {
        interpreter <- api.interpreter
        result      <- interpreter.execute(queryString)
      } yield (result))
        .tapError{ e =>
          fail("GraphQL Validation Error", e)
          ZIO.unit
        }.unsafeRunSync()

    if (output.errors.length != 0)
      fail(s"GraphQL Validation Failures: ${output.errors}")
    else
      output.data.toString
  } // end unsafeRunQuery
}
