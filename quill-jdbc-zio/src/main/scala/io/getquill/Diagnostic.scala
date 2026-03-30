package io.getquill

import kyo.*

import io.getquill.context.{ExecutionInfo, KyoQuillLog}

def getLastExecutedQuery(): Option[String] < Local[Option[String]] =
  KyoQuillLog.latestSqlQuery.get

def getLastExecutionInfo(): Option[ExecutionInfo] < Local[Option[ExecutionInfo]] =
  KyoQuillLog.latestExecutionInfo.get