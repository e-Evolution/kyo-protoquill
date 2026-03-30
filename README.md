# Kyo ProtoQuill

This project is a migration of the original `quill-jdbc-zio` and `quill-cassandra-zio` modules from ZIO to the Kyo effect system (https://github.com/getkyo/kyo). The goal is to replace ZIO-based async/resource handling with Kyo's effect polymorphism while preserving the same Quill API.

## What was migrated

- `quill-jdbc-zio` → now provides `io.getquill.jdbckyo` package
- `quill-cassandra-zio` → now provides `io.getquill.CassandraKyoContext` and related utilities
- All context classes (`PostgresKyoJdbcContext`, `MysqlKyoJdbcContext`, etc.) now use Kyo effects instead of ZIO
- Updated build to Scala 3.8.2
- Dependencies changed from `zio`, `zio-streams`, `zio-json` to `io.getkyo %% kyo-core`, `kyo-prelude`, `kyo-data`, `kyo-zio` (version 1.0‑RC1)
- `zio-json` retained as a `provided` dependency for JSON handling in PostgreSQL (per user request)

## Key changes in source code

### build.sbt
```scala
scalaVersion := "3.8.2"
val kyoVersion = "1.0-RC1"
libraryDependencies ++= Seq(
  "io.getkyo" %% "kyo-core"   % kyoVersion,
  "io.getkyo" %% "kyo-prelude" % kyoVersion,
  "io.getkyo" %% "kyo-data"   % kyoVersion,
  "io.getkyo" %% "kyo-zio"    % kyoVersion
)
// zio-json kept as provided for JSON codecs
libraryDependencies += "dev.zio" %% "zio-json" % "0.6.2" % "provided"
```

### Context instantiation (example)
Before (ZIO):
```scala
val ctx = new PostgresJdbcContext(SnakeCase, ds) with ZioQuillContext[PostgresDialect, SnakeCase]
```

After (Kyo):
```scala
val ctx = new PostgresKyoJdbcContext(SnakeCase, ds)
```

### Effect types
All methods that previously returned `ZIO[R, E, A]` now return `A < (Abort[E] & R & ...)` where the required effects are listed explicitly (e.g., `Sync`, `Scope`, `Async`, `Env[R]`).  

Example:
```scala
// Before
def executeAction(cql: String, prepare: Prepare = identityPrepare)(info: ExecutionInfo, dc: Runner): ZIO[Any, Throwable, Unit] = ...

// After
def executeAction(cql: String, prepare: Prepare = identityPrepare)(info: ExecutionInfo, dc: Runner): Unit < (Abort[Throwable] & Env[CassandraKyoSession] & kyo.IO & Async) = ...
```

### JSON handling (Postgres)
The `PostgresJsonExtensions.scala` file was updated to use `zio.json` directly (since the user indicated to keep using zio‑json). If you prefer a Kyo‑native JSON library (e.g., bilby or circe), replace the imports and implementations accordingly.

## Usage

Add the module to your build:
```scala
libraryDependencies += "io.getquill" %% "quill-jdbc-kyo" % "0.1.0" // or your published version
```
or depend on the project directly.

Create a context as usual:
```scala
import io.getquill.jdbckyo._
import io.getquill._

val ds = // your javax.sql.DataSource
val ctx = new PostgresKyoJdbcContext(Literal, ds)
import ctx._

// Run a query (returns a Kyo effect)
val people: List[Person] < (Abort[Throwable] & Env[ctx.type] & kyo.IO & Async) = 
  query[Person].filter(p => p.age > 18)

// To run the effect you need a Kyo runtime:
import kyo._
val result: Either[Throwable, List[Person]] = 
  people.map(_.right).run // or use .runUntried for sync, .runAsync for Future, etc.
```

See the test sources under `quill-jdbc-zio/src/test/scala` and `quill-cassandra-zio/src/test/scala` for examples of how to run effects with the Kyo runtime.

## Running the tests

The test suite has been adapted to use Kyo instead of ZIO. To run:

```bash
# Increase heap if needed (the test suite can be memory intensive)
sbt -J-Xmx4g test
```

Or run a specific module:
```bash
sbt -J-Xmx4g "project quill-jdbc-zio" test
sbt -J-Xmx4g "project quill-cassandra-zio" test
```

Note: The Cassandra tests require a running Cassandra instance on localhost:9042. If you don't have one, those tests will be skipped or fail with a connection error.

## Documentation and examples

All example applications and documentation that previously referenced ZIO have been updated to use Kyo. Look in:
- `quill-jdbc-zio/src/example/`
- `quill-cassandra-zio/src/example/`
- Any `README.md` or `MIGRATION.md` files within the modules.

If you find any leftover ZIO references, please report them so they can be updated.

## License

This project is licensed under the Apache License 2.0 – see the LICENSE file for details.

---
*Migration completed: [date]*