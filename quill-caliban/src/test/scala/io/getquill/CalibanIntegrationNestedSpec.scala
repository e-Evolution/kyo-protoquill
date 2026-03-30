package io.getquill

import caliban.execution.Field
import caliban.graphQL
import caliban.RootResolver
import io.getquill.CalibanIntegration._
import caliban.schema._
import caliban.schema.Schema.auto._
import caliban.schema.ArgBuilder.auto._
import kyo.*
import kyo.given

class CalibanIntegrationNestedSpec extends CalibanSpec {
  import Ctx._

  // Kyo effect type equivalent to ZIO's Task
  type KyoTask[A] = A < (Abort[Throwable] & Async)

  object Nested {
    import NestedSchema._
    object Dao {
      def personAddress(columns: List[String], filters: Map[String, String]): KyoTask[List[PersonAddressNested]] =
        Abort.catching[Throwable] {
          val result = Ctx.run {
            query[PersonT].leftJoin(query[AddressT]).on((p, a) => p.id == a.ownerId)
              .map((p, a) => PersonAddressNested(p.id, p.name, p.age, a.map(_.street)))
              .filterByKeys(filters)
              .filterColumns(columns)
              .take(10)
          }
          println(s"Results: $result for columns: $columns and filters: ${io.getquill.util.Messages.qprint(filters)}")
          result
        }
    }
  }

  case class Queries(
    personAddressNested: Field => (ProductArgs[NestedSchema.PersonAddressNested] => KyoTask[List[NestedSchema.PersonAddressNested]])
  )

  val api = graphQL(
      RootResolver(
        Queries(
          personAddressNested =>
            (productArgs =>
              Nested.Dao.personAddress(quillColumns(personAddressNested), productArgs.keyValues)
            ),
        )
      )
    )

  "Caliban integration should work for nested object" - {
    "with no top-level filter" in {
      val query =
        """
        {
          personAddressNested {
            id
          }
        }"""
      unsafeRunQuery(query) mustEqual """{"personAddressNested":[{"id":1},{"id":2},{"id":3}]}"""
    }

    "top-level filter is nested field" in {
      val query =
        """
        {
          personAddressNested(name: { first: "One" }) {
            id
            name {
              first
            }
          }
        }"""
      val output = unsafeRunQuery(query)
      println("========== QUERY OUTPUT: " + output)
      output mustEqual """{"personAddressNested":[{"id":1,"name":{"first":"One"}}]}"""
    }

    "top-level filter is nested field and does not occur in body" in {
      val query =
        """
        {
          personAddressNested(name: { first: "One" }) {
            id
          }
        }"""
      unsafeRunQuery(query) mustEqual """{"personAddressNested":[{"id":1}]}"""
    }

    "with one field in the nested object" in {
      val query =
        """
        {
          personAddressNested(id: 1) {
            id
            age
            name {
              first
            }
          }
        }"""
      unsafeRunQuery(query) mustEqual """{"personAddressNested":[{"id":1,"age":44,"name":{"first":"One"}}]}"""
    }

    "with multiple fields in the nested object" in {
      val query =
        """
        {
          personAddressNested(id: 1) {
            id
            age
            name {
              first
              last
            }
          }
        }"""
      unsafeRunQuery(query) mustEqual """{"personAddressNested":[{"id":1,"age":44,"name":{"first":"One","last":"A"}}]}"""
    }
  }
}
