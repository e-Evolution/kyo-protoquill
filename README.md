# Introduction

Kyo Quill is the [Kyo](https://github.com/getkyo/kyo)-native Scala 3 compile-time Language Integrated Queries library. Originally based on [ProtoQuill](https://github.com/zio/zio-protoquill), the project has fully migrated to **Kyo's algebraic effect system** for async/resource handling while preserving all Quill functionality.

For those migrating from ZIO Quill, the core Quill DSL (`quote`, `query`, `run`, `inline def`, etc.) remains identical. Only the effect layer changes: `ZIO[R, E, A]` becomes `A < (Abort[E] & Env[R] & Async)` and `ZLayer` becomes direct dependency passing or Kyo `Layer`.

Currently Supported:

- Basic Quotation, Querying, Lifting, and Types (Compile-Time and Dynamic)
- Inner/Outer, Left/Right joins
- Query.map/flatMap/concatMap/filter and other query constructs
- Insert, Update, Delete Actions (Compile-Time and Dynamic)
- Batch Insert, Batch Update, and Batch Delete Actions
- **Kyo** and Synchronous JDBC contexts
- SQL OnConflict Clauses
- Prepare Query (i.e. `context.prepare(query)`)
- Translate Query (i.e. `context.translate(query)`)
- Cassandra Contexts (using V4 drivers)
- Dynamic Query API
- **Caliban Integration via kyo-caliban** (native Kyo effect types in GraphQL resolvers)

Not Supported:

- Implicit class based extensions. See the [Extensions](#extensions) section below.

There are also quite a few features that Kyo Quill has:

- Scala Methods and Typeclasses Transforming Kyo Quill queries (see [Shareable Code](#shareable-code) and [Advanced Example](#advanced-example)).
- [Custom Parsing](#custom-parsing)
- [Co-Product Rows](#co-product-rows) (Highly experimental)
- [Caliban-Integration](#caliban-integration) (Deep integration with Caliban using native Kyo effects)

# Getting Started

The simplest way to get started with Kyo Quill is with the standard JDBC contexts.
These are synchronous so for a high-throughput system you will ultimately need to switch
to the Kyo-based contexts which leverage Kyo's `Async` and `Scope` effects.

Add the following to your SBT file:

```scala
val kyoVersion = "1.0-RC1"

libraryDependencies ++= Seq(
  // Synchronous JDBC Modules
  "io.getquill" %% "quill-jdbc" % "4.8.5",
  // Or Kyo Modules (with async/resource effects)
  "io.getquill" %% "quill-jdbc-kyo" % "4.8.5",
  // Or Cassandra
  "io.getquill" %% "quill-cassandra" % "4.8.5",
  // Or Cassandra + Kyo
  "io.getquill" %% "quill-cassandra-kyo" % "4.8.5",
  // Add for Caliban Integration (uses kyo-caliban internally)
  "io.getquill" %% "quill-caliban" % "4.8.5"
)
```

Assuming we are using Postgres, add the following `application.conf`:

```
testPostgresDB.dataSourceClassName=org.postgresql.ds.PGSimpleDataSource
testPostgresDB.dataSource.databaseName=<my-database>
testPostgresDB.dataSource.url=<my-jdbc-url>
```

Create a context and a case class representing your table:

```scala
import io.getquill._

object MyApp {
  case class Person(firstName: String, lastName: String, age: Int)

  // SnakeCase turns firstName -> first_name
  val ctx = new PostgresJdbcContext(SnakeCase, "ctx")
  import ctx._

  def main(args: Array[String]): Unit = {
    val named = "Joe"
    inline def somePeople = quote {
      query[Person].filter(p => p.firstName == lift(named))
    }
    val people: List[Person] = run(somePeople)
    println(people)
  }
}
```

## Using Kyo JDBC Context

For applications that need effect-based resource management, use `PostgresKyoJdbcContext`
(or the corresponding context for your database):

```scala
import io.getquill._
import kyo.*

case class Person(firstName: String, lastName: String, age: Int)

// Create a Kyo-based JDBC context with a DataSource
val ds: javax.sql.DataSource = JdbcContextConfig(LoadConfig("testPostgresDB")).dataSource
object ctx extends PostgresKyoJdbcContext(SnakeCase, ds)
import ctx._

// Queries return results directly (synchronous context)
val people: List[Person] = ctx.run(query[Person].filter(_.firstName == "Joe"))
```

### Kyo Effect Type Aliases

The `KyoJdbc` object provides convenient type aliases for Kyo-based JDBC operations:

```scala
import io.getquill.context.KyoJdbc._

// QIO[T] - Query IO with DataSource environment
type QIO[T] = T < (Abort[SQLException] & Env[DataSource] & kyo.Sync & Async)

// QCIO[T] - Query Connection IO with Connection environment
type QCIO[T] = T < (Abort[SQLException] & Env[Connection] & kyo.Sync & Async)
```

### ZIO to Kyo Migration Reference

| ZIO 2                | Kyo                               | Description                         |
| -------------------- | --------------------------------- | ----------------------------------- |
| `ZIO[R, E, A]`       | `A < (Abort[E] & Env[R] & Async)` | Effects with environment and errors |
| `ZIO.succeed(x)`     | `x` (pure value)                  | Success without effects             |
| `ZIO.fail(e)`        | `Abort.fail(e)`                   | Fail with typed error               |
| `ZIO.attempt(body)`  | `Abort.catching[Throwable](body)` | Suspend effects catching exceptions |
| `ZIO.environment[R]` | `Env.get[R]`                      | Get dependency                      |
| `ZLayer`             | Direct passing or `Layer[Out, S]` | Dependency layers                   |
| `ZStream[R, E, A]`   | `Stream[A, S]`                    | Effectful streams                   |
| `Scope.global`       | `Scope.run`                       | Resource scope                      |
| `ZIO.acquireRelease` | `Scope.acquireRelease`            | Acquire/Release                     |
| `FiberRef`           | `Local[T]`                        | Fiber-local state                   |
| `Runtime.unsafeRun`  | `KyoApp`                          | Effect execution                    |
| `zio.Task[A]`        | `A < (Abort[Throwable] & Async)`  | Task effect type                    |

# Tutorial

## Queries

Kyo Quill queries are built using inline quoted expressions.

```scala
// With just this import you can use quote, query, insert/update/delete and lazyLift
import io.getquill._

inline def people = quote {
  query[Person]
}
inline def joes = quote {
  people.filter(p => p.name == "Joe")
}

run(joes)
// SELECT p.name, p.age FROM Person p WHERE p.name = 'Joe'
```

> You _do not_ need to import a context in Kyo Quill to make a quotation, just `io.getquill._`. Contexts are only needed for lifting.

### Quotation is (Mostly) Optional

If all parts of a Query are `inline def`, quotation is not strictly necessary:

```scala
inline def people = query[Person]
inline def joes = people.filter(p => p.name == "Joe")

run(joes)
// SELECT p.name, p.age FROM Person p WHERE p.name = 'Joe'
```

However, if parts of the query are dynamic (i.e. not `inline def`) it is needed:

```scala
inline def people = quote {
  query[Person]
}
val joes = quote {
  people.filter(p => p.name == "Joe")
}

run(joes)
// Warning: Dynamic Query
```

### Quoted Operations

ProtoQuill supports Quill `query[T]` constructs including:

- Outer/Inner, Left/Right Join (both monadic and applicative)
- Map, FlatMap, ConcatMap
- Union, Union-All
- Distinct, Nested
- querySchema

Keep in mind that in Kyo Quill for these to generate compile-time queries, they need to be `inline def`.

### Batch Queries

Kyo Quill supports Insert/Update/Delete actions as well as their batch variations:

```scala
// batch queries with different entities
liftQuery(vips).foreach(v => query[Person].insertValue(Person(v.first + v.last, v.age)))

// batch queries with scalars
liftQuery(List(1,2,3)).foreach(i => query[Person].filter(p => p.id == i).update(_.age -> 123))

// batch queries with additional lifts
liftQuery(people).foreach(p => query[Person].filter(p => p.age > lift(123)).contains(p.age)).updateValue(p))

// batch queries with `returning` clauses
liftQuery(vips).foreach(v => query[Person].insertValue(Person(v.first + v.last, v.age)).returning(_.id))
```

### Metas

QueryMeta, SchemaMeta, InsertMeta, and UpdateMeta are supported:

```scala
// SchemaMeta
inline given SchemaMeta[Person] = schemaMeta("PersonTable", name -> "nameRow")

// Insert Meta
inline given InsertMeta[Person] = insertMeta(_.id)

// Update Meta
inline given UpdateMeta[Person] = updateMeta(_.id)
```

### Shareable Code

Since quotation of `inline def` code is optional, Quill expressions can share code with regular Scala constructs.

```scala
// case class Person(name: String, age: Int)
inline def onlyJoes(p: Person) = p.name == "Joe"

run( query[Person].filter(p => onlyJoes(p)) )
// SELECT p.name, p.age FROM Person p WHERE p.name = 'Joe'

val people: List[Person] = ...
val joes = people.filter(p => onlyJoes(p))
```

### Advanced Example

Since Quill expressions can share code with regular Scala constructs,
this can be generalized into higher-level constructs such as typeclasses.

```scala
// case class Person(name: String, age: Int)

trait Filterable[F[_]]:
  extension [A](inline x: F[A])
    inline def filter(inline f: A => Boolean): F[A]

extension [F[_]](inline people: F[Person])(using inline filterable: Filterable[F])
  inline def onlyJoes = people.filter(p => p.name == "Joe")

class ListFilterable extends Filterable[List]:
  extension [A](inline xs: List[A])
    inline def filter(inline f: A => Boolean): List[A] = xs.filter(f)

class QueryFilterable extends Filterable[List]:
  extension [A](inline xs: List[A])
    inline def filter(inline f: A => Boolean): List[A] = xs.filter(f)

run( query[Person].onlyJoes )

val people: List[Person] = ...
val joes = people.onlyJoes
```

## Lifting and Lazy Lifting

Since Quill-Quotations define blocks of compile-time-inspectable code, adding variables whose value is only known during runtime typically requires lifting:

```scala
// NOTE: Be sure to import a context first!
// val ctx = new MirrorSqlContext(PostgresDialect, Literal); import ctx._

val runtimeValue = somethingFromSomewhere()
inline def somePeople = quote {
  query[Person].filter(p => p.name == lift(runtimeValue))
}
val results: List[Person] = run(somePeople)
```

However, since Quotation in ProtoQuill is static, you can use `lazyLift` to lift a value without importing a context:

```scala
import io.getquill._
val name = ...
inline def q = quote { query[Person].filter(p => p.name == lift(name)) }

// Now we can use this quotation in multiple contexts
{
  val ctx = new PostgresJdbcContext(Literal, "ctx")
  val results = ctx.run(q)
}
{
  val ctx = new H2JdbcContext(Literal, "ctx")
  val results = ctx.run(q)
}
```

## Filtering Tables by Key/Values

One typical use-case that ProtoQuill handles well is filtering a query based on an arbitrary group of column/value pairs. This is typically done with Http-Based systems where URL-parameters `&key=value` are decoded as a map. In ProtoQuill, `filterByKeys` addresses this use-case.

```scala
val values: Map[String, String] = Map("firstName" -> "Joe", "age" -> "22")

val ctx = new MirrorContext(Literal, PostgresDialect)
import ctx._

inline def q = quote {
  query[Person].filterByKeys(values)
}
run(q)

// SELECT p.firstName, p.lastName, p.age
// FROM Person p
// WHERE
//   (p.firstName = ? OR ? IS NULL) AND
//   (p.lastName = ? OR ? IS NULL) AND
//   (p.age = ? OR ? IS NULL) AND
//   true
```

## Getting SQL of the Last Executed Query (Kyo)

In Kyo contexts, you can get the SQL of the last executed query using `Local` effect-based tracking:

```scala
import io.getquill._
import io.getquill.context.KyoQuillLog
import kyo.*

// The KyoQuillLog uses kyo.Local to track execution info
val lastQuery: Option[String] < Local[Option[String]] = getLastExecutedQuery()
val lastInfo: Option[ExecutionInfo] < Local[Option[ExecutionInfo]] = getLastExecutionInfo()
```

## Co-Product Rows

Co-Products are supported using Enums and sealed traits:

1. Create the Coproduct:

   ```scala
   object StaticEnumExample {
     enum Shape(val id: Int):
       case Square(override val id: Int, width: Int, height: Int) extends Shape(id)
       case Circle(override val id: Int, radius: Int) extends Shape(id)
   }
   ```

2. Create a row-typer:

   ```scala
   given RowTyper[Shape] with
     def apply(row: Row) =
       row.apply[String]("type") match
         case "square" => classTag[Shape.Square]
         case "circle" => classTag[Shape.Circle]
   ```

3. Create and run your query:
   ```scala
   inline def q = quote { query[Shape].filter(s => s.id == 18) }
   val result: List[Shape] = ctx.run(q)
   ```

## Custom Parsing

The Parser API allows you to define custom parsing for user-defined logic:

1. Define your business logic:

   ```scala
   object MyBusinessLogic:
     extension (i: Int)
       def **(exponent: Int) = Math.pow(i, exponent)
   ```

2. Define a parser (in a separate compilation unit):

   ```scala
   import io.getquill.parser._
   import io.getquill.ast.{ Ast, Infix }
   import io.getquill.quat.Quat

   case class CustomOperationsParser(root: Parser[Ast] = Parser.empty)(override implicit val qctx: Quotes) extends Parser.Clause[Ast] {
     import quotes.reflect._
     import CustomOps._
     def reparent(newRoot: Parser[Ast]) = this.copy(root = newRoot)
     def delegate: PartialFunction[Expr[_], Ast] =
       case '{ ($i: Int)**($j: Int) } =>
         Infix(List("power(", " ,", ")"), List(astParse(i), astParse(j)), true, Quat.Value)
   }

   object CustomParser extends ParserLibrary:
     import Parser._
     override def operationsParser(using qctx: Quotes) =
       Series.of(new OperationsParser, new CustomOperationsParser)
   ```

3. Use it in your application code:
   ```scala
   given myParser: CustomParser.type = CustomParser
   import MyBusinessLogic._
   case class Person(name: String, age: Int)
   inline def q = quote { query[Person].map(p => p.age ** 2) }
   // SELECT power(p.age ,2) FROM Person p
   ```

## Migration Notes

- Most Scala2-Quill code should either work in Kyo Quill directly or require minimal changes.
  However, since Kyo Quill compile-time queries rely on `inline def`, these queries must be changed from:

  ```scala
  val people = quote { query[Person] }
  val joes = quote { people.filter(p => p.name == "Joe") }
  run(joes) // Dynamic Query Detected
  ```

  To:

  ```scala
  inline def people = quote { query[Person] }
  inline def joes = quote { people.filter(p => p.name == "Joe") }
  run(joes) // SELECT p.name, p.age FROM Person p WHERE p.name = 'Joe'
  ```

- If migrating from ZIO Quill:
  - Replace `"io.getquill" %% "quill-jdbc-zio"` with `"io.getquill" %% "quill-jdbc-kyo"`
  - Replace `"io.getquill" %% "quill-cassandra-zio"` with `"io.getquill" %% "quill-cassandra-kyo"`
  - Replace `import zio._` with `import kyo.*`
  - Replace `ZIO.attempt { ... }` with `Abort.catching[Throwable] { ... }`
  - Replace `ZIO.succeed(x)` with just `x` (pure values are computations in Kyo)
  - Replace `ZIO.fail(e)` with `Abort.fail(e)`
  - Replace `zio.Task[A]` with `A < (Abort[Throwable] & Async)` or define a type alias
  - Replace `.provideLayer(layer)` with direct DataSource passing to context constructor
  - ZIO `FiberRef` becomes Kyo `Local[T]`

# Extensions

Kyo Quill supports standard Dotty extensions. An inline extension will yield a compile-time query.

```scala
case class Person(first: String, last: String)

extension (inline p: Person) // make sure this variable is `inline`
  inline def fullName = p.first + " " + p.last

run( query[Person].map(p => p.fullName) )
// SELECT p.name || ' ' || p.age FROM Person p
```

# Caliban Integration

Caliban integration is provided by the `quill-caliban` module using **native Kyo effects** via `kyo-caliban`. This makes it easy to setup a Caliban GraphQL endpoint where you can filter by any column and include/exclude any column, with exclusions pushed down to the database.

## Setup

```scala
// Import Quill
import io.getquill._

// Import the Caliban integration
import io.getquill.CalibanIntegration._

// Import Kyo effects and kyo-caliban Schema instances
import kyo.*
import kyo.given  // Provides Schema[R, A < S] for Caliban auto-derivation

// Kyo effect type equivalent to ZIO's Task
type KyoTask[A] = A < (Abort[Throwable] & Async)
```

## Full Example

```scala
import io.getquill._
import io.getquill.CalibanIntegration._
import caliban.graphQL
import caliban.RootResolver
import caliban.execution.Field
import caliban.schema.Schema.auto._
import caliban.schema.ArgBuilder.auto._
import kyo.*
import kyo.given

// Given some simple schema
case class PersonT(id: Int, first: String, last: String, age: Int)
case class AddressT(ownerId: Int, street: String)
case class PersonAddress(id: Int, first: String, last: String, age: Int, street: Option[String])

// Create a Kyo JDBC context
lazy val ds = JdbcContextConfig(LoadConfig("testPostgresDB")).dataSource
object Ctx extends PostgresKyoJdbcContext(Literal, ds)
import Ctx._

// Create a query and add .filterColumns and .filterByKeys to the end
inline def peopleAndAddresses(inline columns: List[String], inline filters: Map[String, String]) =
  quote {
    query[PersonT].leftJoin(query[AddressT]).on((p, a) => p.id == a.ownerId)
      .map((p, a) => PersonAddress(p.id, p.first, p.last, p.age, a.map(_.street)))
      .filterColumns(columns)
      .filterByKeys(filters)
  }

// Create a data-service using Kyo effects
object DataService {
  def personAddress(columns: List[String], filters: Map[String, String]): KyoTask[List[PersonAddress]] =
    Abort.catching[Throwable] {
      Ctx.run(peopleAndAddresses(columns, filters))
    }
}

// Kyo effect type for resolvers - kyo-caliban Schema instances
// (imported via `import kyo.given`) handle the Kyo-to-ZIO bridging automatically
case class Queries(
  personAddress: Field => (ProductArgs[PersonAddress] => KyoTask[List[PersonAddress]])
)

// Create your Caliban endpoint
val api = graphQL(
  RootResolver(
    Queries(
      personAddress =>
        (productArgs =>
          DataService.personAddress(
            quillColumns(personAddress),
            productArgs.keyValues
          )
        )
    )
  )
)

// Execute a query for testing (Caliban uses ZIO internally, so test execution uses ZIO runtime)
val calibanQuery =
  """
  {
    personAddress(first: "Joe") {
      id
      last
      street
    }
  }"""

import zio.{Unsafe, Runtime}
val output = Unsafe.unsafe { implicit unsafe =>
  Runtime.default.unsafe.run(
    api.interpreter.flatMap(_.execute(calibanQuery))
  ).getOrThrowFiberFailure()
}
output.data.toString
// {"personAddress":[{"id":1,"last":"A","street":"123 St"}]}
```

### Key Differences from ZIO-Caliban

| ZIO Approach                               | Kyo Approach                                               |
| ------------------------------------------ | ---------------------------------------------------------- |
| `zio.Task[A]` return type                  | `A < (Abort[Throwable] & Async)` return type               |
| `zio.ZIO.attempt { ... }`                  | `Abort.catching[Throwable] { ... }`                        |
| `import caliban.schema.Schema.auto._` only | `import caliban.schema.Schema.auto._` + `import kyo.given` |
| `.provideLayer(dataSourceLayer)`           | Direct DataSource via context constructor                  |
| `zip` for parallel composition             | `for/yield` Kyo composition                                |

The `import kyo.given` is essential - it brings in `caliban.schema.Schema` instances for Kyo effect types (`A < S`), which automatically bridge Kyo effects to ZIO for Caliban's internal execution engine.

### Running a Caliban Server

```scala
import caliban.quick._
import kyo.*

object CalibanServer {
  def main(args: Array[String]): Unit = {
    import zio.{Unsafe, Runtime}
    // Caliban's server runtime uses ZIO internally
    // kyo.ZIOs bridges Kyo effects to ZIO for the server
    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.run(
        for {
          _ <- kyo.ZIOs.run(DataService.resetDatabase())
          _ <- api.runServer(
            port = 8088,
            apiPath = "/api/graphql",
            graphiqlPath = Some("/graphiql")
          )
        } yield ()
      ).getOrThrowFiberFailure()
    }
  }
}
```

### How Caliban Column Filtering Works

When `.filterColumns(columns)` and `.filterByKeys(filters)` are called, the query:

```sql
SELECT p.id, p.first, p.last, p.age, a.street
FROM Person p LEFT JOIN Address a ON p.id = a.ownerId
```

Becomes:

```sql
SELECT
  CASE WHEN ? THEN p.id ELSE null END,
  CASE WHEN ? THEN p.first ELSE null END,
  CASE WHEN ? THEN p.last ELSE null END,
  CASE WHEN ? THEN p.age ELSE null END,
  CASE WHEN ? THEN a.street ELSE null END
FROM Person p LEFT JOIN Address a ON p.id = a.ownerId
WHERE
  (cast(CASE WHEN ? THEN p.id ELSE null END as VARCHAR) = ? OR ? IS NULL)
  AND (CASE WHEN ? THEN p.first ELSE null END = ? OR ? IS NULL)
  AND (CASE WHEN ? THEN p.last ELSE null END = ? OR ? IS NULL)
  AND (cast(CASE WHEN ? THEN p.age ELSE null END as VARCHAR) = ? OR ? IS NULL)
  AND (CASE WHEN ? THEN a.street ELSE null END = ? OR ? IS NULL)
```

The SQL optimizer can see into these `CASE WHEN` clauses when the condition is a static variable, allowing the database to skip scanning columns and tables that are not needed. This means filter-pushdown and predicate-pushdown all the way from the GraphQL frontend down to the DB.

# Module Architecture

| Module                | Description                                                       | Key Dependencies                      |
| --------------------- | ----------------------------------------------------------------- | ------------------------------------- |
| `quill-sql`           | Core SQL engine, macros, compile-time query generation            | `kyo-core`, `kyo-prelude`, `kyo-data` |
| `quill-jdbc`          | Synchronous JDBC contexts (Postgres, MySQL, H2, SQLite, etc.)     | `quill-sql`                           |
| `quill-kyo`           | Kyo effect type definitions (`KyoContext`, `KyoTranslateContext`) | `quill-sql`, `kyo-core`               |
| `quill-jdbc-kyo`      | Kyo JDBC contexts with effect-based resource management           | `quill-kyo`, `quill-jdbc`             |
| `quill-cassandra`     | Cassandra base context                                            | `quill-sql`, `java-driver-core`       |
| `quill-cassandra-kyo` | Cassandra with Kyo effects                                        | `quill-cassandra`, `quill-kyo`        |
| `quill-doobie`        | Doobie integration                                                | `quill-jdbc`, `doobie-core`           |
| `quill-caliban`       | Caliban GraphQL integration with native Kyo effects               | `quill-jdbc-kyo`, `kyo-caliban`       |

# Building and Testing

```bash
# Compile all modules
sbt compile

# Run SQL unit tests (no database required)
sbt "quill-sql/test; quill-sql-tests/test"

# Run JDBC tests (requires PostgreSQL)
export POSTGRES_HOST=localhost
export POSTGRES_PORT=5432
export POSTGRES_PASSWORD=""
sbt "quill-jdbc/testOnly *postgres*"

# Run H2 in-memory tests
sbt "quill-jdbc/testOnly *h2*"

# Run Doobie tests (requires PostgreSQL)
sbt "quill-doobie/test"

# Run Caliban integration tests (requires PostgreSQL)
sbt "quill-caliban/test"
```

# Requirements

- Scala 3.8.1+
- JDK 17+ (JDK 21 recommended)
- Kyo 1.0-RC1
