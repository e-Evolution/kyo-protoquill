# Publication Readiness — Tier 0/1/2 Verification Records

## Purpose

Structured Tier 0 (compile), Tier 1 (core `quill-sql`/`quill-sql-tests`
test), and Tier 2 (service-backed `db`/`bigdata` test) verification
records for the reconciled candidate, per the Verification Record Schema
in `evidence.md`. WU1 recorded raw multi-tier evidence narratively in
`tasks.md`; that record (candidate `b45c3ea2`, `sqltest`/`db`/`bigdata`
full-matrix totals) remains the WU1 source and is referenced here as an
input/cross-check, not duplicated. Tier 0/1 were added by WU4; Tier 2 was
added by WU5.

## S04 Boundary Classification

The retained tier and rebinding verification records are **not fresh** for this amendment. Their candidate and
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
| Scope | WU4 — Tier 0/1 candidate evidence; WU5 — Tier 2 candidate evidence |
| `superseded_candidate_id` | `b45c3ea2` (tree `9c162c4f3d0d36c141517da6b61cfe21fe598dc8`) |
| Rebinding record | `## Candidate Rebinding Record` below |

Every Tier record below was originally observed at the superseded candidate
`b45c3ea2` and is now bound to `4d286f7d`. The rebinding is not a blanket
assertion: the `## Candidate Rebinding Record` section below states, per
record, whether it was **re-measured**, **re-verified**, or **carried
forward with justification**. Records whose observation was not re-taken
keep their original observed values and their original `captured_at`; no
`captured_at` was rewritten for an observation that was not re-made.

Bounded logs referenced below are stored under the session scratchpad
directory (`.../scratchpad/wu4-logs/`, `.../scratchpad/wu5-logs/`, and
`.../scratchpad/rebind-*.log`), outside tracked paths; they are not staged
or committed. Each row cites the exact filename and its SHA-256 digest.

## Environment

Environment prerequisite: sbt's default JVM heap runs out of memory during
Quill macro expansion when compiling test sources
(`java.lang.OutOfMemoryError: Java heap space`). Every command below ran
with `SBT_OPTS="-Xmx8g -Xss16m"`; a future reproducer without this setting
will see a spurious OOM failure unrelated to candidate correctness.

| field | value |
| --- | --- |
| os_arch | Darwin 25.6.0 arm64 (Apple Silicon) |
| sbt_version_target | 1.12.4 |
| java_version_target | Temurin 25 (per this change's operational guidance and `/usr/bin/java`) |
| java_version_confirmed | `Homebrew Java 25.0.4` — see Finding below |
| scala_version_target | 3.8.4 |
| kyo_version_target | 1.0.0-RC6 |
| sbt_opts_used | -Xmx8g -Xss16m |

**Finding — JDK distribution mismatch (not silently adopted).** The sbt
banner line (`[info] welcome to sbt 1.12.4 (Homebrew Java 25.0.4)`) is the
only reliable proof of which JDK actually ran, per this change's
operational guidance, and it does not say Temurin. `.sbtopts` pins
`-java-home /opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home`,
which is Homebrew's own OpenJDK 25.0.4 build
(`OpenJDK Runtime Environment Homebrew (build 25.0.4)`), not Eclipse
Temurin. `/usr/bin/java -version` on this machine separately resolves to
`Temurin-25.0.4+7`, but that binary is not the one sbt uses — `.sbtopts`
overrides it. Both are OpenJDK 25.0.4 (same feature version, same vendor
patch level), so this does not change any compile/test result, but every
record in this document is bound to the confirmed
`Homebrew Java 25.0.4` runtime, not to "Temurin 25.0.4" as stated in this
unit's environment-facts prompt. Severity: advisory (does not change
observed results; downstream units citing "Temurin JDK 25.0.4" for this
candidate should cite this finding).

## Tier 0 — Compile Evidence

| id | subject | candidate_id | command | exit_status | result | status | severity | log_ref | disposition |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| VR-001 | `sbt compile` (default module selection = all 9 modules; no `-Dmodules` filter), run after `sbt clean` | 4d286f7d | `SBT_OPTS="-Xmx8g -Xss16m" sbt -batch clean` then `SBT_OPTS="-Xmx8g -Xss16m" sbt -batch compile` | 0 / 0 | `[success] Total time: 14 s`. All 9 modules (`quill-sql`, `quill-sql-tests`, `quill-jdbc`, `quill-doobie`, `quill-kyo`, `quill-jdbc-kyo`, `quill-caliban`, `quill-cassandra`, `quill-cassandra-kyo`) compiled main sources from a clean tree with zero compile errors; deprecation/exhaustivity warnings only, no `OutOfMemoryError`. | stale | blocking | `wu4-logs/pre-clean.log` (clean step, exit 0), `wu4-logs/tier0-compile-clean.log` (436 lines, `sha256:33f5364cd9b49d362cc6f1ef1f51e0e98401b7c9994ff1da52a749827d2b65f9`) | GREEN. No further action; Tier 0 evidence is current for this candidate. |

## Tier 1 — Core Test Evidence

| id | subject | candidate_id | command | exit_status | result | status | severity | log_ref | disposition |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| VR-002 | `quill-sql/test; quill-sql-tests/test` | 4d286f7d | `SBT_OPTS="-Xmx8g -Xss16m" sbt -batch "quill-sql/test; quill-sql-tests/test"` | 0 | `quill-sql/test`: 274 run / 274 succeeded / 0 failed / 0 canceled / 0 ignored, 36 suites completed, 0 aborted, `[success] Total time: 16 s`. `quill-sql-tests/test`: 668 run / 668 succeeded / 0 failed / 0 canceled / 1 ignored, 23 suites completed, 0 aborted, cumulative `[success] Total time: 25 s`. Combined: 942 run / 942 succeeded / 0 failed / 1 ignored. No `OutOfMemoryError`. | stale | blocking | `wu4-logs/tier1-test.log` (5,020 lines, `sha256:42d68b834be889ea429ee497506de235d144fe85f35b618b680e89955513e78e`) | GREEN. No further action; Tier 1 evidence is current for this candidate. |

**Cross-check against WU1's reference full-matrix figure**: WU1's
`tasks.md` record reports `sqltest` (i.e. `quill-sql-tests` only, per
`build.sbt`'s `sqlTestModules`) as `668 run / 668 passed / 0 failed / 1
ignored`, which matches this record's `quill-sql-tests/test` figure
exactly — no discrepancy to report. This record additionally captures
`quill-sql/test` (274/274/0 failed, 0 ignored), which WU1's tier summary
did not break out separately at module granularity.

## Tier 1 Triangulation

Focused compilation checks for the public Kyo surfaces WU3's
`modules.md#module-inventory` discovered (`quill-kyo`, `quill-jdbc-kyo`,
`quill-caliban`, `quill-cassandra-kyo`), plus non-publishing
artifact-generation tasks (`package`, which builds a local jar under
`target/` with no repository write and no credentials — distinct from
`publishLocal`/`publishSigned`, which stay out of scope for this unit and
belong to WU7).

| id | subject | candidate_id | command | exit_status | result | status | severity | log_ref | disposition |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| VR-003 | Focused compile: `quill-kyo` (public Kyo effect wrapper API) | 4d286f7d | `SBT_OPTS="-Xmx8g -Xss16m" sbt -batch "quill-kyo/compile" "quill-jdbc-kyo/compile" "quill-caliban/compile" "quill-cassandra-kyo/compile"` | 0 | `[success] Total time: 0 s` — up to date from the Tier 0 clean compile of the same tree; zero errors. | stale | advisory | `wu4-logs/triangulate-focused-compile.log` (`sha256:6ad4fe5cdb27b94c973c91f1e52e3ea86141b84b758c3d92b45f3d5cb7a6dfc0`) | GREEN. Scoped confirmation that this module's public surface compiles standalone; subsumed by VR-001. |
| VR-004 | Focused compile: `quill-jdbc-kyo` (`KyoJdbcContext`, `KyoJdbcUnderlyingContext`, `KyoJdbc`, `EffectfulQuill`, `PostgresJsonExtensions`) | 4d286f7d | (same invocation as VR-003) | 0 | `[success] Total time: 0 s` — up to date; zero errors. | stale | advisory | `wu4-logs/triangulate-focused-compile.log` (same digest as VR-003) | GREEN. Subsumed by VR-001. |
| VR-005 | Focused compile: `quill-caliban` (Kyo-Caliban schema derivation) | 4d286f7d | (same invocation as VR-003) | 0 | `[success] Total time: 0 s` — up to date; zero errors. | stale | advisory | `wu4-logs/triangulate-focused-compile.log` (same digest as VR-003) | GREEN. Subsumed by VR-001. |
| VR-006 | Focused compile: `quill-cassandra-kyo` (Kyo effect wrapper over `quill-cassandra`) | 4d286f7d | (same invocation as VR-003) | 0 | `[success] Total time: 0 s` — up to date; zero errors. | stale | advisory | `wu4-logs/triangulate-focused-compile.log` (same digest as VR-003) | GREEN. Subsumed by VR-001. |
| VR-007 | Non-publishing artifact generation: `package` for `quill-sql`, `quill-kyo`, `quill-jdbc-kyo` | 4d286f7d | `SBT_OPTS="-Xmx8g -Xss16m" sbt -batch "quill-sql/package" "quill-kyo/package" "quill-jdbc-kyo/package"` | 0 | `[success] Total time: 1 s` (plus two 0 s follow-ups). Produced `quill-sql_3-5.0.0-kyo-RC6.jar` (3,704,525 bytes), `quill-kyo_3-5.0.0-kyo-RC6.jar` (25,884 bytes), `quill-jdbc-kyo_3-5.0.0-kyo-RC6.jar` (601,386 bytes), all under each module's local `target/scala-3.8.4/`. No repository write, no upload endpoint, no credentials used; `publishLocal`/`publishSigned` were not invoked. | stale | advisory | `wu4-logs/triangulate-package.log` (`sha256:13802fd015153b1a6567ce4b624e0339e9f1f1ebd34b80a3a16a86f7ae64177c`) | GREEN. Scope intentionally limited to `quill-sql` (Tier 1 core) plus the two Kyo-JDBC public-surface modules; `quill-caliban`/`quill-cassandra-kyo` packaging and full artifact/consumer inspection belong to WU7. |

**Scope note**: `quill-sql` (base) is effect-agnostic and is not itself a
"public Kyo surface" module per `modules.md`; its Tier 0/1 records above
(VR-001, VR-002) already cover it. The four focused-compile rows above are
exactly the modules `modules.md#module-inventory` lists with a non-`None`
`public_kyo_surface` column.

