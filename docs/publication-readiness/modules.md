# Publication Readiness — Module and Migration Inventory

## Purpose

Covers every aggregate/intended-publication module defined by `build.sbt`'s
four module sets (`baseModules`, `sqlTestModules`, `dbModules`,
`bigdataModules`), its purpose, public Kyo surface, dependency scope,
service prerequisite, and current verification state, plus the exhaustive
classification of every residual ZIO reference discovered in Kyo-module
source. Follows the evidence record schema and severity rules in
`evidence.md`.

## S04 Boundary Classification

The retained module inventory verification-state claims are **not fresh** for this amendment. Their candidate and
tree references remain historical observations; their Homebrew OpenJDK 25.0.4
actual-sbt-JVM and prior checkout assumptions make runtime/clean-clone claims
`stale`. S04 records `jdk-vendor-version-mismatch`, `sbt-jvm-mismatch`, and
`clone-kind-mismatch`; any changed manifest, dependency, command, or checker
input also makes its affected claim stale. `S06-Temurin-normal-clone-TBD` is
the required replacement ID; no Temurin execution is claimed here.

## Candidate Binding

| Field | Value |
| --- | --- |
| `candidate_id` | `4d286f7d` |
| `candidate_tree` | `8924989ad8d3d676aff73c30ab93ad31878e7385` |
| `captured_at` | 2026-08-27 |
| Environment | Homebrew OpenJDK 25.0.4 (sbt banner; not Temurin), sbt 1.12.4, Scala 3.8.4, Kyo 1.0.0-RC6 |
| Scope | WU3 — module and residual-ZIO inventory |

The root aggregate project `quill` (`project in file(".")`) is not itself a
publishable module (`publishArtifact := false`); it only aggregates the
modules below and is excluded from the four module sets by design, so it is
not listed as a row.

## Module Inventory

Evidence ID: ER-010.

