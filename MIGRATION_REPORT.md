> **Historical Document** — The ZIO→Kyo migration was completed March 2026.
> These files are retained for reference but are no longer active planning documents.

# Migration Report: ZIO 2 → Kyo 1.0-RC1

**Date:** March 30, 2026
**Kyo Version:** 1.0-RC1 (`io.getkyo`)
**Branch:** `master`
**Status:** COMPLETE — 8/8 modules migrated, 1,171 tests passing

---

## Executive Summary

All 8 modules of `kyo-protoquill` have been successfully migrated from ZIO 2.x to Kyo 1.0-RC1. The migration required fixing a critical package shadowing issue, adding missing inline method definitions, rewriting Caliban tests to use native kyo-caliban APIs, removing duplicate legacy files, and fixing Doobie's Scala 3 syntax incompatibility.

---

## Modules Migrated

| Module                | Files Changed | Key Changes                                                                |
| --------------------- | ------------- | -------------------------------------------------------------------------- |
| `quill-sql`           | `build.sbt`   | Added `kyo-core`, `kyo-prelude`, `kyo-data` dependencies                   |
| `quill-kyo`           | 3 files       | Package `io.getquill.context.kyo` → `io.getquill.context.qkyo`             |
| `quill-jdbc-kyo`      | 5 files       | FQN updates, `inline def run` overloads, package renames                   |
| `quill-cassandra-kyo` | 3 deleted     | Removed `CassandraZioContext`, `CassandraZioSession`, `cassandrazio/Quill` |
| `quill-caliban`       | 5 files       | Rewritten to native kyo-caliban (Kyo effect types, Schema given instances) |
| `quill-doobie`        | 2 files       | `Transactor.after < Local(...)` → `Transactor.after.set(...)`              |

---

## Critical Issues Found and Resolved

### Issue 1: Package Shadowing (54 compilation errors)

**Problem:** The package `io.getquill.context.kyo` shadowed the `kyo` library. Files in `io.getquill.context` that wrote `import kyo.*` got the local subpackage instead of the Kyo library, causing all Kyo types (`<`, `Abort`, `Env`, `Async`, `IO`, `Local`, `Stream`) to be unresolved.

**Root cause:** The original ZIO project used `package io.getquill.context.qzio` (with prefix `q`) to avoid shadowing `zio`. The migration renamed to `kyo` instead of `qkyo`, triggering the same shadowing problem.

**Fix:** Renamed package to `io.getquill.context.qkyo` in 5 files.

### Issue 2: Missing `inline def run` Methods

**Problem:** The `Quill` trait in `quill-jdbc-kyo` did not define the `inline def run` overloads. These macro-generated methods (via `InternalApi`) must be explicitly declared on each context trait for user code to call `context.run(query)`.

**Fix:** Added 10 `inline def run` overloads with `@targetName` annotations to `jdbckyo/Quill.scala`, matching the pattern from `JdbcContext.scala`.

### Issue 3: Caliban Tests Still Using ZIO

**Problem:** Three test files and two example files still referenced `import io.getquill.context.ZioJdbc._`, `ZIO[Any, Throwable, ...]`, `.provideLayer(zioDS)`, `ZIO.unit`, `.tapBoth`, and `.unsafeRunSync()`.

**Fix:** Rewrote all files to use native kyo-caliban:

- `zio.Task[A]` → `A < (Abort[Throwable] & Async)` (aliased as `KyoTask[A]`)
- `zio.ZIO.attempt { ... }` → `Abort.catching[Throwable] { ... }`
- Added `import kyo.given` for `Schema[R, A < S]` instances
- Test execution: `zio.Unsafe.unsafe { Runtime.default.unsafe.run(...) }` (matches kyo-caliban upstream)

### Issue 4: Doobie Scala 3 Syntax

**Problem:** `Transactor.after < Local(transactor, HC.commit)` — the `<` on a new line is parsed as an infix operator in Scala 3, and `Local` is not in scope.

**Fix:** Replaced with `Transactor.after.set(transactor, action)` in 2 files.

### Issue 5: Cassandra Duplicate Files

**Problem:** Three files with ZIO naming existed alongside Kyo equivalents: `CassandraZioContext.scala`, `CassandraZioSession.scala`, `cassandrazio/Quill.scala`.

**Fix:** Deleted the 3 duplicates.

### Issue 6: Stale FQN References

**Problem:** `KyoJdbc.scala` referenced `io.getquill.context.qzio.KyoImplicitSyntax` (old package). `CalibanExample*.scala` used incorrect `io.getquill.context.KyoImplicitSyntax._`.

**Fix:** Updated all to `io.getquill.context.qkyo.KyoImplicitSyntax`.

---

## Remaining ZIO Dependencies

| Location                       | Import                                | Justification                                  |
| ------------------------------ | ------------------------------------- | ---------------------------------------------- |
| `PostgresJsonExtensions.scala` | `zio.json.{JsonEncoder, JsonDecoder}` | JSON serialization library (data, not effects) |
| `CalibanSpec.scala`            | `zio.{Unsafe, Runtime}`               | Caliban interpreter is ZIO-native              |
| `CalibanExample*.scala`        | `zio.{Unsafe, Runtime}`               | Caliban server runtime uses ZIO                |

Core modules (`quill-kyo`, `quill-jdbc-kyo`, `quill-cassandra-kyo`) are 100% ZIO-free. Caliban uses ZIO internally (even `kyo-caliban` depends on `kyo-zio` for bridging).

---

## Test Results

| Module                       | Tests     | Passed    | Failed |
| ---------------------------- | --------- | --------- | ------ |
| `quill-sql`                  | 274       | 274       | 0      |
| `quill-sql-tests`            | 668       | 668       | 0      |
| `quill-jdbc` (H2)            | 65        | 65        | 0      |
| `quill-jdbc` (PostgreSQL)    | 148       | 148       | 0      |
| `quill-doobie` (PostgreSQL)  | 8         | 8         | 0      |
| `quill-caliban` (PostgreSQL) | 8         | 8         | 0      |
| **Total**                    | **1,171** | **1,171** | **0**  |

---

## Statistics

- **Files modified:** 18
- **Files deleted:** 3
- **Lines added:** +213
- **Lines removed:** -358
- **Net:** -145 lines (cleaner codebase)
- **Compilation errors fixed:** 54 (shadowing) + 12 (doobie) + 20 (caliban) = 86
- **Test database:** PostgreSQL localhost:5432, H2 in-memory

---

## Tools and Configuration

- **SBT:** 1.x
- **Java:** JDK 17+
- **Scala:** 3.8.1
- **Kyo:** 1.0-RC1
- **Caliban:** 2.10.0 (caliban-quick)
- **kyo-caliban:** 1.0-RC1 (transitive: kyo-zio)
- **Doobie:** 1.0.0-RC12

---

## References

- Kyo repository: https://github.com/getkyo/kyo
- kyo-caliban module: https://github.com/getkyo/kyo/tree/main/kyo-caliban/src
- Original zio-protoquill: https://github.com/zio/zio-protoquill
- Kyo effect system skill: `kyo-effect-system` (Abort, Env, Async, Stream, Local, Scope)