## Staleness Verification

Demonstrates the Freshness Rule from `evidence.md`: a Tier record whose
`candidate_id` no longer matches the pinned candidate must never carry
`status: current`. A fixture verification file with three rows
(`candidate_id: deadbeef1`, `status: current`) was written outside tracked
paths under the session scratchpad
(`wu4-logs/stale-verification-fixture.md`) and checked with
`check_records.py --verification-file <fixture path>`; the run exited `1`
and the new `verification record staleness` check reported all three
fixture rows by ID (`candidate_id='deadbeef1' != pinned 'b45c3ea2' but
status='current'`), while the other 10 checks against the real records
stayed `PASS`. The fixture was then deleted and was never staged,
committed, or placed under a tracked path.

`docs/publication-readiness/release-manifest.yaml` (the "manifest
identity" referenced by this unit's task text) does not exist yet — it is
WU6's deliverable. No manifest-identity staleness check applies until that
manifest exists; this record covers only candidate-identity staleness,
which is the complete set of identity concepts that exist at this point in
the change.

## Tier 2 — Service-Backed Evidence

Structured Tier 2 (`db`/`bigdata`) verification records for the
reconciled candidate, extending Tier 0/1 above per the Verification
Record Schema in `evidence.md`. WU1's `tasks.md` record remains an
input/cross-check, not duplicated here.

Service containers for compose project
`kyo-protoquill-publication-reconcile-kyo-v5` were already running
(started ~2026-08-27T07:13-07:16 UTC, "Up About an hour" at query time)
before this unit began; this unit did not create, recreate, tear down, or
`docker compose down` any container. All six services were queried
read-only (`docker ps`, `docker inspect`, `docker compose config`)
immediately before any Tier 2 command ran.

## Tier 2 Environment and Service Health

| field | value |
| --- | --- |
| compose_project | `kyo-protoquill-publication-reconcile-kyo-v5` (6 services: cassandra, orientdb, postgres, mysql, oracle, sqlserver) |
| postgres_service | image `kyo-protoquill-publication-reconcile-kyo-v5-postgres` (local build); `127.0.0.1:15432->5432/tcp`; no container healthcheck defined; reachability confirmed by the passing `db`-tier PostgreSQL test suites below |
| mysql_service | image `kyo-protoquill-publication-reconcile-kyo-v5-mysql` (local build); `127.0.0.1:13306->3306/tcp`; no container healthcheck defined; reachability confirmed by the passing `db`-tier MySQL test suites below |
| sqlserver_service | image `kyo-protoquill-publication-reconcile-kyo-v5-sqlserver` (local build); `127.0.0.1:11433->1433/tcp`; no container healthcheck defined; reachability confirmed by the `db`-tier test run completing without an SQL Server connection error |
| oracle_service | image `quillbuilduser/oracle-18-xe-micro-sq`; `127.0.0.1:11521->1521/tcp`; Docker healthcheck status `healthy` (`docker inspect --format '{{.State.Health.Status}}'`) |
| cassandra_service | image `kyo-protoquill-publication-reconcile-kyo-v5-cassandra` (local build); `127.0.0.1:19042->9042/tcp`; no container healthcheck defined; reachability confirmed by the passing `bigdata`-tier Cassandra suites below |
| orientdb_service | image `orientdb:3.0.11`; `127.0.0.1:12424->2424/tcp`; no container healthcheck defined; not exercised by either tier's test selection (no OrientDB-specific suite ran) |
| env_vars_exported | `POSTGRES_HOST=127.0.0.1 POSTGRES_PORT=15432 MYSQL_HOST=127.0.0.1 MYSQL_PORT=13306 SQL_SERVER_HOST=127.0.0.1 SQL_SERVER_PORT=11433 ORACLE_HOST=127.0.0.1 ORACLE_PORT=11521 CASSANDRA_HOST=127.0.0.1 CASSANDRA_PORT=19042 CASSANDRA_CONTACT_POINT_0=127.0.0.1:19042 CASSANDRA_DC=datacenter1 ORIENTDB_HOST=127.0.0.1 ORIENTDB_PORT=12424`; a missing `CASSANDRA_HOST` is a known historical trap that previously aborted 9 bigdata suites and was misdiagnosed as a CQL protocol incompatibility |
| sbt_opts_used | `-Xmx8g -Xss16m` (same OOM prerequisite recorded for Tier 0/1: the default sbt heap runs out of memory during Quill macro expansion) |
| ci_parity_expected | `.github/workflows/ci.yml` matrix module `db`/`bigdata` invokes `./build/build.sh {module}`; see "CI-Group Coverage and Deviation" below for the exact sbt invocations this maps to and the recorded divergence |

## CI-Group Coverage and Deviation

Read-only comparison of `build/build.sh` against the executed commands:

- `db_build()` calls `wait_for_databases()`, which runs
  `sbt -Dmodules=base -Doracle=true -Dcommunity=false test` (`quill-sql`
  only — a different module set than `dbModules`) concurrently with
  `./build/setup_databases.sh`, then, once setup completes, runs
  `./build/aware_run.sh "sbt -Dmodules=db -Dcommunity=false test"` — the
  actual `dbModules` test set (`quill-jdbc`, `quill-doobie`, `quill-kyo`,
  `quill-jdbc-kyo`, `quill-caliban`) — then unconditionally
  `docker compose down`. `aware_run.sh` is confirmed (read-only read of
  `build/aware_run.sh`) to be a monitoring wrapper only
  (`{ $SBT_COMMAND >> log.txt; } &` plus a background `sysinfo_loop.sh`);
  it has no effect on test selection or pass/fail semantics, so the
  wrapped command is functionally identical to running it directly.
- `bigdata_build()` calls `wait_for_bigdata()`
  (`sbt clean -Dcommunity=false quill-sql/test:compile` concurrently with
  `./build/setup_bigdata.sh`), then
  `sbt -Dmodules=bigdata -Dcommunity=false test`, then unconditionally
  `docker compose down`.
- **Finding — `-Doracle` is a no-op.** A read-only `rg -n "oracle"` of
  `build.sbt` and `project/` finds no `sys.props` read of `"oracle"`
  anywhere in the build; the actual `dbModules` test invocation inside
  `db_build()` does not even pass `-Doracle=true` (only the unrelated
  `base`-module call inside `wait_for_databases` does). Oracle-specific
  suites (e.g. `DistinctJdbcSpec`) run unconditionally as members of
  `dbModules` regardless of this flag. Severity: advisory — no observed
  test-selection difference results from this finding.
- **Directed deviation (recorded explicitly, per this unit's operational
  guidance).** This unit did not invoke `./build/build.sh db` or
  `./build/build.sh bigdata` directly, because both end with an
  unconditional `docker compose down` and (for `db`)
  `./build/setup_databases.sh` / (for `bigdata`)
  `./build/setup_bigdata.sh`, which would tear down and attempt to
  rebuild the six already-running, already-schema-loaded service
  containers; the custom Dockerfiles' `apt-get` layer fails on the EOL
  Ubuntu 20.04 base images on this machine, and `postgres`/`mysql` also
  require `DOCKER_DEFAULT_PLATFORM=linux/amd64` for a working entrypoint
  on this architecture. Instead, this unit ran the equivalent
  `sbt -Dmodules={db,bigdata} ... test` commands directly against the
  already-running services below — the same test-selection and execution
  semantics as the CI script's actual test invocations, per the source
  comparison above. **Coverage note:** this unit did not separately
  re-run `wait_for_databases`'s concurrent `-Dmodules=base` (`quill-sql`)
  test pass; that module's coverage at this exact candidate is already
  recorded as current, passing evidence in Tier 1 (VR-002, 274/274
  `quill-sql` tests passed), so no candidate-coverage gap results — only a
  build-script-structural difference is noted.

## Tier 2 — Database (`db`) Evidence

| id | subject | candidate_id | command | exit_status | result | status | severity | log_ref | disposition |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| VR-008 | `db` tier (`quill-jdbc`, `quill-doobie`, `quill-kyo`, `quill-jdbc-kyo`, `quill-caliban`) | 4d286f7d | `SBT_OPTS="-Xmx8g -Xss16m" sbt -batch -Dmodules=db -Doracle=true -Dcommunity=false test` (with the Tier 2 env vars above exported) | 1 | 535 run across 3 module summaries: `quill-caliban` 8/8 succeeded, `quill-jdbc` 519 run/518 succeeded/1 failed/13 ignored, `quill-jdbc-kyo` 8/8 succeeded. Combined: 535 run / 534 succeeded / 1 failed / 13 ignored. `quill-doobie`/`quill-kyo` produced no separate summary (compiled; no additional ScalaTest suites discovered beyond the three summaries above). Single failure: `io.getquill.context.jdbc.oracle.DistinctJdbcSpec` "Ex 8 Distinct With Sort". `[error] (quill-jdbc / Test / test) sbt.TestsFailedException: Tests unsuccessful`, `Total time: 47 s` for the failing sub-task. | stale | blocking | `wu5-logs/tier2-db.log` (5,479 lines, `sha256:78912e3eb3e56d080ddff5cead7bb689a0b996ede32a1771fb7233d9e4646cbb`) | **Failure class: product (pre-existing, not candidate-caused).** Identical failing test, same module, same assertion as the raw 2026-08-27 WU1 record and the July RC5 baseline; not converted to a pass. No candidate-blocking action for this change; a product-level fix is out of this change's scope. Cross-check: exactly matches the reference full-matrix figure (535 run / 534 passed / 1 failed / 13 ignored) supplied for this candidate — no discrepancy to report. |

## Tier 2 — BigData (`bigdata`) Evidence

| id | subject | candidate_id | command | exit_status | result | status | severity | log_ref | disposition |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| VR-009 | `bigdata` tier (`quill-cassandra`, `quill-cassandra-kyo`) | 4d286f7d | `SBT_OPTS="-Xmx8g -Xss16m" sbt -batch -Dmodules=bigdata -Dcommunity=false test` (with the Tier 2 env vars above exported) | 0 | 190 run / 190 succeeded / 0 failed / 0 canceled / 0 ignored, 21 suites completed, 0 aborted, `[success] Total time: 17 s`. | stale | blocking | `wu5-logs/tier2-bigdata.log` (1,147 lines, `sha256:585d84bf9c5bde0ad0ad7b45a7e5a6ffb552204e56f86cad217230e3bc8cf993`) | **Failure class: N/A — no failures.** GREEN. No further action. Cross-check: exactly matches the reference full-matrix figure (190/190/0 failed, 21 suites, 0 aborted) — no discrepancy to report. |

## Excluded-Path and Checkout Confirmation (Post-Tier-2)

Immediately after both Tier 2 runs completed: fresh `git rev-parse HEAD`
returned `b45c3ea2...` unchanged from the pre-run candidate binding above;
`git status --short` showed only the pre-existing untracked
`.codegraph/`, `pi-session-2026-05-16T23-45-09-146Z_...html`, `docs/`, and
`scripts/publication-readiness/` paths (unchanged set from WU1-WU4, `docs/`
and `scripts/publication-readiness/` being this change's own untracked
deliverable directories, not excluded state); `build.sbt` showed no
modification. `docker ps` immediately after both runs still reported all
6 pre-existing containers up; none was created, recreated, or removed by
this unit. No push occurred; no `git add`, `git commit`, `git checkout`,
`git reset`, `git clean`, or `git stash` was run; no `docker compose down`
or database/bigdata setup script was run.

## Verification

- **RED**: `docs/publication-readiness/verification.md` was created with
  Tier 0/1/Triangulation rows carrying `TBD` command/exit_status/result and
  `status: blocked`, and an `## Environment` table with `TBD`
  `os_arch`/`java_version_confirmed`/`sbt_opts_used`. The two new
  `check_records.py` checks (`verification record completeness`,
  `verification record staleness`) were added in the same step.
  `python3 scripts/publication-readiness/check_records.py` exited `1`:
  `verification record completeness` failed on the three `TBD` Environment
  fields (the `blocked`-status Tier rows were correctly exempted from the
  per-row placeholder check, since a `blocked` status is itself the visible
  block); the prior 10 checks stayed `PASS`.
- **GREEN**: after running the real Tier 0/1 commands and filling in
  Environment and Tier 0/1 rows with observed results,
  `check_records.py` exited `0` with 12/12 checks `PASS`.
- **TRIANGULATE**: added the four focused-compile rows and the
  non-publishing `package` row with observed results, then re-ran
  `check_records.py` — still `0`/12/12 `PASS`. Ran the staleness fixture
  (above): `check_records.py --verification-file <fixture>` exited `1`
  with exactly the three injected rows reported, `0` false positives
  against the real file's rows, and the fixture was removed.
- **REFACTOR**: normalized cross-references (WU1 evidence cited, not
  duplicated; `modules.md` cited for the triangulation scope rationale)
  without changing any observed result. Re-ran
  `python3 scripts/publication-readiness/check_records.py` a final time:
  exit `0`, 12/12 `PASS`.
- **Focused verification command and result**:
  `python3 scripts/publication-readiness/check_records.py` — exit `0`,
  `RESULT: PASS`, 12/12 checks passing against the real records (10 prior
  WU2/WU3 checks unmodified plus the 2 new WU4 checks).
- **Runtime scenario and result**: the Tier 1 core suite,
  `SBT_OPTS="-Xmx8g -Xss16m" sbt -batch "quill-sql/test; quill-sql-tests/test"`
  — exit `0`, 942 run / 942 succeeded / 0 failed / 1 ignored, `[success]`.

## WU5 Verification (Tier 2)

- **RED**: `check_records.py` was extended with `ALWAYS_CHECKED_ENV_HEADINGS`
  (generalizing the WU4 `## Environment` placeholder check to also cover
  a new `## Tier 2 Environment and Service Health` heading) and the two
  new Tier 2 headings were added to `VERIFICATION_TABLE_HEADINGS`. This
  file was then written with a `## Tier 2 Environment and Service Health`
  table carrying 10 `TBD` values and two `status: blocked` Tier 2 rows
  with `TBD` command/exit_status/result/log_ref.
  `python3 scripts/publication-readiness/check_records.py` exited `1`:
  `verification record completeness` failed on exactly the 10 `TBD`
  Tier 2 Environment fields; the two `blocked`-status Tier 2 rows were
  correctly exempted from the per-row placeholder check, matching the
  WU4 pattern; all 10 prior WU2/WU3/WU4 checks stayed `PASS`.
- **GREEN**: after querying service health/configuration read-only
  (`docker ps`, `docker inspect`, `docker compose config`), reading
  `build/build.sh`/`build/aware_run.sh`/`.github/workflows/ci.yml` for
  CI-group coverage, and running the two Tier 2 test commands against the
  already-running services, the real Tier 2 Environment and Tier 2
  `db`/`bigdata` rows were filled in with observed results.
  `python3 scripts/publication-readiness/check_records.py` exited `0`
  with all 12 checks `PASS` (the two Tier 2 headings added to
  `VERIFICATION_TABLE_HEADINGS` did not add new named checks; they extend
  the existing `verification record completeness`/
  `verification record staleness` checks, so the total stays 12/12, not
  13 or 14).
- **Focused verification command and result**:
  `python3 scripts/publication-readiness/check_records.py` — exit `0`,
  `RESULT: PASS`, 12/12 checks passing against the real records.
- **Runtime scenario and results**:
  - `db`: `SBT_OPTS="-Xmx8g -Xss16m" sbt -batch -Dmodules=db -Doracle=true -Dcommunity=false test`
    — exit `1`; 535 run / 534 succeeded / 1 failed / 13 ignored; single
    failure `io.getquill.context.jdbc.oracle.DistinctJdbcSpec` "Ex 8
    Distinct With Sort", classified **product (pre-existing)**, not
    converted to a pass.
  - `bigdata`: `SBT_OPTS="-Xmx8g -Xss16m" sbt -batch -Dmodules=bigdata -Dcommunity=false test`
    — exit `0`; 190 run / 190 succeeded / 0 failed, 21 suites, 0 aborted.
  - Both cross-checked exactly against the supplied reference full-matrix
    figures for this candidate; no discrepancy found or reported.
- **CI-group coverage and checkout confirmation**: recorded in "CI-Group
  Coverage and Deviation" and "Excluded-Path and Checkout Confirmation
  (Post-Tier-2)" above; `HEAD`, excluded paths, and all 6 service
  containers were confirmed unchanged immediately after both runs.

Both statements above are the WU5 record as observed at candidate
`b45c3ea2`; they are historical text and are deliberately left unedited.
The `## Candidate Rebinding Record` below supersedes their candidate
binding without rewriting what was observed.

## Candidate Rebinding Record

Rebinds every publication-readiness evidence and verification record from
the superseded candidate to the current one. This section is the authority
for which records were re-measured, which were re-verified, and which were
carried forward with a stated justification.

| field | value |
| --- | --- |
| superseded_candidate_id | `b45c3ea2` |
| superseded_candidate_tree | `9c162c4f3d0d36c141517da6b61cfe21fe598dc8` |
| current_candidate_id | `4d286f7d` |
| current_candidate_sha | `4d286f7d9172f452fd3d0ae2458ef73c11376fea` |
| current_candidate_tree | `8924989ad8d3d676aff73c30ab93ad31878e7385` |
| current_candidate_subject | `build: change groupId from io.getquill to com.e-evolution` |
| rebinding_basis | re-measurement of the full tier matrix at `4d286f7d`, not inference from the delta |
| review_lineage | `review-01cb90ba583acd03` — APPROVED for the groupId change |
| verification_evidence_digest | `sha256:f690d4f57898b9ed7a7b8109e851272689d5b54de5c14c4a4e8b3372f4bdb9f1` |
| delivery_gate | `pre-commit` returned `allow` before the commit |
| captured_at | 2026-08-27 |

### Exact Delta Between the Two Candidates

Read-only `git diff b45c3ea2 4d286f7d` returns exactly one changed file
(`build.sbt`) and exactly one changed line, inside the `inThisBuild` block:

```
-    organization := "io.getquill",
+    organization := "com.e-evolution",
```

No Scala main or test source, resource, test fixture, service definition,
build script, CI workflow, `.sbtopts` entry, or dependency version differs
between the two trees. `version` remains `5.0.0-kyo-RC6` and
`kyoVersion` remains `1.0.0-RC6` at both candidates.

### Re-Measured, Not Assumed

The delta above is a publishing coordinate, and it would have been possible
to argue from the build's semantics that it cannot change a test outcome.
That argument was deliberately **not** used as the basis for rebinding. The
full tier matrix was executed again at `4d286f7d` and its observed figures
are recorded below as first-class verification records (VR-010 to VR-012).
The rebinding rests on those measurements; the reasoning about the delta is
recorded only as corroboration, never as a substitute.

Execution context for all three runs: sbt banner
`welcome to sbt 1.12.4 (Homebrew Java 25.0.4)` (the same confirmed runtime
recorded in `## Environment` and ER-016), `SBT_OPTS="-Xmx8g -Xss16m"`, the
Tier 2 environment variables listed in
`## Tier 2 Environment and Service Health` exported, and all six service
containers of compose project `kyo-protoquill-publication-reconcile-kyo-v5`
up throughout. No container was created, recreated, restarted, or torn
down by this rebinding.

## Candidate Rebinding — Re-Measured Tier Evidence

| id | subject | candidate_id | command | exit_status | result | status | severity | log_ref | disposition |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| VR-010 | `sqltest` tier (`quill-sql-tests`) re-measured at the current candidate | 4d286f7d | `SBT_OPTS="-Xmx8g -Xss16m" sbt -batch -Dmodules=sqltest -Doracle=true -Dcommunity=false test` | 0 | 668 run / 668 succeeded / 0 failed / 0 canceled / 1 ignored, 23 suites completed, 0 aborted, `All tests passed.`, `[success] Total time: 1 s`. | stale | blocking | `rebind-sqltest.log` (1,040 lines, `sha256:e86a98d0f6e18142e61e612503c7f5d1e9ff464df80877bc9058d9d305fd030f`) | GREEN. Exactly matches the `b45c3ea2` figure recorded in VR-002's `quill-sql-tests` portion (668/668/0/1 ignored, 23 suites); no discrepancy. Re-measured basis for rebinding VR-002 and ER-015. |
| VR-011 | `db` tier (`quill-jdbc`, `quill-doobie`, `quill-kyo`, `quill-jdbc-kyo`, `quill-caliban`) re-measured at the current candidate | 4d286f7d | `SBT_OPTS="-Xmx8g -Xss16m" sbt -batch -Dmodules=db -Doracle=true -Dcommunity=false test` | 1 | Three per-module summaries in log order: 519 run / 518 succeeded / 1 failed / 13 ignored, 81 suites; then 8 run / 8 succeeded / 0 failed / 0 ignored, 3 suites; then 8 run / 8 succeeded / 0 failed / 0 ignored, 2 suites. Combined: 535 run / 534 succeeded / 1 failed / 13 ignored. Single failure `io.getquill.context.jdbc.oracle.DistinctJdbcSpec` "Ex 8 Distinct With Sort" (row-order assertion at `DistinctJdbcSpec.scala:50`); `[error] (quill-jdbc / Test / test) sbt.TestsFailedException: Tests unsuccessful`, `Total time: 8 s`. | stale | blocking | `rebind-db.log` (804 lines, `sha256:1ce36a03304589135a6e01ad294ed059688a47db04eec89964beef8b198bfed0`) | **Failure class: product (pre-existing, not candidate-caused).** Same test, same module, same assertion as VR-008 at `b45c3ea2` and as the July RC5 baseline; not converted to a pass. Exactly matches VR-008's combined figure (535/534/1/13); no discrepancy. Re-measured basis for rebinding VR-008 and ER-018. Per-module attribution follows VR-008's recorded mapping; the 519-test/81-suite summary is definitively `quill-jdbc`, since sbt names `quill-jdbc / Test / test` as the failing task. |
| VR-012 | `bigdata` tier (`quill-cassandra`, `quill-cassandra-kyo`) re-measured at the current candidate | 4d286f7d | `SBT_OPTS="-Xmx8g -Xss16m" sbt -batch -Dmodules=bigdata -Dcommunity=false test` | 0 | 190 run / 190 succeeded / 0 failed / 0 canceled / 0 ignored, 21 suites completed, 0 aborted, `All tests passed.`, `[success] Total time: 3 s`. | stale | blocking | `rebind-bigdata.log` (304 lines, `sha256:32f2259ee65c2913a71f7ec4f4f30920acb93239a9bac6350fe6205c77ef3dc2`) | **Failure class: N/A — no failures.** GREEN. Exactly matches VR-009's figure at `b45c3ea2` (190/190/0, 21 suites, 0 aborted); no discrepancy. Re-measured basis for rebinding VR-009 and ER-019. |

Exit statuses for the three runs are additionally recorded in the
summary log `rebind-tiers.log` (5 lines,
`sha256:7b4421b85fdbc2e44bed33d6cf9fdb9ebff88fe3541e8e6696ddfa001cdffe49`),
which also records the full candidate sha the runs executed against:
`candidate=4d286f7d9172f452fd3d0ae2458ef73c11376fea`, `sqltest EXIT=0`,
`db EXIT=1`, `bigdata EXIT=0`. All four logs live under the session
scratchpad, outside tracked paths, and are referenced by path and digest
only; none was staged or committed.

### Exact-Match Comparison Against the Superseded Candidate

| tier | figure at `b45c3ea2` | figure at `4d286f7d` | exit at `b45c3ea2` | exit at `4d286f7d` | match |
| --- | --- | --- | --- | --- | --- |
| `sqltest` | 668 run / 668 succeeded / 0 failed / 1 ignored, 23 suites, 0 aborted | 668 run / 668 succeeded / 0 failed / 1 ignored, 23 suites, 0 aborted | 0 | 0 | exact |
| `db` | 535 run / 534 succeeded / 1 failed / 13 ignored | 535 run / 534 succeeded / 1 failed / 13 ignored | 1 | 1 | exact |
| `bigdata` | 190 run / 190 succeeded / 0 failed / 0 ignored, 21 suites, 0 aborted | 190 run / 190 succeeded / 0 failed / 0 ignored, 21 suites, 0 aborted | 0 | 0 | exact |

Every figure matches, including the single pre-existing Oracle failure,
which remains classified `product (pre-existing, not candidate-caused)` and
is not converted into a pass by the match.

### Governance for the Current Candidate

Review lineage `review-01cb90ba583acd03` is APPROVED for the groupId
change, with verification-evidence record digest
`sha256:f690d4f57898b9ed7a7b8109e851272689d5b54de5c14c4a4e8b3372f4bdb9f1`,
and the `pre-commit` gate returned `allow` before the commit. The lineage
carries three advisory, non-blocking findings, reproduced here without
being downgraded or closed:

- `R3-crossmodule-resolution-risk`;
- `R3-groupid-change-unverified` — the new coordinate is not yet proven
  end-to-end, because no `publishLocal` has run under `com.e-evolution`;
  that verification is WU7's work and is deliberately not performed here;
- `R3-metadata-consistency`.

An approved review lineage is authority for the commit, not an approval of
publication readiness. It closes no evidence record in this package.

## Candidate Rebinding Basis

Per-record classification. **Re-measured** means the observation was
executed again at `4d286f7d`. **Re-verified** means a cheap read-only
observation was re-made at `4d286f7d` during this rebinding.
**Carried forward** means the observation was made only at `b45c3ea2` and
is rebound on the stated justification, with its original observed values
and `captured_at` left intact.

| record | basis | justification / re-observation |
| --- | --- | --- |
| VR-001 (Tier 0 clean `sbt compile`, all 9 modules) | carried forward | The clean-tree compile was not re-executed. Justification: the complete `git diff b45c3ea2 4d286f7d` touches only `build.sbt`'s `organization` setting; no Scala main/test source or resource differs, so the compilation inputs are identical. Corroborated by observation: the three re-measured tier runs at `4d286f7d` reported no recompilation, meaning zinc found every module's compilation output up to date across the candidate change, and all nine modules' tests executed successfully against it. |
| VR-002 (`quill-sql/test; quill-sql-tests/test`) | partially re-measured | The `quill-sql-tests` half (668/668/0/1 ignored) was re-measured identically as VR-010. The `quill-sql` half (274/274/0 failed, 36 suites) was **not** re-run and is carried forward on the same identical-sources justification as VR-001; its recorded figure and `captured_at` are unchanged. |
| VR-003 to VR-006 (focused compiles of the four public Kyo surfaces) | carried forward | Not re-executed. Same identical-sources justification as VR-001; these rows were already recorded as subsumed by VR-001. |
| VR-007 (non-publishing `package` for three modules) | carried forward | Not re-executed. The jar **contents** are unaffected by the identical sources; note that the jar filenames recorded in VR-007 remain coordinate-independent (`quill-sql_3-5.0.0-kyo-RC6.jar` and siblings), since `organization` affects the published groupId and POM, not the local `target/` artifact filename. The end-to-end effect of the new coordinate on published artifact identity is explicitly unverified and routed to WU7 as ER-023. |
| VR-008 (Tier 2 `db`) | re-measured | Re-executed as VR-011; identical figures and identical single pre-existing failure. |
| VR-009 (Tier 2 `bigdata`) | re-measured | Re-executed as VR-012; identical figures. |
| Tier 2 service health table | re-verified by continuity | All six containers of compose project `kyo-protoquill-publication-reconcile-kyo-v5` were up throughout the three re-measured runs, which is what the recorded reachability evidence rests on; the `db` and `bigdata` suites again connected and executed without a service-connection error. No container was created, recreated, restarted, or torn down. |
| ER-001 to ER-004 (schema definitions) | carried forward | These records describe schemas defined inside this documentation package, not properties of the build tree. A `build.sbt` `organization` change cannot affect them. |
| ER-005 (destination-decision.yaml structural state) | re-verified | `docs/publication-readiness/destination-decision.yaml` was re-read at `4d286f7d`: `decision_status` is still `pending`, every `owners.*` is still `unassigned`, both `dates.*` are still `pending`, `scope`/`conditions` are still `pending`, `selected_destination.status` is still `unresolved`, and both `authorization.*` entries are still `not-selected`/`not-authorized`. The file's own internal `candidate_id` field still reads `b45c3ea2` and is now stale; that is recorded as ER-026 and is not corrected here, because the file lies outside this rebinding's authorized path set. |
| ER-006 (structural checker exists and is validated) | re-verified | `python3 scripts/publication-readiness/check_records.py` was re-run at `4d286f7d` after rebinding; see `## Candidate Rebinding Verification` below. |
| ER-007, ER-008 (GitHub fork provenance) | carried forward | Properties of GitHub repository metadata, not of the candidate tree; a local `organization` setting cannot affect them. Not re-queried. |
| ER-009 (blocked provenance/legal questions) | carried forward | Remains blocked and unowned; a coordinate change resolves nothing in it. Note the change moves the artifact namespace away from `io.getquill`, which is relevant context for the naming question but does not answer it; the record stays blocked. |
| ER-010 (module inventory, nine modules across four sets) | re-verified | `build.sbt` was re-read at `4d286f7d`: `baseModules` = `quill-sql`; `sqlTestModules` = `quill-sql-tests`; `dbModules` = `quill-jdbc`, `quill-doobie`, `quill-kyo`, `quill-jdbc-kyo`, `quill-caliban`; `bigdataModules` = `quill-cassandra`, `quill-cassandra-kyo`. Nine modules, unchanged. Mechanically re-confirmed by the checker's `aggregate module coverage` check, which reads `build.sbt` at the current candidate. |
| ER-011 (residual ZIO reference classification) | carried forward | Derived from Kyo-module source files, none of which differs between the two trees. Mechanically re-confirmed for structure by the checker's `residual ZIO reference classification` check. |
| ER-012 (three tracked `.bak`/`.bak2` files) | re-verified | Still tracked at `4d286f7d`; the delta touches no path under `quill-caliban/src/test`. Remains blocked, unowned, routed to WU9; not deleted, moved, or edited. |
| ER-013 (four tracked historical report files) | re-verified | Still tracked at `4d286f7d`; the delta touches none of them. Remains blocked, unowned, routed to WU9. |
| ER-014 (Tier 0 compile) | carried forward | Structured index row for VR-001; inherits VR-001's justification. |
| ER-015 (Tier 1 core test) | partially re-measured | Structured index row for VR-002; inherits VR-002's split basis. |
| ER-016 (sbt's pinned JDK is Homebrew Java 25.0.4) | re-verified | The sbt banner line `welcome to sbt 1.12.4 (Homebrew Java 25.0.4)` appears at line 1 of all three re-measured tier logs at `4d286f7d`. The finding holds unchanged. |
| ER-017 (triangulation and fixture rejection) | carried forward | Inherits VR-003 to VR-007's justification for the compile/package portion. The checker-fixture portion is a property of the checker, re-confirmed by the post-rebinding checker run below. |
| ER-018 (Tier 2 `db`) | re-measured | Structured index row for VR-008; re-executed as VR-011. |
| ER-019 (Tier 2 `bigdata`) | re-measured | Structured index row for VR-009; re-executed as VR-012. |

## Candidate Rebinding — Open Consequences of the Coordinate Change

Recorded as findings with unassigned owners and routed to their owning work
units. None is fixed by this rebinding; all remain open.

1. **Package namespace and publication coordinate now differ.** Every
   module still declares the Scala package `io.getquill` (re-verified at
   `4d286f7d`: `src/main/scala/io/getquill/` exists in all eight
   source-bearing modules, and `quill-kyo` sources declare
   `package io.getquill.context.qkyo`), while the publication coordinate is
   now `com.e-evolution`. Consumers will depend on one string and import
   another. Needs explicit consumer documentation. Routed to WU9 as ER-021.
2. **`README.md` advertises the superseded coordinate.** Re-verified at
   `4d286f7d`: lines 47, 49, 51, 53, and 55 advertise
   `"io.getquill" %% "quill-*" % "5.0.0"`, and line 42 declares
   `val kyoVersion = "1.0.0-RC5"` while the build itself is at Kyo
   `1.0.0-RC6` and version `5.0.0-kyo-RC6`. Routed to WU9 as ER-022. Not
   edited here.
3. **Locally installed artifacts under the old coordinate are orphaned.**
   Re-verified at `4d286f7d`: all eight modules have a
   `~/.ivy2/local/io.getquill/<module>_3/5.0.0-kyo-RC6/` directory, and
   `~/.ivy2/local/com.e-evolution/` does not exist. This build no longer
   produces the `io.getquill` coordinate, so those artifacts can no longer
   be reproduced from it, and a fresh local install under `com.e-evolution`
   is required before any consumer-resolution claim can be made. That
   install is WU7's work and was deliberately not performed here. Routed to
   WU7 as ER-023.
4. **`build.sbt` release metadata points at non-existent locations.**
   Re-verified at `4d286f7d`: `scmInfo` is
   `https://github.com/getkyo/kyo-protoquill` and `homepage` is
   `https://getkyo.io/kyo-quill`. An authenticated `gh api
   repos/getkyo/kyo-protoquill` returns HTTP 404 (`"message": "Not Found"`)
   and the repository's HTML page returns 404; `https://getkyo.io/kyo-quill`
   returns HTTP 404. Method note: an unauthenticated request to the same
   API endpoint returns 403 (rate limiting), which must not be mistaken for
   a 404; the 404 above is from the authenticated call. Routed to WU6 as
   ER-024. `build.sbt` was not edited.
5. **Untracked Python bytecode in a publication-readiness script
   directory.** Re-verified at `4d286f7d`: an untracked
   `scripts/publication-readiness/__pycache__/check_records.cpython-314.pyc`
   exists, and `.gitignore` contains no `__pycache__` or `*.pyc` entry.
   Python bytecode should be ignored before any source publication.
   Recorded as a cleanliness finding, ER-025. `.gitignore` was not edited.
6. **Three sibling readiness documents still bind the superseded
   candidate.** `docs/publication-readiness/destination-decision.yaml`
   carries `candidate_id: "b45c3ea2"` and
   `candidate_tree: "9c162c4f3d0d36c141517da6b61cfe21fe598dc8"`; its
   substantive fields were re-verified unchanged (see ER-005's basis row),
   but its candidate binding is stale. `provenance.md` and `modules.md`
   likewise still carry `b45c3ea2` in their own Candidate Binding tables,
   and `modules.md`'s inventory column header reads
   `verification_state (candidate b45c3ea2)`. All three lie outside this
   rebinding's authorized path set, so they are recorded rather than
   corrected. Recorded as ER-026 for their owning units. Note this is a
   real coverage gap in the structural checker: it compares only the
   Evidence Index and the Tier tables against the pin, so none of these
   three stale bindings fails a check today. The corresponding check is
   deliberately not added here, because it would fail against files this
   unit may not edit; adding it belongs with the fix.

## Candidate Rebinding Verification

- **Checker pin updated first**: `PINNED_CANDIDATE_ID` in
  `scripts/publication-readiness/check_records.py` was changed from
  `b45c3ea2` to `4d286f7d`. Every check's logic is unchanged. The one
  additional edit is the new
  `## Candidate Rebinding — Re-Measured Tier Evidence` heading appended to
  `VERIFICATION_TABLE_HEADINGS`, which brings VR-010 to VR-012 under the
  existing `verification record completeness` and
  `verification record staleness` checks; it adds no named check (the total
  stays 12) and weakens nothing.
- **RED (fixture-demonstrated, stated as what was actually run)**: the
  records were rebound in the same editing pass as the pin change, so no
  checker run was captured against the genuine intermediate state. Rather
  than assert an unobserved result, the RED state was demonstrated
  explicitly: copies of the rebound `evidence.md` and `verification.md`
  were written outside tracked paths under the session scratchpad with
  every `candidate_id` cell reverted to `b45c3ea2`, and
  `check_records.py --evidence-file <copy> --verification-file <copy>` was
  run against them. It exited `1`, with `evidence index stale references`
  reporting all 26 Evidence Index rows and `verification record staleness`
  reporting all 12 Tier rows as bound to `b45c3ea2` while carrying a
  non-`stale` status; the other 10 checks stayed `PASS`. The row counts are
  the post-rebinding counts (26 ER, 12 VR) because the fixture derives from
  the finished records, not the 19 ER / 9 VR the true intermediate state
  held. The copies were then deleted and were never staged, committed, or
  placed under a tracked path. This is the Freshness Rule firing on a real
  candidate change, reproduced faithfully.
- **GREEN**: after re-measuring the three tiers, rebinding the records, and
  adding VR-010 to VR-012 plus ER-020 to ER-026, the same command exited
  `0` with 12/12 checks `PASS`.
- **Focused verification command and result**:
  `python3 scripts/publication-readiness/check_records.py` — exit `0`,
  `RESULT: PASS`, 12/12 checks against the real records.
- **Runtime scenario and results**: the three tier commands recorded as
  VR-010, VR-011, and VR-012 above, with exit statuses `0`, `1`, and `0`
  and figures matching `b45c3ea2` exactly.
- **Checkout and excluded-path confirmation**: `git rev-parse HEAD` returns
  `4d286f7d9172f452fd3d0ae2458ef73c11376fea` and
  `git rev-parse HEAD^{tree}` returns
  `8924989ad8d3d676aff73c30ab93ad31878e7385`. `git status --short` shows
  only the pre-existing untracked excluded state plus this change's own
  untracked `docs/` and `scripts/publication-readiness/` deliverable
  directories; `build.sbt` shows no working-tree modification. No push,
  tag, release, artifact upload, or credential use occurred, and no
  `git add`, `git commit`, `git checkout`, `git reset`, `git clean`, or
  `git stash` was run. No local artifact install was performed. No service
  container was created, recreated, restarted, or torn down.

## Tier 3 — Local-Only Publication Dry Run and Consumer Checks (WU7)

RED was written before any generation command ran: all rows below carried
`status: blocked` and `TBD` placeholders, which the schema treats as the
visible block (the same pattern WU4/WU5 used) — the checker's placeholder
check exempts a `blocked`-status row by design, so `RESULT: PASS` at that
point reflected the mechanical checker only, not readiness; no Tier 3
production evidence existed yet. Upload-guard evidence (no credentials
file, no publication env vars, no signing key material, `sbt-ci-release`
present) was captured first, before any generation command ran; isolated
outputs live under the session scratchpad (`.../scratchpad/wu7/`), outside
tracked paths, referenced below by path and SHA-256 digest, and were never
staged or committed.

**Upload-guard evidence (captured before generation).** No file under
`~/.sbt` matches `credentials|sonatype` (one match was a stale build-log
artifact under `plugins/target/`, not a credentials file); `~/.sbt/1.0/*.sbt`
does not exist; `env | rg -i 'sonatype|pgp|gpg'` returned no matches; `gpg`/
`gpg2` are not installed on this machine at all (`command not found`), which
is a stronger signing-impossibility proof than "zero secret keys" — no
signing tool is present to hold or use a key. `project/plugins.sbt:9`
confirms `sbt-ci-release` 1.11.1 is installed, so the publication tasks
exist; what is absent is any credential, token, or signing key to run them.
No `publish`, `publishSigned`, `ci-release`, or `sonatypeBundleRelease` task
was invoked by this unit — only `publishLocal`.

## Tier 3 — Local Publication (Isolated `publishLocal`) Evidence

| id | subject | candidate_id | command | exit_status | result | status | severity | log_ref | disposition |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| VR-013 | `publishLocal` for all 8 published modules into a newly created, empty, isolated `sbt.ivy.home` directory (`wu7/isolated-ivy2-home/`, confirmed empty before this run) | 4d286f7d | `SBT_OPTS="-Xmx8g -Xss16m -Dsbt.ivy.home=<isolated>" sbt -batch "quill-sql/publishLocal" "quill-jdbc/publishLocal" "quill-doobie/publishLocal" "quill-kyo/publishLocal" "quill-jdbc-kyo/publishLocal" "quill-caliban/publishLocal" "quill-cassandra/publishLocal" "quill-cassandra-kyo/publishLocal"` | 0 | All 8 modules published POM+jar+sources+javadoc (quill-sql produced no javadoc jar, an up-to-date-cache artifact of a prior local `doc` run, not a scope error) under `<isolated>/local/com.e-evolution/<module>_3/5.0.0-kyo-RC6/`. `~/.ivy2/local/com.e-evolution` remained absent and `~/.ivy2/local/io.getquill`'s mtime (`May 16 18:06:03 2026`) was unchanged before and after, confirming the shared cache received zero writes. | stale | blocking | `wu7/logs/publishlocal-isolated.log` (141 lines, `sha256:639bad0b74c59c3b643ca9a13b9d00c7d375b15ca92074dbc05d638738172bad`) | GREEN. The false-green hazard (resolving from the orphaned shared `io.getquill` install) is structurally defeated: this run wrote only to the isolated directory. |

## Tier 3 — Artifact Identity, Dependency Scope, License, and Content Inspection

| id | subject | candidate_id | command | exit_status | result | status | severity | log_ref | disposition |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| VR-014 | Artifact identity: groupId/artifactId/version per generated POM, all 8 modules | 4d286f7d | `rg` of the groupId, artifactId, and version XML elements in each POM under `<isolated>/local/com.e-evolution/<module>_3/5.0.0-kyo-RC6/poms/<module>_3.pom` | 0 | All 8 POMs declare `groupId=com.e-evolution`, `artifactId=<module>_3`, `version=5.0.0-kyo-RC6`, matching `release-manifest.yaml`'s `fields.coordinates`/`fields.version.observed` exactly; no module mismatched. | stale | advisory | `wu7/logs/artifact-digests.txt` (SHA-256 of all 16 generated POM/jar files) | GREEN. Identity confirmed against the WU6 manifest, not merely self-consistent. |
| VR-015 | Dependency scope inspection: compile vs test across all 8 generated POMs; test-scope JDBC drivers must not leak into compile scope | 4d286f7d | Python `xml.etree.ElementTree` parse of each POM's `<dependencies>`, printing `(scope, groupId:artifactId)` per entry | 0 | All 6 JDBC drivers (`mysql-connector-j`, `h2`, `postgresql`, `sqlite-jdbc`, `mssql-jdbc`, `ojdbc8`) are `scope=test` in every module that declares them (`quill-jdbc`, `quill-doobie`, `quill-jdbc-kyo`; `quill-caliban` test-scopes `postgresql` only). Zero test-scope dependency appears without an explicit `<scope>test</scope>` element (default/no-element = compile). `io.getquill:quill-engine_3`/`quill-util_3` (4.8.5) appear compile-scoped in `quill-sql` only — legitimate upstream Quill dependencies, not a defect. `HikariCP`, `doobie-core`, `caliban-quick`, `java-driver-core`, and the four `kyo-*` artifacts are compile-scoped as expected runtime library dependencies. | stale | blocking | `wu7/logs/dependency-scope-audit.txt` (full per-module scope table, reproduced above) | GREEN. No compile-scope leak found; this closes the specific hazard named in this unit's task text. |
| VR-016 | License metadata inspection in generated POMs (license, organization URL, SCM) | 4d286f7d | `rg` of `<licenses>`, `<organization>`, `<scm>`, `<developers>` blocks across all 8 POMs | 0 | All 8 declare `Apache License 2.0` / `http://www.apache.org/licenses/LICENSE-2.0`, matching `build.sbt`/`release-manifest.yaml#fields.license`; `<developers>` lists only `deusaquilus`, matching ER-009's attribution gap. `<organization>`/`<scm>` reproduce the dead `https://getkyo.io/kyo-quill` homepage and dead `https://github.com/getkyo/kyo-protoquill` SCM connection recorded at ER-024, confirming those already-blocking `build.sbt` values propagate unchanged into every generated POM. | stale | blocking | same digests as VR-014 | Not a new defect: cross-references the already-blocking ER-024 (dead SCM/homepage) and ER-009 (attribution). Confirms propagation into publishable artifacts, which had not been verified before this unit. Routed to WU6/WU9; `build.sbt`/`LICENSE.txt` not edited. |
| VR-017 | Archive contents inspection: jar file listing, all 8 modules | 4d286f7d | `unzip -l` per jar, checked for non-`io/getquill`/`META-INF` package roots | 0 | Every jar's package roots are exclusively `io/getquill/...` plus `META-INF/`; file counts range 11–1,450 across modules; no `.DS_Store`, `test/`, `__MACOSX`, `.git`, or absolute-path zip entries found in any of the 8 jars. | stale | advisory | same digests as VR-014 | GREEN. No unrelated file leaked into any archive. |
| VR-018 | Absolute filesystem path, credential, and secret scan across all 8 generated POMs and jars | 4d286f7d | `rg` over POM text for `/Users/`, `/private/`, `password`, `secret`, `token`, PEM headers; per jar, class-file strings piped through `rg` for the same patterns plus this machine's actual scratchpad/home paths | 0 | Zero matches in any of the 8 POMs or 8 jars. | stale | blocking | same digests as VR-014 | GREEN. No leaked local path or credential in any generated artifact. |

## Tier 3 — Consumer Resolution and Compile/Run Evidence

| id | subject | candidate_id | command | exit_status | result | status | severity | log_ref | disposition |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| VR-019 | Isolation baseline: a consumer fixture depending on `com.e-evolution %% quill-jdbc-kyo % 5.0.0-kyo-RC6` resolved with the default, unmodified `~/.ivy2` (no isolated repository referenced) must FAIL, proving the shared cache and Maven Central both hold nothing under the new coordinate | 4d286f7d | `sbt -batch run` from `wu7/consumer-fixture/` with plain `SBT_OPTS`, no `sbt.ivy.home` override | 1 | `sbt.librarymanagement.ResolveException: Error downloading com.e-evolution:quill-jdbc-kyo_3:5.0.0-kyo-RC6` citing `not found: /Users/e-Evolution/.ivy2/local/com.e-evolution/quill-jdbc-kyo_3/.../ivy.xml` (shared cache empty) and `not found: https://repo1.maven.org/maven2/com/e-evolution/...` (Maven Central 404, matching ER-031); network was reachable (a real 404 was returned, not a connection failure). | stale | blocking | `wu7/logs/consumer-baseline-default-ivy2.log` (46 lines, `sha256:138c0579f7594f86f417aaba7cb94bbb66ab03a0e8f5d3247cccec478b2e9174`) | GREEN as a negative control: this failure is required and proves the isolation the next row demonstrates is real, not accidental. |
| VR-020 | Primary consumer scenario: the same fixture resolved exclusively from the isolated `publishLocal` output; depends on `com.e-evolution %% quill-jdbc-kyo`, imports `io.getquill._` (from the transitively resolved `quill-sql`), builds a quoted query, prints its AST (compile + run), and separately declares a compile-only type reference to the public Kyo surface `io.getquill.context.qkyo.KyoJdbcContext[?, ?]` | 4d286f7d | `SBT_OPTS="-Xmx4g -Xss16m -Dsbt.ivy.home=<isolated>" sbt -batch run` from `wu7/consumer-fixture/` | 0 | Compiled and ran; stdout `WU7-CONSUMER-OK ast=querySchema("Person").filter(p => p.age > 18)`. The compile-only `KyoJdbcContext[?, ?]` type alias line compiled as part of the same single-source-file build; had it failed to resolve, the whole compile would have failed. Post-run: `~/.ivy2/local/com.e-evolution` still absent, `io.getquill`'s mtime unchanged — resolution came only from the isolated repository. Ran on `Homebrew Java 26.0.1` (this standalone fixture has no `.sbtopts` pin, unlike the main build's 25.0.4), incidentally showing the artifact is consumable across a JDK minor/major boundary above its `-release:25` target. | stale | blocking | `wu7/logs/consumer-isolated-success.log` (10 lines, `sha256:0fcc16334b56d6c7cb35e23bc96b3959e010873402f82522d85683c4b25dab15`) | GREEN. Proves both required facts in one bounded scenario: the `com.e-evolution`/`io.getquill` coordinate-namespace split (ER-021) is consumable in practice, and it resolves from the isolated repository, never the shared cache or the network (VR-019 already showed both of those hold nothing). |
| VR-021 | Triangulation: a second, differently shaped consumer fixture (`com.e-evolution %% quill-sql` only, no transitive Kyo module; a different case class `Product`/query filter than VR-020) resolved exclusively from the isolated repository | 4d286f7d | `SBT_OPTS="-Xmx4g -Xss16m -Dsbt.ivy.home=<isolated>" sbt -batch run` from `wu7/consumer-fixture-2-sql-only/` | 0 | Compiled and ran; stdout `WU7-CONSUMER-2-OK ast=querySchema("Product").filter(p => p.price > BigDecimal.apply(0))`. Post-run: shared `~/.ivy2/local/com.e-evolution` still absent, `io.getquill` mtime unchanged. | stale | advisory | `wu7/logs/consumer2-sql-only-isolated.log` (10 lines, `sha256:be0bac592a577e9eeb57c22011e372d56f9d8558b2be6c18ce817a9f201b79bb`) | GREEN. Confirms VR-020's result generalizes to a second, minimal dependency graph rather than being an artifact of one specific fixture. |

## WU7 Verification (Tier 3)

- **RED**: three Tier 3 headings (`## Tier 3 — Local Publication (Isolated
  `publishLocal`) Evidence`, `## Tier 3 — Artifact Identity, Dependency
  Scope, License, and Content Inspection`, `## Tier 3 — Consumer Resolution
  and Compile/Run Evidence`) were added to `check_records.py`'s
  `VERIFICATION_TABLE_HEADINGS` before any generation command ran; this
  file was then written with 9 blocking rows (VR-013 to VR-021) carrying
  `status: blocked` and `TBD` command/exit_status/result/log_ref.
  `python3 scripts/publication-readiness/check_records.py` returned `0`/
  15/15 `PASS` at that point, because a `blocked`-status row is exempt from
  the placeholder check by design (the same pattern WU4/WU5 established);
  the block itself, not a mechanical checker failure, was the visible RED
  state — no Tier 3 production evidence existed yet.
- **GREEN**: upload-guard evidence was captured first (no credentials file,
  no publication env vars, `gpg`/`gpg2` not installed, `sbt-ci-release`
  present). `publishLocal` for all 8 modules then ran against a freshly
  created, empty, isolated `sbt.ivy.home` directory (VR-013); the 8
  generated POMs and jars were inspected for identity, dependency scope,
  license metadata, archive contents, and secrets/absolute paths (VR-014 to
  VR-018); a consumer fixture resolved against the default `~/.ivy2` failed
  as required (VR-019), and the same fixture resolved against the isolated
  repository compiled and ran (VR-020). All 9 rows were filled with
  observed results and `status: current`. `check_records.py` exited `0`
  with 15/15 `PASS`.
- **TRIANGULATE**: a second consumer fixture with a different dependency
  graph (`quill-sql` only, no transitive Kyo module) and a different query
  shape (`Product`/`price` instead of `Person`/`age`) was resolved against
  the isolated repository and also compiled and ran (VR-021). Two
  fixture-rejection tests then targeted the new Tier 3 headings
  specifically, outside tracked paths under the session scratchpad: (1) a
  copy of this file with every Tier 3 `candidate_id` reverted to the
  superseded `b45c3ea2` while `status` stayed `current` —
  `check_records.py --verification-file <copy>` exited `1`, reporting
  exactly the 9 injected rows (VR-013 to VR-021) by ID via `verification
  record staleness`, with 0 false positives against the real file's other
  rows; (2) a copy with VR-021's `exit_status` cell changed to `TBD` while
  `status` stayed `current` — the same command exited `1`, reporting
  exactly `VR-021: column 'exit_status' is still 'TBD' while
  status='current'` via `verification record completeness`. Both copies
  were deleted immediately after and were never staged, committed, or
  placed under a tracked path.
- **REFACTOR**: no procedure duplication was introduced — VR-014, VR-017,
  and VR-018 share one digest-inventory log reference (`artifact-digests.
  txt`) instead of three separate files, and the two blocking findings that
  are not new defects (VR-016's license/SCM propagation, matching ER-024/
  ER-009) are cross-referenced to their owning Evidence Index rows rather
  than restated as fresh findings. No generated artifact, fixture, or log
  was committed or staged; every WU7 output lives under the session
  scratchpad (`wu7/isolated-ivy2-home/`, `wu7/consumer-fixture/`,
  `wu7/consumer-fixture-2-sql-only/`, `wu7/logs/`) and is referenced here
  only by path and SHA-256 digest.
- **Focused verification command and result**:
  `python3 scripts/publication-readiness/check_records.py` — exit `0`,
  `RESULT: PASS`, 15/15 checks passing against the real records (13 prior
  WU2-WU6 checks unmodified plus the same 2 verification checks now also
  covering the 3 new Tier 3 headings).
- **Runtime scenario and results**: VR-019 (baseline, exit `1`,
  `ResolveException` against both the shared ivy cache and Maven Central),
  VR-020 (isolated, exit `0`, compiled and ran, `WU7-CONSUMER-OK`), VR-021
  (isolated, second fixture, exit `0`, compiled and ran,
  `WU7-CONSUMER-2-OK`).
- **Excluded-path and checkout confirmation**: `git rev-parse HEAD` returned
  `4d286f7d9172f452fd3d0ae2458ef73c11376fea` unchanged before and after
  every command in this unit; `git status --short` showed only the
  pre-existing untracked `.codegraph/`, `pi-session-2026-05-16T23-45-09-
  146Z_...html`, `docs/`, and `scripts/publication-readiness/` paths
  (unchanged set from WU1-WU6); `build.sbt` showed no modification. No
  `git add`, `git commit`, `git checkout`, `git reset`, `git clean`, or
  `git stash` was run. This unit's forbidden and unauthorized surface —
  `publish`, `publishSigned`, `ci-release`, `sonatypeBundleRelease`, a
  Maven deploy task, a repository push, a tag, or a GitHub Release — was
  never run; only `publishLocal` and `sbt run` executed (against the
  isolated repository and, once, the default local repository as a
  negative control).

## Tier 4 — CI Verification vs. Publication Reachability (WU10)

Candidate: `4d286f7d` (tree `8924989ad8d3d676aff73c30ab93ad31878e7385`), matching `HEAD` before and after every command in this unit. Edit scope: `.github/workflows/` only, explicitly approved for this unit alone (tasks.md WU10); no `build.sbt` or Scala source was touched.

### Before/After Event Graph and Permission/Secret Matrix

| file | job | condition before | condition after | permissions (before → after) | publication secrets/tokens | reachable before: PR / push | reachable after: PR / push | notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ci.yml | release | `github.event_name != 'pull_request'` | `github.event_name == 'release'` | default (not declared) → `contents: read` (workflow-level) | `PGP_PASSPHRASE`, `PGP_SECRET`, `SONATYPE_PASSWORD`, `SONATYPE_USERNAME` | no / **yes (this unit's core RED finding)** | no / no | Secrets renamed from `GETQUILL_SONATYPE_TOKEN_*` to `SONATYPE_*` by the 2026-08-27 reconciliation, not by this unit; both name forms resolve to an empty string when the named repository secret does not exist via GitHub's `secrets.<NAME>` syntax (no workflow error), so the rename adds no new reachable path and no silently-unset-secret path — reachability is gated only by the `if:` condition fixed here. The third required fixture, an unauthorized release sub-event (action `created`, not `published`), is blocked earlier still, by this workflow's own `on.release.types: [published]` trigger filter, before any job `if:` is evaluated — unchanged by this fix. |
| ci.yml | build | (none — always runs) | (none — always runs) | default (not declared) → `contents: read` (workflow-level) | none | n/a (no publication command or secret reference; only `./build/build.sh`) | n/a | Unchanged; see the CI-Group Parity Record below. |
| release-drafter.yml | update_release_draft | (none — always runs) | (none — always runs) | default (not declared) → `contents: write` | `GITHUB_TOKEN` (draft creation only, never a Sonatype/PGP credential) | never (no pull_request trigger) / yes | never / yes (unchanged) | Least-privilege permission added; drafting a release is not itself sbt's `ci-release` task or a Sonatype/PGP secret reference, and a human must still explicitly publish the draft for `ci.yml`'s fixed `release` job to fire at all. |
| dependency-graph.yml | dependency-graph | (none — always runs) | (none — always runs) | `contents: write` (already explicit; required by `scalacenter/sbt-dependency-submission@v3`) | `GITHUB_TOKEN` (implicit, via the submission action) | never / yes | never / yes (unchanged) | Already minimal for its function; this unit's fix is a JDK-25 pin (`actions/setup-java@v4.7.1`, matching `ci.yml`'s `build` job) for build correctness, not a permission or reachability change. |
| scala-steward.yml | scala-steward | (none — always runs on schedule) | (none — always runs on schedule) | default (not declared, unchanged) | `SCALA_STEWARD_GITHUB_APP_*` via `github-app-auth-only: true` (a scoped GitHub App token) | never (no PR or push trigger) | never (unchanged) | Out of this unit's GREEN scope by design: fires only on a daily cron or manual dispatch, never a pull request or push; already the most restrictive option this action offers. |

### CI-Group Parity Record

The `build` job's `strategy.matrix.module` list (`sqltest`, `db`, `bigdata`) and its `./build/build.sh ${{ matrix.module }}` step invocation are byte-for-byte unchanged by this unit; only the `release` job's `if:` condition and the workflow-level `permissions:` block changed. CI execution semantics for all three module groups are therefore unaffected, so per this unit's own verification clause no `sqltest` rerun was required or performed; `./build/build.sh` itself was not invoked at all by this unit (see the orchestrator's operational note on its `docker compose down` teardown of the running service containers).

## Tier 4 — Publication Reachability Fixture Evidence

| id | subject | candidate_id | command | exit_status | result | status | severity | log_ref | disposition |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| VR-050 | RED baseline: static reachability against the original, unfixed `.github/workflows/` | 4d286f7d | `python3 scripts/publication-readiness/event_graph.py` | 1 | `[UNREACHABLE] pull_request`; `[REACHABLE] ordinary_push` — `ci.yml:release` via `if="${{github.event_name != 'pull_request'}}"`, command=True, secrets=[PGP_PASSPHRASE, PGP_SECRET, SONATYPE_PASSWORD, SONATYPE_USERNAME]; `[UNREACHABLE] unauthorized_release` | current | advisory | `wu10/red-event-graph.log` (6 lines, `sha256:fde531aae8231d6e259664c0e13b776ffaca7392273f70ae2be1397af200e6de`) | Historical RED baseline required by this unit's task text; closed by VR-052 below. |
| VR-051 | Fixture: pull_request, post-fix | 4d286f7d | `python3 scripts/publication-readiness/event_graph.py` | 0 | `[UNREACHABLE] pull_request` — unchanged from RED; a pull request was never the vulnerable path, since the original condition already excluded `pull_request` | current | blocking | `wu10/green-event-graph.log` (5 lines, `sha256:e6f70e80ad6eb3da237968da020518285cea62f7fd7fe0934354f45df9e82e75`) | GREEN. No publication-capable path reachable from a pull request. |
| VR-052 | Fixture: ordinary_push to master, post-fix | 4d286f7d | `python3 scripts/publication-readiness/event_graph.py` | 0 | `[UNREACHABLE] ordinary_push` — the `release` job's `if:` fix (`github.event_name == 'release'`) closes VR-050's finding | current | blocking | same log as VR-051 | GREEN. Closes this unit's core RED finding and the `ci.yml` portion of ER-044. |
| VR-053 | Fixture: unauthorized_release (action=created, not published), post-fix | 4d286f7d | `python3 scripts/publication-readiness/event_graph.py` | 0 | `[UNREACHABLE] unauthorized_release` — blocked at GitHub's own `on.release.types: [published]` trigger filter before any job `if:` is evaluated, both before and after this unit's fix | current | advisory | same log as VR-051 | GREEN. Confirms the trigger-level filter, not only the job-level condition, already excludes non-published release sub-events. |

## WU10 Verification (Tier 4)

- **RED**: `scripts/publication-readiness/event_graph.py` was written first and run against the real, unfixed `.github/workflows/`; it exited `1`, reporting `ordinary_push` reachable to sbt's `ci-release` task with all four Sonatype/PGP secrets in scope (VR-050), while `pull_request` and `unauthorized_release` were already unreachable. `check_records.py` (before its own WU10 extension existed) still exited `0` with 19/19 `PASS`, since no structural record was missing yet — the RED state here is the event-graph script's own exit `1`, exactly as this unit's task text requires ("record the failing result"), not a `check_records.py` failure.
- **GREEN**: the smallest dedicated `.github/workflows/` change was made: `ci.yml`'s `release` job condition changed from `github.event_name != 'pull_request'` to `github.event_name == 'release'` (the workflow's own `on.release.types: [published]` filter already restricts which release sub-events reach this point at all). Re-running the same command against the fixed files exited `0` with all three fixtures `[UNREACHABLE]` (VR-051 to VR-053). `check_records.py` was then extended with the new `event graph reachability` check (delegating to `event_graph.py`) and the `## Tier 4 — Publication Reachability Fixture Evidence` heading was added to `VERIFICATION_TABLE_HEADINGS`; the full suite exited `0` with 20/20 `PASS` (19 prior WU2-WU9 checks unmodified). No replacement publication workflow was added or enabled, and no build-coverage step was removed.
- **TRIANGULATE**: all three required fixtures (pull-request, ordinary-push, and unauthorized-release-event) were exercised (VR-051 to VR-053). A fourth, checker-validation-only mutation test proved the detection logic generalizes beyond `ci.yml`: a scratchpad copy of the fixed workflow set with an injected sbt's `ci-release`-task step and a `PGP_SECRET` reference appended to `release-drafter.yml` was run through `event_graph.py --workflows-dir <copy>` and correctly reported `ordinary_push` reachable via `release-drafter.yml:update_release_draft`. A second mutation test ran `check_records.py --workflows-dir <scratchpad copy of the original, unfixed ci.yml>` and confirmed the new `event graph reachability` check itself fails (exit `1`) against the pre-fix snapshot and passes against the real repository. Both scratchpad copies were deleted immediately after and were never staged, committed, or placed under a tracked path.
- **REFACTOR**: least-privilege `permissions:` blocks were added to `ci.yml` (`contents: read`, workflow-level) and `release-drafter.yml` (`contents: write`), replacing each workflow's previous implicit default; `dependency-graph.yml`'s existing `contents: write` was confirmed already minimal for `scalacenter/sbt-dependency-submission@v3` and left unchanged; `scala-steward.yml` was left unchanged (already the most restrictive option, GitHub-App-scoped). No duplication was introduced across the three edited files; each carries exactly one new permissions block or setup step. Build coverage did not change (CI-Group Parity Record above).
- **Focused verification command and result**: `python3 scripts/publication-readiness/check_records.py` — exit `0`, `RESULT: PASS`, 20/20 checks passing (19 prior WU2-WU9 checks unmodified plus the 1 new WU10 check).
- **Runtime scenario**: no `./build/build.sh sqltest` rerun was required or performed — CI execution semantics for `sqltest`/`db`/`bigdata` are unchanged (CI-Group Parity Record above), which is the explicit condition in this unit's own verification clause for skipping the rerun. `./build/build.sh` itself was not invoked at all by this unit.
    - **Excluded-path and checkout confirmation**: `git rev-parse HEAD` returned `4d286f7d9172f452fd3d0ae2458ef73c11376fea` unchanged before and after every command in this unit; `git status --short` shows the same pre-existing untracked `.codegraph/`, `pi-session-2026-05-16T23-45-09-146Z_...html`, `docs/`, and `scripts/publication-readiness/` paths (unchanged set from WU1-WU9) plus this unit's tracked modifications limited to exactly `ci.yml`, `release-drafter.yml`, and `dependency-graph.yml` under `.github/workflows/`; `build.sbt` and every Scala source file show no modification. No `git add`, `git commit`, `git checkout`, `git reset`, `git clean`, or `git stash` was run. This unit never used a publication or signing credential and never triggered a repository push, tag, or GitHub Release — the entire unit is static analysis plus three `.github/workflows/` edits.

