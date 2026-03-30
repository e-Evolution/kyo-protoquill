package io.getquill.context.qkyo

import io.getquill.NamingStrategy
import io.getquill.context.{Context, ExecutionInfo, ContextVerbStream}
import kyo.*
import kyo.Stream

trait KyoContext[+Idiom <: io.getquill.idiom.Idiom, +Naming <: NamingStrategy]
    extends Context[Idiom, Naming]
    with ContextVerbStream[Idiom, Naming] {

  type Error
  type Environment

  override type StreamResult[T] = Stream[T, (Abort[Error] & Async)]
  override type Result[T] = T < (Abort[Error] & Async)
  override type RunQueryResult[T] = List[T]
  override type RunQuerySingleResult[T] = T

  def executeQuery[T](sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(
      info: ExecutionInfo,
      dc: Runner
  ): Result[List[T]]

  def executeQuerySingle[T](sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(
      info: ExecutionInfo,
      dc: Runner
  ): Result[T]
}
