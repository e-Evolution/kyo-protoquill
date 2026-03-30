package io.getquill

import org.scalatest.BeforeAndAfterAll

object KyoSpec {
  def runUnsafe[T](t: T): T = t
}

trait KyoSpec extends Spec with BeforeAndAfterAll {

  def accumulate[T](stream: Iterable[T]): List[T] = stream.toList

  def collect[T](stream: Iterable[T]): List[T] = accumulate(stream)

  def collect[T](t: T): T = t

  implicit class StreamTestExt[T](stream: Iterable[T]) {
    def runSyncUnsafe() = collect[T](stream)
  }

  implicit class KyoTestExt[T](t: T) {
    def runSyncUnsafe() = collect[T](t)
  }
}

trait KyoProxySpec extends Spec with BeforeAndAfterAll {

  def accumulate[T](stream: Iterable[T]): List[T] = stream.toList

  def collect[T](stream: Iterable[T]): List[T] = accumulate(stream)

  def collect[T](t: T): T = t

  implicit class KyoAnyOps[T](t: T) {
    def runSyncUnsafe(): T = collect[T](t)
  }

  implicit class StreamTestExt[T](stream: Iterable[T]) {
    def runSyncUnsafe() = collect[T](stream)
  }

  implicit class KyoTestExt[T](t: T) {
    def runSyncUnsafe() = collect[T](t)
  }
}
