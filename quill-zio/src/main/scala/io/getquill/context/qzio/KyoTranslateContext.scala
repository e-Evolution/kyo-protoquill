package io.getquill.context.qzio

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

  override type TranslateResult[T] = T < (Abort[Error] & Async)

  override def wrap[T](t: => T): TranslateResult[T] = t

  override def push[A, B](result: TranslateResult[A])(f: A => B): TranslateResult[B] =
    result.map(f)

  override def seq[A](list: List[TranslateResult[A]]): TranslateResult[List[A]] =
    Async.collectAll(list).map(_.toList)
}