## S05 Fresh Temurin Verification Foundation

This is an **open record foundation**, not runtime evidence. It may be completed only by fresh observation in a maintainer-supplied normal local clone. Historical Homebrew and earlier-clone observations remain stale and cannot be rebadged as fresh.

| field | required placeholder |
| --- | --- |
| candidate_ref | `master@4d286f7d9172f452fd3d0ae2458ef73c11376fea` |
| candidate_tree | `8924989ad8d3d676aff73c30ab93ad31878e7385` |
| repository_kind | `normal-local-clone` |
| clone_identity | `open` |
| freshness | `open` |
| os_arch | `open` |
| actual_sbt_jvm_vendor | `open` |
| actual_sbt_jvm_version | `open` |
| java_release | `open` |
| sbt_jvm_evidence | `open` |
| scala_version | `open` |
| sbt_version | `open` |
| commands | `open` |
| service_prerequisites | `open` |
| started_at_utc | `open` |
| completed_at_utc | `open` |
| result | `open` |
| totals | `open` |
| workspace_assumption | `open` |
| bounded_refs | `open` |
| disposition | `open` |
| owner | `unassigned` |
| approver | `unassigned` |

A future record must prove the actual sbt JVM from sbt-process evidence; shell `java` output alone is insufficient. Any missing, ambiguous, mismatched, or fabricated-fresh value remains unsatisfied and preserves `not-ready`.


