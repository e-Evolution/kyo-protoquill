package io.getquill.context

import kyo.*

object KyoQuillLog {
  val latestExecutionInfo: Local[Option[ExecutionInfo]] =
    Local.init(None)

  final class ExecutionInfoAware(val executionInfo: () => ExecutionInfo) { self =>
    def apply[A, S](kyo: A < S): A < S =
      latestExecutionInfo.let(Some(executionInfo()))(kyo)
  }

  def withExecutionInfo(info: => ExecutionInfo): ExecutionInfoAware =
    new ExecutionInfoAware(() => info)


  val latestSqlQuery: Local[Option[String]] =
    Local.init(None)

  final class SqlQueryAware(val sqlQuery: () => String) {
    self =>
    def apply[A, S](kyo: A < S): A < S =
      latestSqlQuery.let(Some(sqlQuery()))(kyo)
  }

  def withSqlQuery(sqlQuery: => String): SqlQueryAware =
    new SqlQueryAware(() => sqlQuery)
}
