package io.getquill.context

import io.getquill.context.ExecutionInfo
import kyo._

object KyoQuillLog {
  val latestExecutionInfo: kyo.Local[Option[ExecutionInfo]] =
    kyo.Local.init(None)

  final class ExecutionInfoAware(val executionInfo: () => ExecutionInfo) { self =>
    def apply[A, S](kyo: A < S): A < S =
      latestExecutionInfo.let(Some(executionInfo()))(kyo)
  }

  def withExecutionInfo(info: => ExecutionInfo): ExecutionInfoAware =
    ExecutionInfoAware(() => info)

  val latestSqlQuery: kyo.Local[Option[String]] =
    kyo.Local.init(None)

  final class SqlQueryAware(val sqlQuery: () => String) {
    self =>
    def apply[A, S](kyo: A < S): A < S =
      latestSqlQuery.let(Some(sqlQuery()))(kyo)
  }

  def withSqlQuery(sqlQuery: => String): SqlQueryAware =
    SqlQueryAware(() => sqlQuery)
}