## S06 Fresh Temurin Normal-Clone Observation

**Disposition: passed, fresh runtime observation.** This observation supports the
clean-clone and reproducible-verification evidence requirements but does not close
any blocker, assign an owner or approver, or change the aggregate `not-ready`
disposition.

| field | observed value |
| --- | --- |
| observation_id | `S06-Temurin-normal-clone-2026-08-30` |
| candidate_ref | `master@4d286f7d9172f452fd3d0ae2458ef73c11376fea` |
| candidate_tree | `8924989ad8d3d676aff73c30ab93ad31878e7385` |
| runtime_location | `/Users/e-Evolution/Develop/ai.ageticeos/kyo-protoquill-s06-normal-clone-4d286f7d` |
| repository_kind | independent normal local clone; `.git` and common-dir both `.git`; detached HEAD; zero remotes before and after every runtime command |
| authoritative_record_location | `/Users/e-Evolution/Develop/ai.ageticeos/kyo-protoquill` |
| os_arch | `Mac OS X 26.6.2 / aarch64` |
| JDK probe | `/usr/libexec/java_home -v 25` resolved `/Users/e-Evolution/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home`; `java.vendor=Eclipse Adoptium`; `java.vendor.version=Temurin-25.0.4+7`; Java release `25`; runtime `25.0.4+7-LTS` |
| process configuration | `JAVA_HOME` was set only for each SBT process; SBT received `-java-home /Users/e-Evolution/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home`; no persistent JVM setting was changed |
| actual SBT JVM | SBT banner in every SBT log: `sbt 1.12.4 (Eclipse Adoptium Java 25.0.4)` |
| scala_version | `3.8.4` |
| scala_version_evidence | Preserved attributable successful compile collector output `.s06-collector-outputs/sbt-compile.log` (`sha256:a608ec159c48c21fcacc3eadd1b57b38dfff68232fb29dcd10755b53bf554648`), line 35: `compiling 113 Scala sources to .../target/scala-3.8.4/classes ...`; this is the successful compile log, not the failed auxiliary JVM probe. |

