package io.getquill

import io.getquill.context.sql.PeopleSpec
import io.getquill.context.qzio.KyoJdbcContext
import io.getquill.jdbckyo.Quill

trait PeopleKyoSpec extends PeopleSpec with KyoSpec {

  val context: Quill[_, _]
  import context._

  inline def `Ex 11 query` = quote(query[Person])
  val `Ex 11 expected` = peopleEntries
}

trait PeopleKyoProxySpec extends PeopleSpec with KyoProxySpec {

  val context: KyoJdbcContext[_, _]
  import context._

  val `Ex 11 query` = quote(query[Person])
  val `Ex 11 expected` = peopleEntries
}