| module | module_set | purpose | public_kyo_surface | dependency_scope | service_prerequisite | verification_state (candidate `4d286f7d`) |
| --- | --- | --- | --- | --- | --- | --- |
| `quill-sql` | base | Core Quill SQL query engine: DSL, parsing, quotation, code generation. | Compiles against `kyo-core`, `kyo-prelude`, `kyo-data` `1.0.0-RC6`; context abstractions at this layer are effect-agnostic. | `quill-engine`/`quill-util` `4.8.5`, `kyo-core`/`kyo-prelude`/`kyo-data` `1.0.0-RC6`. | None (in-memory/DSL tests). | sqltest tier: 668/668 passed, 1 ignored (local run, not clean-checkout CI). |
| `quill-sql-tests` | sqltest | Heavy SQL-generation test suite, split from `quill-sql` for parallel compilation. | N/A — test-only module. | `quill-sql` (`compile->compile;test->test`). | None. | sqltest tier: 668/668 passed, 1 ignored (shared total with `quill-sql`; not broken out per module in local evidence). |
| `quill-jdbc` | db | Synchronous JDBC context implementations (non-Kyo). | None (pre-Kyo layer; consumed by `quill-jdbc-kyo`). | `quill-sql` (`compile->compile;test->test`). | Relational DB drivers per test suite (Postgres/MySQL/SQLServer/Oracle/SQLite/H2). | db tier: 535 run / 534 passed / 1 failed (pre-existing, unrelated `DistinctJdbcSpec` Oracle failure) / 13 ignored, aggregated across 3 module summaries (8+519+8); per-module breakdown is WU4/WU5 work. |
| `quill-doobie` | db | Doobie/cats-effect JDBC integration. | None (cats-effect layer, not Kyo). | `quill-jdbc` (`compile->compile;test->test`); `doobie-core`/`doobie-postgres` `1.0.0-RC12`. | PostgreSQL (`doobie-postgres` test dependency). | db tier: included in the same 535-test aggregate above; not yet broken out per module (WU4/WU5). |
| `quill-kyo` | db | Kyo effect-based context abstractions layered over `quill-sql`. | `kyo-core`/`kyo-prelude`/`kyo-data` `1.0.0-RC6` effect wrapper API. | `quill-sql` (`compile->compile;test->test`); `kyo-core`/`kyo-prelude`/`kyo-data`. | None beyond `quill-sql`. | db tier: included in the same 535-test aggregate above; not yet broken out per module (WU4/WU5). |
| `quill-jdbc-kyo` | db | Kyo effect JDBC context (`KyoJdbcContext`, `KyoJdbcUnderlyingContext`, `KyoJdbc`, `EffectfulQuill`), plus PostgreSQL JSON extensions. | `KyoJdbcContext`, `KyoJdbcUnderlyingContext`, `KyoJdbc` (Kyo `Layer`-based connection acquisition), `EffectfulQuill`. | `quill-kyo`, `quill-sql`, `quill-jdbc` (`compile->compile;test->test`); `zio-json` (JSON codec only, see Residual ZIO Reference Classification). | Relational DB drivers; PostgreSQL specifically for the JSON-extension test suite. | db tier: included in the same 535-test aggregate above; not yet broken out per module (WU4/WU5). |
| `quill-caliban` | db | GraphQL (Caliban) integration exposing Quill/Kyo queries. | Kyo-Caliban schema derivation via `kyo-caliban` `1.0.0-RC6`. | `quill-jdbc-kyo` (`compile->compile`); `caliban-quick` `3.1.2`, `kyo-caliban` `1.0.0-RC6`; `postgresql` `42.7.13` (test). | PostgreSQL (test dependency); Caliban GraphQL interpreter. | db tier: included in the same 535-test aggregate above; no ZIO references in main source (test-only, see Residual ZIO Reference Classification). |
| `quill-cassandra` | bigdata | Synchronous Cassandra context (non-Kyo). | None (pre-Kyo layer; consumed by `quill-cassandra-kyo`). | `quill-sql` (`compile->compile;test->test`); `java-driver-core` `4.19.3`. | Apache Cassandra (`java-driver-core`). | bigdata tier: 190/190 passed across 21 suites, 0 aborted (aggregate with `quill-cassandra-kyo`; not broken out per module). |
| `quill-cassandra-kyo` | bigdata | Kyo effect Cassandra context. | `kyo-core`/`kyo-prelude`/`kyo-data` `1.0.0-RC6` effect wrapper over `quill-cassandra`. | `quill-cassandra`, `quill-kyo` (`compile->compile;test->test`); `kyo-core`/`kyo-prelude`/`kyo-data`. | Apache Cassandra (`java-driver-core`). | bigdata tier: 190/190 passed across 21 suites, 0 aborted (aggregate with `quill-cassandra`; not broken out per module). |

All verification-state figures above are **local evidence only**, executed
2026-08-27 against candidate `4d286f7d` (re-measured 2026-08-27 with identical results; Homebrew OpenJDK 25.0.4 per the sbt banner, not Temurin, sbt 1.12.4,
Scala 3.8.4, Kyo 1.0.0-RC6). No clean-checkout CI result exists for this
candidate; per-module structured Tier 0-2 records with exact commands,
timings, and failure classification are WU4/WU5 deliverables, not this
inventory.

## Residual ZIO Reference Classification

Evidence ID: ER-011.

Every discovered residual ZIO dependency, import, symbol, comment, or
document reference in Kyo-module source is classified exactly once. This
table is bounded to `quill-kyo`, `quill-jdbc-kyo`, `quill-cassandra-kyo`,
and `quill-caliban` main/test source, per a repository-wide search
confirming these are the only Kyo-module files containing the string `zio`
(case-insensitive). Tracked historical `.md` reports and `.bak` files are
recorded separately in `provenance.md`'s Publication-Cleanliness Findings,
not re-classified line-by-line here.

