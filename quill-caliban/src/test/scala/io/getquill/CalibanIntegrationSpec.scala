package io.getquill

import caliban.execution.Field
import caliban.graphQL
import caliban.RootResolver
import io.getquill.CalibanIntegration._
import caliban.schema.Schema.auto._
import caliban.schema.ArgBuilder.auto._
import kyo.*
import kyo.given

class CalibanIntegrationSpec extends CalibanSpec {
  import Ctx._

  // Kyo effect type equivalent to ZIO's Task
  type KyoTask[A] = A < (Abort[Throwable] & Async)

  object Flat {
    import FlatSchema._
    object Dao {
      def personAddress(columns: List[String], filters: Map[String, String]): KyoTask[List[PersonAddress]] =
        Abort.catching[Throwable] {
          val result = Ctx.run {
            query[PersonT].leftJoin(query[AddressT]).on((p, a) => p.id == a.ownerId)
              .map((p, a) => PersonAddress(p.id, p.first, p.last, p.age, a.map(_.street)))
              .filterByKeys(filters)
              .take(10)
          }
          println(s"Results: $result for columns: $columns")
          result
        }
    }
  }

  case class Queries(
    personAddressFlat: Field => (ProductArgs[FlatSchema.PersonAddress] => KyoTask[List[FlatSchema.PersonAddress]]),
  )

  val api = graphQL(
      RootResolver(
        Queries(
          personAddressFlat =>
            (productArgs =>
              Flat.Dao.personAddress(quillColumns(personAddressFlat), productArgs.keyValues)
            )
        )
      )
    )

  "Caliban integration should work for flat object" - {
    "with no filters" in {
      val query =
        """
        {
          personAddressFlat {
            id
          }
        }"""
      unsafeRunQuery(query) mustEqual """{"personAddressFlat":[{"id":1},{"id":2},{"id":3}]}"""
    }

    "with filteration column included" in {
      val query =
        """
        {
          personAddressFlat(first: "One") {
            id
            first
            last
            street
          }
        }"""
      unsafeRunQuery(query) mustEqual """{"personAddressFlat":[{"id":1,"first":"One","last":"A","street":"123 St"}]}"""
    }
    "with no filteration column excluded" in {
      val query =
        """
        {
          personAddressFlat(first: "One") {
            id
            last
            street
          }
        }"""
      unsafeRunQuery(query) mustEqual """{"personAddressFlat":[{"id":1,"last":"A","street":"123 St"}]}"""
    }
  }
}
