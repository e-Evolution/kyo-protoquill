> **Historical Document — Superseded.** This document describes the initial
> migration plan completed in March 2026, when this project targeted Kyo
> `1.0-RC1`, Scala `3.8.1`, and JDK `17+`. The project has since moved to
> Kyo `1.0.0-RC6`, Scala `3.8.4`, and JDK `25+`; every figure below is
> superseded and must not be treated as the project's current state. For
> current, measured evidence see `docs/publication-readiness/verification.md`
> and `docs/publication-readiness/modules.md`. This file is retained for
> historical reference only and is not an active planning document.

# Migration Plan: ZIO 2 → Kyo 1.0-RC1

## Executive Summary

**Project:** kyo-protoquill (forked from zio/zio-protoquill)
**Goal:** Complete migration of ZIO 2.x effect system to Kyo 1.0-RC1
**Branch:** `master`
**Started:** 2026-03-18
**Completed:** 2026-03-30
**Status:** DONE — All 8 modules migrated, compiled, and tested (1,171 tests passing)

---

## 1. Project Analysis

### 1.1 Module Structure

| Module                | Description        | Original ZIO Deps                                            | Migration Status |
| --------------------- | ------------------ | ------------------------------------------------------------ | ---------------- |
| `quill-sql`           | Core SQL engine    | `zio` (via quill-engine)                                     | Done             |
| `quill-sql-tests`     | SQL tests          | `quill-sql`                                                  | Done             |
| `quill-jdbc`          | JDBC base context  | —                                                            | Done             |
| `quill-doobie`        | Doobie integration | —                                                            | Done             |
| `quill-kyo`           | Kyo context traits | `zio`, `zio-streams` → `kyo-core`, `kyo-prelude`, `kyo-data` | Done             |
| `quill-jdbc-kyo`      | JDBC + Kyo         | `quill-kyo`, `zio-json` → `kyo-core`                         | Done             |
| `quill-cassandra`     | Cassandra engine   | —                                                            | Done             |
| `quill-cassandra-kyo` | Cassandra + Kyo    | `quill-cassandra`, `quill-kyo` → `kyo-core`                  | Done             |
| `quill-caliban`       | GraphQL (Caliban)  | — → `kyo-caliban`                                            | Done             |

### 1.2 API Mapping: ZIO → Kyo

| ZIO 2                | Kyo                               | Description                         |
| -------------------- | --------------------------------- | ----------------------------------- |
| `ZIO[R, E, A]`       | `A < (Abort[E] & Env[R] & Async)` | Effects with environment and errors |
| `ZIO.succeed(x)`     | `x` (pure value, `T` ≡ `T < Any`) | Success without effects             |
| `ZIO.fail(e)`        | `Abort.fail[E](e)`                | Fail with typed error               |
| `ZIO.attempt(body)`  | `Abort.catching[Throwable](body)` | Suspend effects catching exceptions |
| `ZIO.environment[R]` | `Env.get[R]`                      | Get dependency                      |
| `ZLayer`             | Direct passing or `Layer[Out, S]` | Dependency layers                   |
| `ZStream[R, E, A]`   | `Stream[A, S]`                    | Effectful streams                   |
| `Scope.global`       | `Scope.run`                       | Resource scope                      |
| `ZIO.scoped`         | `Scope.run`                       | Resources with cleanup              |
| `ZIO.acquireRelease` | `Scope.acquireRelease`            | Acquire/Release                     |
| `ZIO.blocking`       | `Sync.defer`                      | Blocking operations                 |
| `FiberRef`           | `Local[T]`                        | Fiber-local state                   |
| `Runtime.unsafeRun`  | `KyoApp`                          | Effect execution                    |
| `ZIO.collectAll`     | `Async.collectAll`                | Parallel collection                 |
| `ZIO.foreach`        | `Async.collectAll`                | Parallel iteration                  |
| `ZIO.zip`            | `for/yield` or `Async.zip`        | Parallel composition                |
| `ZIO.race`           | `Async.race`                      | Race effects                        |
| `zio.Task[A]`        | `A < (Abort[Throwable] & Async)`  | Task effect type                    |

### 1.3 Dependencies Changed in build.sbt

```scala
// REMOVED:
"dev.zio" %% "zio" % zioVersion
"dev.zio" %% "zio-streams" % zioVersion

// ADDED:
"io.getkyo" %% "kyo-core" % "1.0-RC1"
"io.getkyo" %% "kyo-prelude" % "1.0-RC1"
"io.getkyo" %% "kyo-data" % "1.0-RC1"
"io.getkyo" %% "kyo-caliban" % "1.0-RC1"  // for quill-caliban

// RETAINED (data dependency, not effects):
"dev.zio" %% "zio-json" % "0.8.0"  // PostgreSQL JSON extensions
```

---

## 2. Migration Strategy by Module

### Migration Order (dependencies first):

```
1. quill-sql        — No ZIO deps, added kyo-core/prelude/data
2. quill-jdbc       — No ZIO deps
3. quill-kyo        — Core Kyo context traits (KyoContext, KyoTranslateContext, KyoImplicitSyntax)
4. quill-jdbc-kyo   — JDBC + Kyo (KyoJdbc, KyoQuillLog, Quill trait, KyoPrepareContext)
5. quill-cassandra-kyo — Cassandra + Kyo (CassandraKyoContext, CassandraKyoSession)
6. quill-cassandra  — No ZIO deps
7. quill-caliban    — GraphQL with kyo-caliban native API
8. quill-doobie     — No ZIO deps (fixed Scala 3 syntax issues)
```