| overall result | `passed`; compile and both core suites exited `0`; no publication-capable task ran |

### Command Evidence

| command location | command | started_at_utc | completed_at_utc | exit | totals / result | bounded output reference |
| --- | --- | --- | --- | --- | --- | --- |
| supplied normal clone | `/usr/libexec/java_home -v 25` followed by the resolved JDK `java -XshowSettings:properties -version` | 2026-08-30T04:10:50Z | 2026-08-30T04:10:50Z | 0 | 62 lines, 2,632 bytes; Eclipse Adoptium / Temurin 25.0.4+7 / release 25 proven | `.s06-collector-outputs/jdk-probe.log`; `sha256:66d5c119f889b528b1ccb6f328dbae423e2550b1cd7a4c86f1082b5ac41b7bd9` |
| supplied normal clone | `JAVA_HOME=<Temurin-home> sbt -java-home <Temurin-home> -batch 'show javaHome' 'show javaVersion' 'show scalaVersion'` | 2026-08-30T04:11:06Z | 2026-08-30T04:11:12Z | 1 | 58 lines, 2,144 bytes; banner proved Eclipse Adoptium Java 25.0.4; `show javaVersion` is not a valid sbt key, so this auxiliary probe is recorded as failed and is not treated as a build result | `.s06-collector-outputs/sbt-jvm-proof.log`; `sha256:70fe905fb731f46a5d47b01be61dc1aba1646cc5f6ec8c087ef06015473d144f` |
| supplied normal clone | `JAVA_HOME=<Temurin-home> sbt -java-home <Temurin-home> -batch compile` | 2026-08-30T04:11:29Z | 2026-08-30T04:11:49Z | 0 | 436 lines, 31,060 bytes; all selected modules compiled; success total 16 s | `.s06-collector-outputs/sbt-compile.log`; `sha256:a608ec159c48c21fcacc3eadd1b57b38dfff68232fb29dcd10755b53bf554648` |
| supplied normal clone | `JAVA_HOME=<Temurin-home> sbt -java-home <Temurin-home> -batch 'quill-sql/test; quill-sql-tests/test'` | 2026-08-30T04:12:00Z | 2026-08-30T04:13:00Z | 0 | 5,021 lines, 389,363 bytes; `quill-sql`: 274 succeeded / 0 failed / 0 ignored, 36 suites; `quill-sql-tests`: 668 succeeded / 0 failed / 1 ignored, 23 suites; success totals 19 s and 38 s | `.s06-collector-outputs/sbt-core-tests.log`; `sha256:aa5fde502e4c65d0dbccd9b4dffa638f045c2aab136c76ede86bc8d266ce3690` |

