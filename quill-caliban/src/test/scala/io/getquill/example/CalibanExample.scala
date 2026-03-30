package io.getquill

import caliban.graphQL
import caliban.schema.Annotations.GQLDescription
import caliban._
import caliban.quick._ 

import io.getquill._
import io.getquill.context.KyoImplicitSyntax._
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

import caliban._

object Dao {
  case class PersonAddressPlanQuery(plan: String, pa: List[PersonAddress])
  private val logger = ContextLogger(classOf[Dao.type])

  object Ctx extends PostgresKyoJdbcContext(Literal)
  import Ctx._
  lazy val ds = JdbcContextConfig(LoadConfig("testPostgresDB")).dataSource
  given Implicit[DataSource] = Implicit(ds)

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

  def personAddress(columns: List[String], filters: Map[String, String]) = {
    println(s"Getting columns: $columns")
    run(q(columns, filters)).implicitDS.mapError(e => {
      logger.underlying.error("personAddress query failed", e)
      e
    })
  }

  def personAddressPlan(columns: List[String], filters: Map[String, String]) = {
    run(plan(columns, filters), OuterSelectWrap.Never).map(_.mkString("\n")).implicitDS.mapError(e => {
      logger.underlying.error("personAddressPlan query failed", e)
      e
    })
  }

  def resetDatabase() =
    (for {
      _ <- run(sql"TRUNCATE TABLE AddressT, PersonT RESTART IDENTITY".as[Delete[PersonT]])
      _ <- run(liftQuery(ExampleData.people).foreach(row => query[PersonT].insertValue(row)))
      _ <- run(liftQuery(ExampleData.addresses).foreach(row => query[AddressT].insertValue(row)))
    } yield ()).implicitDS
} // end Dao

object CalibanExample {
  case class Queries(
      personAddress: Field => (ProductArgs[PersonAddress] => Task[List[PersonAddress]]),
      personAddressPlan: Field => (ProductArgs[PersonAddress] => Task[Dao.PersonAddressPlanQuery])
  )

  val api = graphQL(
    RootResolver(
      Queries(
        personAddress =>
          (productArgs =>
            Dao.personAddress(quillColumns(personAddress), productArgs.keyValues)
          ),
        personAddressPlan =>
          (productArgs =>
            (Dao.personAddressPlan(quillColumns(personAddress), productArgs.keyValues)
              zip Dao.personAddress(quillColumns(personAddress), productArgs.keyValues))
              .map { case (pa, plan) => Dao.PersonAddressPlanQuery(pa, plan) }
          )
      )
    ),
    Nil, // directives
    Nil, // schemaDirectives
    None // schemaDescription
  )

  def main(args: Array[String]): Unit = {
    val ds = JdbcContextConfig(LoadConfig("testPostgresDB")).dataSource
    val app = for {
      _ <- Dao.resetDatabase()
      _ <- api.interpreter.flatMap { interpreter =>
        interpreter.runServer(
          port = 8088,
          apiPath = "/api/graphql",
          graphiqlPath = Some("/graphiql")
        )
      }
    } yield ()
    app.runSyncUnsafe(ds)
  }
} // end CalibanExample
