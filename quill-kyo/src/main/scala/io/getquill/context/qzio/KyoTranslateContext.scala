package io.getquill.context.qkyo

import io.getquill.NamingStrategy
import io.getquill.context.{Context, ContextEffect}
import io.getquill.idiom.Idiom
import kyo.*
import io.getquill.context.ContextTranslateMacro

trait KyoTranslateContext[+Dialect <: io.getquill.idiom.Idiom, +Naming <: NamingStrategy]
    extends Context[Dialect, Naming]
    with ContextTranslateMacro[Dialect, Naming] {

  type Error
  type Environment

  override type TranslateResult[T] = T < (Abort[Error] & Env[Environment] & IO & Async)

  override def wrap[T](t: => T): TranslateResult[T] = t

  override def push[A, B](result: TranslateResult[A])(f: A => B): TranslateResult[B] =
    result.map(f)

  override def seq[A](list: List[TranslateResult[A]]): TranslateResult[List[A]] =
    list.foldRight(wrap(List.empty[A])) { (elem, acc) =>
      for {
        a  <- elem
        as <- acc
      } yield a :: as
    }
}
