package io.getquill.context.json

import io.getquill.context.jdbc.{ Decoders, Encoders, JdbcContextTypes }

import java.sql.Types
import scala.reflect.{ ClassTag, classTag }
import io.getquill.{ JsonValue, JsonbValue }
import zio.json.{JsonEncoder, JsonDecoder}

trait PostgresJsonExtensions extends Encoders with Decoders {
  this: JdbcContextTypes[_, _] =>

  implicit def jsonEntityEncoder[T: JsonEncoder: ClassTag]: Encoder[JsonValue[T]] =
    encoder(Types.OTHER, (index, value: JsonValue[T], row) =>
      row.setObject(index, value.value, Types.OTHER)
    )

  implicit def jsonEntityDecoder[T: JsonDecoder: ClassTag]: Decoder[JsonValue[T]] =
    decoder((index, row, session) =>
      JsonValue(row.getObject(index).asInstanceOf[T])
    )

  implicit def jsonbEntityEncoder[T: JsonEncoder: ClassTag]: Encoder[JsonbValue[T]] =
    encoder(Types.OTHER, (index, value: JsonbValue[T], row) =>
      row.setObject(index, value.value, Types.OTHER)
    )

  implicit def jsonbEntityDecoder[T: JsonDecoder: ClassTag]: Decoder[JsonbValue[T]] =
    decoder((index, row, session) =>
      JsonbValue(row.getObject(index).asInstanceOf[T])
    )

  implicit def jsonAstEncoder: Encoder[JsonValue[String]] =
    encoder(Types.OTHER, (index, value: JsonValue[String], row) =>
      row.setString(index, value.value)
    )

  implicit def jsonAstDecoder: Decoder[JsonValue[String]] =
    decoder((index, row, session) =>
      JsonValue(row.getString(index))
    )

  implicit def jsonbAstEncoder: Encoder[JsonbValue[String]] =
    encoder(Types.OTHER, (index, value: JsonbValue[String], row) =>
      row.setString(index, value.value)
    )

  implicit def jsonbAstDecoder: Decoder[JsonbValue[String]] =
    decoder((index, row, session) =>
      JsonbValue(row.getString(index))
    )

  def astEncoder[Wrapper](valueToString: Wrapper => String, jsonType: String): Encoder[Wrapper] =
    encoder(Types.OTHER, (index, value: Wrapper, row) =>
      row.setString(index, valueToString(value))
    )

  def astDecoder[Wrapper](valueFromString: String => Wrapper): Decoder[Wrapper] =
    decoder((index, row, session) =>
      valueFromString(row.getString(index))
    )

  def entityEncoder[JsValue, Wrapper](unwrap: Wrapper => JsValue)(jsonType: String, jsonEncoder: JsonEncoder[JsValue]): Encoder[Wrapper] =
    encoder(Types.OTHER, (index, value: Wrapper, row) =>
      row.setString(index, jsonEncoder.encodeJson(unwrap(value)).toString)
    )

  def entityDecoder[JsValue: ClassTag, Wrapper](wrap: JsValue => Wrapper)(jsonType: String, jsonDecoder: JsonDecoder[JsValue]): Decoder[Wrapper] =
    decoder((index, row, session) =>
      jsonDecoder.decodeJson(row.getString(index)).fold(
        err => throw new RuntimeException(s"Failed to decode JSON: $err"),
        wrap
      )
    )
}
