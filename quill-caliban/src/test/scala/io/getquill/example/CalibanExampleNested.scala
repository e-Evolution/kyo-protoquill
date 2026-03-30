package io.getquill

import caliban.graphQL
import caliban.schema.Annotations.GQLDescription
import caliban._

import io.getquill._
import io.getquill.context.qkyo.KyoImplicitSyntax._
import io.getquill.util.LoadConfig

import java.io.Closeable
import javax.sql.DataSource

import scala.language.postfixOps
import caliban.execution.Field
import caliban.schema.ArgBuilder
import io.getquill.CalibanIntegration._
import io.getquill.util.ContextLogger
import io.getquill
import io.getquill.NestedSchema.*
import caliban.schema.Schema.auto._
import caliban.schema.ArgBuilder.auto._

import kyo.*
import kyo.given

object DaoNested {
  case class PersonAddressPlanQuery(plan: String, pa: List[PersonAddressNested])
  private val logger = ContextLogger(classOf[DaoNested.type])

  lazy val ds = JdbcContextConfig(LoadConfig("testPostgresDB")).dataSource
  object Ctx extends PostgresKyoJdbcContext(Literal, ds)
  import Ctx._

  inline def q(inline columns: List[String], inline filters: Map[String, String]) =
    quote {
      query[PersonT].leftJoin(query[AddressT]).on((p, a) => p.id == a.ownerId)
        .map((p, a) => PersonAddressNested(p.id, p.name, p.age, a.map(_.street)))
        .filterColumns(columns)
        .filterByKeys(filters)
        .take(10)
    }
  inline def plan(inline columns: List[String], inline filters: Map[String, String]) =
    quote { sql"EXPLAIN ${q(columns, filters)}".pure.as[Query[String]] }

  def personAddress(columns: List[String], filters: Map[String, String]): KyoTask[List[PersonAddressNested]] = {
    println(s"Getting columns: $columns")
    Abort.catching[Throwable] {
      Ctx.run(q(columns, filters))
    }
  }

  def personAddressPlan(columns: List[String], filters: Map[String, String]): KyoTask[String] = {
    Abort.catching[Throwable] {
      Ctx.run(plan(columns, filters), OuterSelectWrap.Never).mkString("\n")
    }
  }

  def resetDatabase(): KyoTask[Unit] =
    Abort.catching[Throwable] {
      Ctx.run(sql"TRUNCATE TABLE AddressT, PersonT RESTART IDENTITY".as[Delete[PersonT]])
      Ctx.run(liftQuery(ExampleData.people).foreach(row => query[PersonT].insertValue(row)))
      Ctx.run(liftQuery(ExampleData.addresses).foreach(row => query[AddressT].insertValue(row)))
      ()
    }
} // end DaoNested

object CalibanExampleNested {
  case class Queries(
      personAddress: Field => (ProductArgs[PersonAddressNested] => KyoTask[List[PersonAddressNested]]),
      personAddressPlan: Field => (ProductArgs[PersonAddressNested] => KyoTask[DaoNested.PersonAddressPlanQuery])
  )

  val api = graphQL(
    RootResolver(
      Queries(
        personAddress =>
          (productArgs =>
            DaoNested.personAddress(quillColumns(personAddress), productArgs.keyValues)
          ),
        personAddressPlan =>
          (productArgs => {
            val columns = quillColumns(personAddressPlan)
            val filters = productArgs.keyValues
            for {
              plan <- DaoNested.personAddressPlan(columns, filters)
              pa   <- DaoNested.personAddress(columns, filters)
            } yield DaoNested.PersonAddressPlanQuery(plan, pa)
          })
      )
    ),
    Nil, // directives
    Nil, // schemaDirectives
    None // schemaDescription
  )

  def main(args: Array[String]): Unit = {
    import zio.{Unsafe, Runtime}
    import caliban.quick._
    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.run(
        for {
          _ <- kyo.ZIOs.run(DaoNested.resetDatabase())
          _ <- api.runServer(
            port = 8088,
            apiPath = "/api/graphql",
            graphiqlPath = Some("/graphiql")
          )
        } yield ()
      ).getOrThrowFiberFailure()
    }
  }
} // end CalibanExampleNested
