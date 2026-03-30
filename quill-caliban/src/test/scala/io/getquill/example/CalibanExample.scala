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
import io.getquill.FlatSchema._
import caliban.schema.Schema.auto._
import caliban.schema.ArgBuilder.auto._

import kyo.*
import kyo.given

// Kyo effect type equivalent to ZIO's Task
type KyoTask[A] = A < (Abort[Throwable] & Async)

object Dao {
  case class PersonAddressPlanQuery(plan: String, pa: List[PersonAddress])
  private val logger = ContextLogger(classOf[Dao.type])

  lazy val ds = JdbcContextConfig(LoadConfig("testPostgresDB")).dataSource
  object Ctx extends PostgresKyoJdbcContext(Literal, ds)
  import Ctx._

  inline def q(inline columns: List[String], inline filters: Map[String, String]) =
    quote {
      query[PersonT].leftJoin(query[AddressT]).on((p, a) => p.id == a.ownerId)
        .map((p, a) => PersonAddress(p.id, p.first, p.last, p.age, a.map(_.street)))
        .filterColumns(columns)
        .filterByKeys(filters)
        .take(10)
    }
  inline def plan(inline columns: List[String], inline filters: Map[String, String]) =
    quote { sql"EXPLAIN ${q(columns, filters)}".pure.as[Query[String]] }

  def personAddress(columns: List[String], filters: Map[String, String]): KyoTask[List[PersonAddress]] = {
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
} // end Dao

object CalibanExample {
  case class Queries(
      personAddress: Field => (ProductArgs[PersonAddress] => KyoTask[List[PersonAddress]]),
      personAddressPlan: Field => (ProductArgs[PersonAddress] => KyoTask[Dao.PersonAddressPlanQuery])
  )

  val api = graphQL(
    RootResolver(
      Queries(
        personAddress =>
          (productArgs =>
            Dao.personAddress(quillColumns(personAddress), productArgs.keyValues)
          ),
        personAddressPlan =>
          (productArgs => {
            val columns = quillColumns(personAddressPlan)
            val filters = productArgs.keyValues
            for {
              plan <- Dao.personAddressPlan(columns, filters)
              pa   <- Dao.personAddress(columns, filters)
            } yield Dao.PersonAddressPlanQuery(plan, pa)
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
          _ <- kyo.ZIOs.run(Dao.resetDatabase())
          _ <- api.runServer(
            port = 8088,
            apiPath = "/api/graphql",
            graphiqlPath = Some("/graphiql")
          )
        } yield ()
      ).getOrThrowFiberFailure()
    }
  }
} // end CalibanExample
