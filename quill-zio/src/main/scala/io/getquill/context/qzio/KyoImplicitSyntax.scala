package io.getquill.context.qzio

import kyo.*
import kyo.Stream

object KyoImplicitSyntax {
  final case class Implicit[R](env: R)

  implicit final class ImplicitSyntaxOps[R, E, A](private val self: A < (Abort[E] & Async))
      extends AnyVal {
    def implicitly(implicit r: Implicit[R]): A < (Abort[E] & Async) = self
  }

  implicit final class StreamImplicitSyntaxOps[R, E, A](
      private val self: Stream[A, Abort[E] & Async]
  ) extends AnyVal {
    def implicitly(implicit r: Implicit[R]): Stream[A, Abort[E] & Async] = self
  }
}
