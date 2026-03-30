package io.getquill.jdbckyo

import io.getquill.NamingStrategy
import io.getquill.context.sql.idiom.SqlIdiom
import kyo.*
import kyo.Stream

trait QuillBaseContext[+Dialect <: SqlIdiom, +Naming <: NamingStrategy] {

  type Runner = Unit
  type TranslateRunner = Unit
  type Session = java.sql.Connection

  def ds: javax.sql.DataSource

  private[getquill] val currentConnection: Local[Option[java.sql.Connection]] = Local.init(None)
}