### 2.1 Module `quill-kyo`

**Files modified:**

- `KyoContext.scala` — Package renamed to `io.getquill.context.qkyo`
- `KyoTranslateContext.scala` — Package renamed
- `KyoImplicitSyntax.scala` — Package renamed

**Key change:** Package `io.getquill.context.kyo` renamed to `io.getquill.context.qkyo` to avoid shadowing the `kyo` library (same pattern as original `qzio` avoiding `zio` shadowing).

### 2.2 Module `quill-jdbc-kyo`

**Files modified:**

- `KyoJdbc.scala` — FQN references updated from `qzio` to `qkyo`
- `KyoQuillLog.scala` — Uses `kyo.Local` for SQL logging
- `KyoPrepareContext.scala` — Package renamed to `qkyo`
- `ResultSetIterator.scala` — Package renamed to `qkyo`
- `Quill.scala` — Added 10 `inline def run` overloads with `@targetName`

### 2.3 Module `quill-cassandra-kyo`

**Files modified/deleted:**

- `CassandraKyoContext.scala` — Kept (uses `kyo.*`)
- `CassandraKyoSession.scala` — Kept (uses `kyo.*`)
- `cassandrarkyo/Quill.scala` — Kept (uses `kyo.*`)
- `CassandraZioContext.scala` — DELETED (duplicate)
- `CassandraZioSession.scala` — DELETED (duplicate)
- `cassandrazio/Quill.scala` — DELETED (duplicate)

### 2.4 Module `quill-caliban`

**Files rewritten to use native kyo-caliban:**

- `CalibanSpec.scala` — Test base trait with `PostgresKyoJdbcContext`
- `CalibanIntegrationSpec.scala` — Flat schema tests with Kyo resolvers
- `CalibanIntegrationNestedSpec.scala` — Nested schema tests with Kyo resolvers
- `CalibanExample.scala` — Standalone example with `kyo.ZIOs.run` bridge
- `CalibanExampleNested.scala` — Nested example

**Key patterns:**

- Resolver types: `A < (Abort[Throwable] & Async)` instead of `zio.Task[A]`
- DAO methods: `Abort.catching[Throwable] { ctx.run(...) }`
- Schema derivation: `import kyo.given` provides `Schema[R, A < S]` instances
- Test execution: `zio.Unsafe.unsafe { Runtime.default.unsafe.run(...) }` (same as kyo-caliban upstream)

### 2.5 Module `quill-doobie`

**Files fixed for Scala 3 compatibility:**

- `PeopleDoobieReturningSpec.scala` — `Transactor.after < Local(...)` → `Transactor.after.set(...)`
- `PostgresDoobieContextSuite.scala` — Same fix

---

## 3. Execution Results

### Phase 1: Preparation — COMPLETED

- [x] Source code analysis
- [x] ZIO dependency identification
- [x] API mapping
- [x] Plan creation

### Phase 2: Core Migration — COMPLETED

- [x] Modify `build.sbt` — remove ZIO core, add Kyo
- [x] Migrate `quill-kyo` (3 files, package rename)
- [x] Migrate `quill-jdbc-kyo` (5 files, add `inline def run`)
- [x] Migrate `quill-cassandra-kyo` (delete 3 duplicates)
- [x] Migrate `quill-caliban` tests (5 files rewritten to kyo-caliban)
- [x] Fix `quill-doobie` Scala 3 syntax (2 files)
- [x] Compile and verify — 0 errors

### Phase 3: Testing — COMPLETED

- [x] Compile all test sources — 0 errors (9 modules)
- [x] Run SQL unit tests — 942 passed
- [x] Run H2 JDBC tests — 65 passed
- [x] Run PostgreSQL JDBC tests — 148 passed
- [x] Run Doobie tests — 8 passed
- [x] Run Caliban integration tests — 8 passed
- [x] **Total: 1,171 tests passed, 0 failed**

### Phase 4: Documentation — COMPLETED

- [x] MIGRATION_PLAN.md updated
- [x] MIGRATION_REPORT.md updated
- [x] FINAL_STATUS_REPORT.md updated
- [x] VALIDATION_REPORT_FINAL.md updated
- [x] README.md created with Kyo documentation and examples

---

## 4. Risks Identified and Outcomes

| Risk                                     | Severity | Outcome                                                         |
| ---------------------------------------- | -------- | --------------------------------------------------------------- |
| Package `kyo` shadowing                  | Critical | **Hit.** Fixed by renaming to `qkyo`                            |
| Missing `inline def run`                 | Critical | **Hit.** Added 10 overloads to Quill trait                      |
| Caliban Schema derivation with Kyo types | High     | **Resolved.** `import kyo.given` provides Schema instances      |
| `ZLayer` → direct DI                     | High     | **Resolved.** DataSource passed directly to context constructor |
| `FiberRef` → `Local[T]`                  | Medium   | **Resolved.** `kyo.Local.init(None)` in KyoQuillLog             |
| `zio-json` dependency                    | Low      | **Retained.** Data-only dependency, not effects                 |
| Doobie Scala 3 syntax                    | Medium   | **Hit.** Fixed `< Local(...)` → `Lens.set(...)`                 |

---

## 5. References

- Kyo repository: https://github.com/getkyo/kyo
- kyo-caliban source: https://github.com/getkyo/kyo/tree/main/kyo-caliban/src
- Original zio-protoquill: https://github.com/zio/zio-protoquill
