package io.getquill

import io.getquill.context.qzio.KyoImplicitSyntax.*
import org.scalatest.BeforeAndAfterAll
import kyo.*
import kyo.stream.Stream

import java.sql.Connection
import javax.sql.DataSource

object KyoSpec {
  def runLayerUnsafe[T](layer: Env.Layer[Any, T]): T =
    ???
}

trait KyoSpec extends Spec with BeforeAndAfterAll {

  def accumulate[T](stream: Stream[T, Any]): List[T] < Any =
    Stream.runCollect(stream)

  def collect[T](stream: Stream[T, Any]): List[T] =
    ???

  def collect[T](kyo: T < Any): T =
    ???

  implicit class StreamTestExt[T](stream: Stream[T, Any]) {
    def runSyncUnsafe() = collect[T](stream)
  }

  implicit class KyoTestExt[T](kyo: T < Any) {
    def runSyncUnsafe() = collect[T](kyo)
  }
}

trait KyoProxySpec extends Spec with BeforeAndAfterAll {

  def accumulateDS[T](stream: Stream[T, (Abort[Throwable] & Env[DataSource] & S)]): List[T] < (Abort[Throwable] & S) =
    Stream.runCollect(stream)

  def accumulate[T](stream: Stream[T, (Abort[Throwable] & Env[Connection] & S)]): List[T] < (Abort[Throwable] & S) =
    Stream.runCollect(stream)

  def collect[T](stream: Stream[T, (Abort[Throwable] & Env[DataSource] & S)])(implicit runtime: KyoImplicitSyntax.Implicit[DataSource]): List[T] =
    ???

  def collect[T](kyo: T < (Abort[Throwable] & Env[DataSource] & S))(implicit runtime: KyoImplicitSyntax.Implicit[DataSource]): T =
    ???

  implicit class KyoAnyOps[T](kyo: T < Any) {
    def runSyncUnsafe(): T = ???
  }

  implicit class StreamTestExt[T](stream: Stream[T, (Abort[Throwable] & Env[DataSource] & S)])(implicit runtime: KyoImplicitSyntax.Implicit[DataSource]) {
    def runSyncUnsafe() = collect[T](stream)
  }

  implicit class KyoTestExt[T](kyo: T < (Abort[Throwable] & Env[DataSource] & S))(implicit runtime: KyoImplicitSyntax.Implicit[DataSource]) {
    def runSyncUnsafe() = collect[T](kyo)
  }
}
