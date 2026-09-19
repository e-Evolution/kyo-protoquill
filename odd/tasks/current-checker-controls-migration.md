# Current Checker Controls Migration

## Objective

Migrate the still-open technical work from the F002, S10, and R03 OpenSpec plans into one current-state ODD feature while preserving every OpenSpec artifact unchanged as historical/native authority.

## Migration Decision

The user selected **copy and preserve**:

- ODD owns new current-state verification and any newly reproduced correction.
- `openspec/changes/rebaseline-f002-current-checker/**`, `openspec/changes/rebaseline-s10-publication-controls/**`, and `openspec/changes/recover-r03-checker-boundary/**` remain unchanged and open.
- ODD results do not backfill historical RED evidence, predecessor hashes, correction authority, review authority, publication authority, release authority, or delivery authority.
- Duplicate technical concerns are deduplicated in ODD rather than implemented twice.

## Source Backlog Mapping

| Historical pending item | ODD disposition |
|---|---|
| F002 Task 8 B07-02/B07-03 authorization gate | Adopt the already completed current-state behaviors from commits `499a54cc` and `748278bb`; verify them without changing the historical AF68 chain. |
| S10 Tasks 3.1–3.4 correction slice | Merge with R03 F-002 because both describe the same positive-authorization predicate; correct only if current verification reproduces a defect. |
| S10 Tasks 4.1–4.2 verification/report | Replace with an ODD current-state verification record in this document; do not create the historical OpenSpec report. |
| R03 pending F-002 chain | Merge with the S10 correction concern; preserve the unavailable historical RED/predecessor proof as unavailable. |
| R03 Task 7.1 recovery report | Replace with an ODD migration status in this document; do not create `evidence/recovery-report.json`. |

## Current Boundary

- Parent branch: `fix/checker-integrity-corrections`.
- Parent commit: `e4d0a9b09594e7f3a087f7be56225a0842f1ccc3`.
- Child branch: `odd/current-checker-controls-migration`.
- Current checker SHA-256 at migration start: `5d9a3620b89c784d038d0c863c2e6107bdb81679518a7de75544b0c2f605bf34`.
- Static inspection indicates all three pending behaviors are implemented.
- The S10 positive-authorization cases are not trustworthy coverage yet: their test-looking statements are unreachable inside an exception context, and a nearby S10 test is a nested local function rather than a registered class test.

## Scope

### Writable

- `odd/tasks/current-checker-controls-migration.md`
- `scripts/publication-readiness/check_records.py` only for the bounded S10 test-registration repair, unless a focused current-state RED proves a production defect.

### Read-only

- The three source OpenSpec change trees and all evidence beneath them.
- The five S10 control inputs:
  - `docs/publication-readiness/destination-decision.yaml`
  - `docs/publication-readiness/release-manifest.yaml`
  - `docs/publication-readiness/reconciliation.md`
  - `docs/publication-readiness/governance/release-controls.md`
  - `scripts/publication-readiness/check_records.py`

### Excluded

- `.git/gentle-ai/**`, `.codegraph/`, `pi-session-*.html`, S11–S15, accepted historical evidence, Git remotes, workflows, publication, release, and delivery.
- Push, pull request creation, merge, tag, or remote mutation without later explicit authorization.

## Delivery and Review Workload

- Delivery strategy: `auto-chain`.
- Chain strategy: `feature-branch-chain`.
- Dependency: this child branch is based on the completed checker-integrity branch; review it against `e4d0a9b0`, not against the default branch.
- Forecast for this child slice: 120–250 authored changed lines, including the ODD record and bounded test repair.
- Budget: at most 400 authored additions plus deletions for this child slice.

## Tasks

- [ ] **ODD-MIG-01 — Adopt the historical backlog into ODD**
  - Status: in progress.
  - Record the exact pending items and their ODD dispositions.
  - Preserve the source OpenSpec trees unchanged.
  - Establish the child-branch boundary and review budget.
  - Close with a documentation work-unit commit.