### Boundary Readback

The clone identity was rechecked before and after every command above and remained
an independent normal clone at the exact commit and tree with detached HEAD and no
remotes. No Git network command, ref/index mutation, clone lifecycle operation,
publishing task (`publish` or `publishLocal`), credential use, service operation,
The collector outputs remain only in the
supplied clone; no record or checker artifact was copied into it.

## S13 CI-Safety Static Analyzer

The static analyzer reads the six bounded synthetic fixtures and the four current
workflow files without executing GitHub Actions. It rejects ordinary-push
publication, pull-request publication, unauthorized-release publication, and
unscoped publication secrets; it accepts the published-release publication route.
It also records repository mutations independently: dependency submission is
repository mutation, not artifact publication. Current-workflow readback classifies
ordinary-push `dependency-submission` and `release-draft` mutations while finding no
ordinary-push, pull-request, or unauthorized-release artifact-publication route.
This is static evidence only: workflows remain read-only, `not-ready` and all seven
blockers remain open, and no secret, release, publication, remote, or delivery
authority is granted.

## S14 CI Workflow Correction Verification

The bounded correction makes the `ci.yml` release job require both the `release`
event and its `published` action, preserving the existing Temurin 25 setup and
`sqltest`/`db`/`bigdata` build matrix. `release-drafter.yml` remains a
repository mutation with its `contents: write` scope, not artifact publication.
`dependency-graph.yml` retains its required `contents: write` dependency
submission scope and Temurin 25 setup; its comment makes no unsupported claim
about fork-push history or workflow provenance. Static analysis reports no
artifact-publication route for pull request, ordinary push, or unauthorized
release, and one authorized published-release route. Runtime: N/A — the changes
only tighten a guard and correct workflow metadata/comments; build execution is
unchanged, so `./build/build.sh sqltest` was not run.

## S15 Final Verification

Static aggregation readback binds to `master@4d286f7d9172f452fd3d0ae2458ef73c11376fea`
and tree `8924989ad8d3d676aff73c30ab93ad31878e7385`. The only fresh runtime
evidence is the bounded normal-local-clone Temurin 25 observation (ER-065);
it does not close BL-006 or BL-007. Historical `be8826eb`, comparison
`b45c3ea2`, Homebrew OpenJDK, alternate checkouts, and invalidated inputs are
non-fresh. Runtime: N/A — this is a readback of captured evidence.

The checker rejects a missing blocker, stale false closure, Temurin auto-close,
authority-free closure, and a slice over 400 lines. It confirms exactly 15
product slices, `plan_only: true`, `mutation_authorized: false`, unresolved or
evidence-backed provenance, deferred JSON migration, and no delivery or
publication authority.

## S15 Cross-Reference Result

All seven blockers remain open with current/stale evidence, gap, owner,
approver, and required disposition. The aggregate is `not-ready`; no
owner, approver, sign-off, date, or authority was invented.
