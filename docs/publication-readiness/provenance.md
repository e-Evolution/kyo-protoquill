# Publication Readiness — Provenance

## Purpose

Classified, dated, candidate-bound evidence for repository provenance: the
GitHub fork relationship, ProtoQuill lineage, inherited Apache-2.0 material,
a bounded history comparison, and Quill-lineage dependencies. Follows the
evidence record schema and severity rules in `evidence.md`. This document
does not resolve copyright, notice, naming, trademark, or upstream-acceptance
questions — those stay blocked pending a named owner (see "Blocked
Questions" below).

## S04 Boundary Classification

The retained provenance observations are **not fresh** for this amendment. Their candidate and
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
| Scope | WU3 — provenance, fork relationship, module/residual-ZIO inventory |

## GitHub Fork Relationship (verified-fact)

Queried against the GitHub repository API, 2026-08-27:

| Repository | `fork` | `parent` | `source` | License | Archived |
| --- | --- | --- | --- | --- | --- |
| `e-Evolution/kyo-protoquill` | `true` | `zio/zio-protoquill` | `zio/zio-protoquill` | Apache-2.0 | no |
| `zio/zio-protoquill` | `false` | `null` | `null` | Apache-2.0 | no |

Both repositories are public; `e-Evolution/kyo-protoquill`'s default branch
is `master`. (ER-007)

## ProtoQuill Lineage — Two Distinct Statements

These are two separate facts and must not be conflated:

1. **This repository IS a GitHub fork.** `e-Evolution/kyo-protoquill` has
   `fork: true` with `parent`/`source` = `zio/zio-protoquill`. This is
   GitHub-recorded fork metadata, not an inference. (ER-007)
2. **`zio/zio-protoquill` is NOT itself a GitHub fork of `zio/zio-quill`.**
   It reports `fork: false`, `parent: null`, `source: null`. Its
   relationship to Quill is code lineage — shared authorship, technique, and
   a historical description in `README.md` of Kyo Quill as "originally based
   on ProtoQuill" — not GitHub fork metadata. (ER-008)

This closes the item proposal.md §2.1 listed as "inference requiring
maintainer confirmation": the repository is accurately described as a
GitHub fork of `zio/zio-protoquill`, while `zio/zio-protoquill`'s own
relationship to `zio/zio-quill` remains code lineage only.

## Inherited Apache-2.0 Material (verified-fact)

- `LICENSE.txt` contains the Apache License 2.0 text and a copyright notice
  for Flavio Brasil.
- `build.sbt` declares `licenses := List(("Apache License 2.0", ...))`.
- Apache-2.0 is compatible in principle with redistribution and
  modification of the inherited ProtoQuill/Quill lineage code, but the
  specific copyright, notice, and attribution obligations for a public
  release are not resolved by this observation alone (see "Blocked
  Questions").

## Bounded History Comparison (verified-fact)

- Candidate `4d286f7d` ("build: change groupId from io.getquill to
  com.e-evolution") is local `master`, 33 commits ahead of `origin/master`
  and 0 behind. It sits directly on `b45c3ea2` ("build: bump Kyo to
  1.0.0-RC6 and version to 5.0.0-kyo-RC6"), which was the candidate at the
  time this document was first written and which was 32 ahead / 0 behind.
- `merge-base(master, upstream/master)` equals the `upstream/master` tip
  `76803759`, proving `master` is a direct descendant of the current
  `zio/zio-protoquill` upstream with no unintegrated upstream commits.
- The net content delta from the previously released `acc60185` to
  `b45c3ea2` is four files: `build.sbt`, `project/plugins.sbt`,
  `.github/workflows/ci.yml`, and `.github/workflows/dependency-graph.yml`.
  S03 records the workflow's bounded Git evidence below; it does not infer
  authorship or local origin. All Scala source is byte-for-byte identical
  across that delta.

  dependency/CI version bumps (sqlite-jdbc, mysql-connector-j,
  logback-classic, sbt-scoverage, postgresql, zio-json, HikariCP, zio,
  scala3-library, a dependency-graph Action) plus the `dev.zio` Sonatype
  namespace change, which does not apply to this fork (this fork keeps the
  `io.getquill` namespace). Full detail: `proposal.md` §2.1 and the Rescope
  Record.
- This comparison is bounded to the reconciliation delta already recorded
  under WU1; it is not a full commit-by-commit authorship audit.

## Quill-Lineage Dependencies (verified-fact)

The build retains an active technical dependency on the Quill lineage:

| Dependency | Coordinates | Version | Consuming module(s) |
| --- | --- | --- | --- |
| `quill-engine` | `io.getquill %% quill-engine` | `4.8.5` (`zioQuillVersion`) | `quill-sql` |
| `quill-util` | `io.getquill %% quill-util` | `4.8.5` (`zioQuillVersion`) | `quill-sql` |
| `zio-json` | `dev.zio %% zio-json` | `zioJsonVersion` (`0.8.0`) | `quill-jdbc-kyo` (JSON codec only) |

This is the only ZIO-origin runtime dependency retained by a Kyo module's
main source, and it is scoped to a JSON codec (see `modules.md`'s Residual
ZIO Reference Classification). It is not evidence of an incomplete Kyo
effect migration.

## Blocked Questions (unresolved-question — do not resolve here)

The following remain explicitly blocked pending a named, attributable
owner. This document records them; it does not close them.

| Question | Owner needed | Status |
| --- | --- | --- |
| Copyright notice completeness for a public release | Maintainer/legal reviewer | blocked, `unassigned` |
| Attribution/notice wording for inherited ProtoQuill/Quill code | Maintainer/legal reviewer | blocked, `unassigned` |
| Use of the name "Kyo Quill" and existing Quill Maven coordinates | Project/ecosystem maintainers | blocked, `unassigned` |
| Trademark implications of retained `io.getquill` namespace and "Quill" naming | Maintainer/legal reviewer | blocked, `unassigned` |
| Upstream acceptance of any portion of the 32 local commits | `zio/zio-protoquill` maintainers | blocked, `unassigned` |

(ER-009)

## Publication-Cleanliness Findings (blocking — routed to WU9, not resolved here)

Two categories of tracked files were discovered during this inventory that
were not present in the original proposal's blocker list. Both are
recorded as blocking findings with an unassigned disposition owner; neither
is deleted, moved, or edited by this work unit.

1. **Tracked backup files that would ship in a public source publication.**
   Three `.bak`/`.bak2` files are tracked in git under `quill-caliban`:
   `quill-caliban/src/test/scala/io/getquill/CalibanSpec.scala.bak`,
   `quill-caliban/src/test/scala/io/getquill/example/CalibanExample.scala.bak`,
   `quill-caliban/src/test/scala/io/getquill/example/CalibanExample.scala.bak2`.
   No disposition has been made for these; the decision (remove, or retain
   with justification) is routed to WU9. (ER-012)
2. **Tracked historical reports describing a superseded era.** Four
   historical report files are tracked at repository root —
   `FINAL_STATUS_REPORT.md`, `MIGRATION_PLAN.md`, `MIGRATION_REPORT.md`,
   `VALIDATION_REPORT_FINAL.md` — describing the superseded Kyo RC1 /
   Scala 3.8.1 / JDK 17 era. `FINAL_STATUS_REPORT.md` is the origin of a
   false "1,171 tests passing" figure that does not match current RC6
   evidence. The maintainer has previously decided to keep these files as
   historical records; the remaining public-claims remediation decision
   (how to qualify or disclaim the stale figures for a public reader) is
   routed to WU9. (ER-013)

## Archive Reference Inventory (S01)

`docs/publication-readiness/archive-reference-ledger.md` records the sole in-scope
former active-change path found in the archived publication-readiness Markdown. The
row is `intentionally-historical`: the original rollback statement accurately
expresses the proposal's active-state context, while the ledger supplies its resolving
archived target. No archived artifact was edited, and this inventory does not change
the `not-ready` disposition or any of the seven open blockers.

## Dependency-Graph Provenance (S03)

| classification | statement | citations |
| --- | --- | --- |
| verified-fact | `b45c3ea25a3de59b58329cf4c76e8938a1c7892c` and `4d286f7d9172f452fd3d0ae2458ef73c11376fea` both contain mode `100644`, blob `ba9a124d344086013856cf37c02f2b18806d5273` at `.github/workflows/dependency-graph.yml`; their parent/tree comparisons show no path change. | `.github/workflows/dependency-graph.yml`; `b45c3ea25a3de59b58329cf4c76e8938a1c7892c`; `4d286f7d9172f452fd3d0ae2458ef73c11376fea`; `ba9a124d344086013856cf37c02f2b18806d5273` |
| inference | Targeted history attributes an add (`A`) to `.github/workflows/dependency-graph.yml` in `0162fd4705debcd92a418b80c76be932722d3abb`; continuity through both baselines supports retention, not a claim of local origin. | `.github/workflows/dependency-graph.yml`; `0162fd4705debcd92a418b80c76be932722d3abb`; `a6aad4b14c23c8244a14d59f72f18cf2e0dc8d81`; `bf247b976f2f84d86a83d30dc48c81a1e94fa0cf` |
| unresolved-question | Repository objects do not establish whether `.github/workflows/dependency-graph.yml` was inherited, who authored it, or why prior readiness text described an unobserved JDK-25 amendment. | `.github/workflows/dependency-graph.yml`; `0162fd4705debcd92a418b80c76be932722d3abb`; `ba9a124d344086013856cf37c02f2b18806d5273` |

## Verification

See the WU3 evidence block
checker cycle and the manual source-link readback confirming every
repository-relative path cited in this document and in `modules.md`
exists at candidate `4d286f7d`. Runtime scenario: `N/A` — this document is
inventory only.