- [ ] **ODD-MIG-02 — Restore a trustworthy S10 focused test boundary**
  - Status: pending.
  - Observe structural RED by proving the intended class-level positive-authorization test is absent from `unittest.defaultTestLoader`.
  - Remove the dead nested/local S10 test structure and register one explicit class-level test for `authorized`, `ready`, `publishable`, and positive source-publication authorization with incomplete prerequisites.
  - Do not change production behavior unless the registered focused test produces a genuine behavioral RED.
  - Run the focused test, full checker self-test, and structural diff checks.
  - Obtain independent read-only verification because native assessment has been unavailable.
  - Close with one test-boundary work-unit commit.

- [ ] **ODD-MIG-03 — Verify the migrated current-state controls**
  - Status: pending.
  - Verify S09 document-path semantic validation, duplicate flattened-YAML rejection, and S10 positive-authorization rejection through registered focused tests and direct probes.
  - Run the normal checker, complete self-test, and event-graph harness.
  - Record current SHA-256/size facts for the five S10 inputs without copying their contents.
  - Confirm the three OpenSpec trees remain unchanged from the parent boundary.
  - Record historical unknowns and absent authorities explicitly.

- [ ] **ODD-MIG-04 — Reconcile and publish the ODD migration status**
  - Status: pending.
  - Record work-unit commits, final authored-line count, exact tracked scope, protected exclusions, and verification results.
  - Mark technical ODD tasks complete only from current-state evidence.
  - Leave all native/OpenSpec checkboxes and reports untouched and open.
  - Close with a final documentation work-unit commit.

## Acceptance Criteria

- The pending F002, S10, and R03 technical concerns have one unambiguous ODD disposition each.
- `unittest.defaultTestLoader` registers focused class-level tests for S09 semantic validation, duplicate YAML keys, and S10 positive authorization states.
- Current behavior rejects invalid S09 records, duplicate normalized YAML keys, and positive authorization states with incomplete prerequisites.
- Current valid blocked publication-readiness documents continue to pass.
- The normal checker, full self-test, and event graph pass.
- Only this task document and the bounded checker test region change on the child branch unless a genuine new defect is reproduced.
- The child slice remains within 400 authored lines.
- The three OpenSpec trees, `.git/gentle-ai/**`, `.codegraph/`, and the session HTML remain untouched.
- No historical recovery, native ledger closure, review, publication, release, or delivery authority is claimed.

## Verification Plan

- Structural registration probe: load `RecoveryContractTests` through an importable module name and inspect `unittest.defaultTestLoader.getTestCaseNames`.
- Focused tests: run the three registered current-state boundaries with `unittest.defaultTestLoader`.
- Normal checker: `PYTHONDONTWRITEBYTECODE=1 python3 scripts/publication-readiness/check_records.py`.
- Full self-test: `PYTHONDONTWRITEBYTECODE=1 python3 scripts/publication-readiness/check_records.py --self-test`.
- Event graph: `PYTHONDONTWRITEBYTECODE=1 python3 scripts/publication-readiness/event_graph.py`.
- Structural checks: `git diff --check`, exact name-status/numstat against `e4d0a9b0`, and path-limited comparison of the three OpenSpec trees.
- Runtime harness: the normal checker and event graph are the runtime boundaries for these standalone controls.
- Rollback boundary: the explicit S10 class-level test and removal of its dead nested/unreachable predecessor structure; no production hunk is expected.

## Progress

- 2026-09-19: User selected ODD migration by copying pending work while preserving OpenSpec.
- 2026-09-19: Read-only mapping found one F002 gate, six S10 checkboxes, one R03 report checkbox, and one R03 progress-only F-002 chain.
- 2026-09-19: Deduplicated S10 correction work and R03 F-002 into one current authorization-predicate boundary.
- 2026-09-19: Static inspection found the production predicate already fail-closed but its intended positive-state regression cases unreachable.
- 2026-09-19: Created child branch `odd/current-checker-controls-migration` at parent `e4d0a9b0` to keep the new review slice below 400 lines.

## Next Step

Commit ODD-MIG-01, then repair and independently verify the S10 focused test registration without changing production behavior unless a real current-state failure is reproduced.