| module | source | reference | category |
| --- | --- | --- | --- |
| `quill-jdbc-kyo` | `quill-jdbc-kyo/src/main/scala/io/getquill/jdbckyo/EffectfulQuill.scala:21` | Comment: "Kyo equivalent of jdbczio.QuillBaseContext." | compatibility naming |
| `quill-jdbc-kyo` | `quill-jdbc-kyo/src/main/scala/io/getquill/context/qkyo/KyoJdbcContext.scala:15` | Comment: "Kyo equivalent of ZioJdbcContext." | compatibility naming |
| `quill-jdbc-kyo` | `quill-jdbc-kyo/src/main/scala/io/getquill/context/qkyo/KyoJdbcContext.scala:27,64` | Comments comparing connection propagation to ZIO's `FiberRef`. | historical text |
| `quill-jdbc-kyo` | `quill-jdbc-kyo/src/main/scala/io/getquill/context/qkyo/KyoJdbcUnderlyingContext.scala:17` | Comment: "Kyo equivalent of ZioJdbcUnderlyingContext." | compatibility naming |
| `quill-jdbc-kyo` | `quill-jdbc-kyo/src/main/scala/io/getquill/context/KyoJdbc.scala:31,33,43` | Comments comparing Kyo `Layer` acquisition to ZIO's `ZLayer`/`ZIO.service`. | historical text |
| `quill-jdbc-kyo` | `quill-jdbc-kyo/src/main/scala/io/getquill/context/json/PostgresJsonExtensions.scala:8` | `import zio.json.{JsonEncoder, JsonDecoder}` — JSON codec dependency. | bridge/codec usage |
| `quill-caliban` | `quill-caliban/src/test/scala/io/getquill/CalibanIntegrationSpec.scala:15` | Comment: "Kyo effect type equivalent to ZIO's Task". | historical text |
| `quill-caliban` | `quill-caliban/src/test/scala/io/getquill/CalibanIntegrationNestedSpec.scala:16` | Comment: "Kyo effect type equivalent to ZIO's Task". | historical text |
| `quill-caliban` | `quill-caliban/src/test/scala/io/getquill/CalibanSpec.scala:28-29` | Comment explaining why ZIO's unsafe runner is used for interpreter execution. | historical text |
| `quill-caliban` | `quill-caliban/src/test/scala/io/getquill/CalibanSpec.scala:31` | `import zio.{Unsafe, Runtime}` — ZIO interop bridge for Caliban's ZIO-native interpreter. | bridge/codec usage |
| `quill-caliban` | `quill-caliban/src/test/scala/io/getquill/example/CalibanExample.scala:27` | Comment: "Kyo effect type equivalent to ZIO's Task". | historical text |
| `quill-caliban` | `quill-caliban/src/test/scala/io/getquill/example/CalibanExample.scala:101,106` | `import zio.{Unsafe, Runtime}` and `kyo.ZIOs.run(...)` — ZIO interop bridge. | bridge/codec usage |
| `quill-caliban` | `quill-caliban/src/test/scala/io/getquill/example/CalibanExampleNested.scala:98,103` | `import zio.{Unsafe, Runtime}` and `kyo.ZIOs.run(...)` — ZIO interop bridge. | bridge/codec usage |

**Classification outcome:** 5 bridge/codec usage, 6 historical text,
3 compatibility naming, **0 actionable migration debt**. The single direct
`import zio` in Kyo-module main source (`PostgresJsonExtensions.scala:8`)
is codec usage, not effect-migration debt, and is the explicit subject of
the deferred follow-up SDD change for the `zio-json` -> Kyo Schema/Json
codec migration (see `tasks.md`'s "Deferred Follow-up SDD Change"
section) — it is not actionable within this change.

## Verification

See the WU3 evidence block in `tasks.md` for the RED/GREEN structural
checker cycle (`aggregate module coverage` and
`residual ZIO reference classification` checks) and the manual
source-link readback confirming every path cited above exists at candidate
`4d286f7d`. Runtime scenario: `N/A` — inventory only; no application
runtime boundary applies to a structural/documentation inventory.
