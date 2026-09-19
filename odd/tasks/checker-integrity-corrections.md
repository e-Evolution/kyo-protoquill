# Checker Integrity Corrections

## Objective

Harden the current publication-readiness checker so it rejects semantically invalid S09 document evidence and duplicate flattened-YAML keys, while preserving all accepted historical evidence and leaving native S10/R03 lineage blockers untouched.

## Problem

The current checker still has two independently audited defects:

1. S09 document validation checks required markers but does not invoke the existing semantic S09 validation path.
2. `flat_yaml` silently overwrites duplicate keys.

The existing R03 recovery lineage and S10 baseline are not current authority for these corrections. The user selected ODD-only execution, so this feature changes current product code and in-file tests only; it does not rewrite or close active OpenSpec/native ledgers.

## Why

Fail-open evidence parsing can allow structurally plausible but invalid readiness records to pass. Correcting the current checker reduces that risk without making unsupported historical or authority claims.

## Scope

### In scope

- `scripts/publication-readiness/check_records.py`
- `docs/publication-readiness/evidence.md` only to declare the S09 record representation and an explicit current empty-record policy; historical evidence rows remain unchanged.
- In-file `RecoveryContractTests` coverage for both defects.
- This ODD continuity document.
- Two reviewable work-unit commits, one per behavior.

### Out of scope

- Any mutation under `.git/gentle-ai/**`.
- Any mutation under `openspec/changes/**` or archived publication-readiness evidence.
- Resolving or reclassifying the native R03 recovery lineage.
- Reusing the stale S10 checker baseline.
- Publication, release, push, pull request, or remote workflow execution.
- `.codegraph/`, `pi-session-*.html`, `.sbtopts`, S11–S15, or accepted AF68/R03 evidence.

## Constraints

- Route: ODD-only, explicitly selected by the user.
- Strict TDD applies because all three active task plans define it for this checker.
- TDD runner: `PYTHONDONTWRITEBYTECODE=1 python3 scripts/publication-readiness/check_records.py --self-test`.
- RED must be observed before each production correction; GREEN and REFACTOR evidence must be reported from actual commands.
- Existing invalid nested/unreachable test registration must be repaired only as needed to create a trustworthy class-level focused boundary.
- One writer thread; no parallel writers in this checkout.
- Preserve accepted evidence bytes and native provider state.
- Review workload forecast: approximately 180–320 authored changed lines, below the 400-line advisory budget.
- Delivery strategy: `ask-on-risk`; no chain strategy is currently required.
- Receipt-driven development status: unknown at feature creation; native assessment will determine verification/review routing after each work-unit commit.

## Tasks

- [x] **ODD-CHK-01 — Enforce semantic S09 document validation**
  - Status: completed.
  - Route: delegated direct writer.
  - Trigger: the corrected implementation spans the checker and its documented S09 record representation, so the multi-file write rule requires one bounded writer.
  - Add a valid class-level focused test that proves a marker-complete but semantically invalid S09 document is rejected.
  - Observe RED before changing production validation.
  - Parse every explicitly declared S09 slice record from the document and reuse the existing semantic `validate_s09_slice_record` path; do not duplicate its rules or supply safe hardcoded defaults.
  - Declare the record representation and explicit current empty-record policy without altering historical evidence rows or fabricating a completed slice.
  - Exercise actual record fields for exactly-400/401 accounting, protected paths, stale-link propagation, delivery authority, mutation-capable instructions, missing/malformed fields, and duplicate S09 representation sections.
  - Reach GREEN, run the full self-test suite, then refactor without behavior drift.
  - Commit as one work unit with tests and this document.
  - Evidence: strict TDD captured semantic and duplicate-section REDs before their corrections. Writer and independent checks passed 2 focused tests, 54 full tests, 38/38 validations, structural checks, exact scope review, and byte-identical historical Evidence Index comparison. The required parent spot check reran the full harness with the same passing totals. Native assessment remained `unassessable`, so the independent verifier was the risk-directed check. Work-unit commit: `499a54cc1c42398c17c9553e56e32057d7ab2f9f` (`fix: validate declared S09 slice records`), authored product/docs delta `+166/-1=167`.

- [ ] **ODD-CHK-02 — Reject duplicate flattened-YAML keys**
  - Status: in progress.
  - Route: delegated direct writer.
  - Trigger: strict-TDD implementation and in-file test-boundary work benefit from the same bounded writer pattern.
  - Add a valid class-level focused test for duplicate-key rejection.
  - Observe RED before changing `flat_yaml`.
  - Reject duplicates deterministically with a useful error; do not silently overwrite.
  - Reach GREEN, run the full self-test suite, then refactor without behavior drift.
  - Commit as one work unit with tests and this document.
  - Evidence pending: focused RED/GREEN, full self-test, runtime harness classification, commit, native risk/outcome.

- [ ] **ODD-CHK-03 — Reconcile final ODD state**
  - Route: parent reconciliation plus risk-directed verification.
  - Confirm only authorized product/task-document paths changed.
  - Confirm protected/untracked artifacts remain untouched.
  - Record final verification and authored-line totals.
  - Record that F002/S10/R03 native ledgers remain open where provider authority is required.

## Acceptance Criteria

- Exactly one S09 record-representation section is allowed; duplicate sections fail closed.
- Every explicitly declared S09 slice record is parsed from the document; absence is allowed only through the documented explicit empty-record policy.
- Marker-complete but semantically invalid S09 slice records fail for line budget, protected paths, stale-link propagation, delivery authority, mutation-capable instructions, and missing/malformed fields.
- A semantically valid S09 document retains its current accepted behavior.
- Duplicate keys in flattened YAML fail deterministically instead of overwriting earlier values.
- Both new focused tests are registered at class level and can be run independently.
- The complete Python self-test suite passes after each work unit.
- No file under `openspec/changes/**`, `.git/gentle-ai/**`, `.codegraph/`, or the session HTML artifact is modified.
- The two behavior changes are captured as reviewable Conventional Commits on `fix/checker-integrity-corrections`.
- S10 and R03 remain explicitly blocked rather than falsely closed.

## Verification Plan

### ODD-CHK-01

- Focused tests: class-level `RecoveryContractTests.test_s09_document_requires_semantic_validation` plus `RecoveryContractTests.test_s09_document_path_validates_declared_slice_records` using `unittest.defaultTestLoader`.
- Full check: `PYTHONDONTWRITEBYTECODE=1 python3 scripts/publication-readiness/check_records.py --self-test`.
- Runtime harness: the checker self-test executable is the runtime boundary for this standalone validation behavior.
- Rollback boundary: the S09 focused tests/parser/semantic invocation in `scripts/publication-readiness/check_records.py` plus only the S09 record-representation/empty-policy text added to `docs/publication-readiness/evidence.md`.

### ODD-CHK-02

- Focused test: class-level `RecoveryContractTests.test_flat_yaml_rejects_duplicate_keys` using `unittest.defaultTestLoader`.
- Full check: `PYTHONDONTWRITEBYTECODE=1 python3 scripts/publication-readiness/check_records.py --self-test`.
- Runtime harness: the checker self-test executable is the runtime boundary for parser behavior.
- Rollback boundary: the duplicate-key focused test and duplicate detection in `flat_yaml`.

## Progress

- 2026-09-19: Reconciled repository state and memory. Three active OpenSpec plans remain: F002 31/32, S10 3/9, and R03 15/16.
- 2026-09-19: Verified B07-02 and B07-03 remain real current-code defects.
- 2026-09-19: User selected ODD-only execution, authorizing a feature branch and work-unit commits while preserving native R03/S10 blockers.
- 2026-09-19: Created branch `fix/checker-integrity-corrections` from `e9b45dec7e6104ff8a157fc0ae2e899c991f0665`.
- 2026-09-19: Started ODD-CHK-01; durable local and Engram copies were reconciled before source work.
- 2026-09-19: The delegated writer added only the class-level test, then correctly stopped when the parent-supplied runpy harness failed on `<run_path>` before reaching the intended RED. No production code changed.
- 2026-09-19: Independent read-only diagnosis loaded the checker under an importable module name and observed the intended semantic RED (`None` instead of `slice cannot grant delivery authority`); ODD-CHK-01 proceeded to GREEN.
- 2026-09-19: The writer added a document-level S09 semantic invocation; focused GREEN passed, the full checker self-test passed 53 tests and 38/38 validations, and only the authorized tracked file changed.
- 2026-09-19: Native risk assessment returned `unassessable` because native assess produced empty output. Receipt-driven development is off, so the task was routed to an independent verifier as high risk.
- 2026-09-19: Independent verification passed the focused/full commands but rejected closure: the code parses only narrative delivery authority and hardcodes every other slice field, while the current S09 document contains no populated record. B07-02 remains open.
- 2026-09-19: The task scope was corrected to add an explicit S09 record representation/empty policy in `docs/publication-readiness/evidence.md` and document-path tests for every audited constraint class; existing evidence rows remain immutable.
- 2026-09-19: The bounded correction observed RED against the surrogate, then implemented fenced `s09-slice` parsing plus exact empty-sentinel semantics. Writer checks passed 2 focused tests, 54 full tests, 38/38 validations, and diff checks; only the two authorized tracked files changed.
- 2026-09-19: Repeated native assessment again returned `unassessable` with empty native output, so the corrected candidate remained routed to independent reverification.
- 2026-09-19: Independent reverification passed 2 focused tests, 54 full tests, 38/38 validations, structural checks, and a byte-identical Evidence Index comparison, but found a static duplicate-section bypass: a later S09 section after a level-two heading is ignored. ODD-CHK-01 remained open.
- 2026-09-19: The follow-up writer observed duplicate-section RED, required exactly one global S09 heading, then passed 2 focused tests, 54 full tests, 38/38 validations, and structural checks. Total product/docs diff is 166 insertions and 1 deletion.
- 2026-09-19: Native assessment after the final correction again returned `unassessable`, preserving the independent-verification requirement.
- 2026-09-19: Final independent verification closed B07-02 with no remaining bounded finding; all requested checks passed and the historical Evidence Index remained byte-identical.
- 2026-09-19: Parent spot check reran the full self-test (54 tests; 38/38 validations), then committed ODD-CHK-01 as `499a54cc1c42398c17c9553e56e32057d7ab2f9f`.
- 2026-09-19: Started ODD-CHK-02; the remaining current-code defect is duplicate flattened-YAML key overwrite.

## Verification Evidence

- ODD-CHK-01 test-only diff: `scripts/publication-readiness/check_records.py | 21 +++++++++++++++++++++`.
- Structural check: `git diff --check -- scripts/publication-readiness/check_records.py` passed.
- Initial focused RED attempt: rejected because harness setup failed with `ValueError: invalid format: '<run_path>'`.
- Corrected importable-module focused RED: 1 test, exit 1, `AssertionError: None != 'slice cannot grant delivery authority'`; accepted as intended behavioral RED evidence.
- Independent structural recheck: `git diff --check -- scripts/publication-readiness/check_records.py` passed.
- Writer focused GREEN: 1 test, `OK`.
- Writer full/runtime harness: 53 tests, `OK`; document validations 38/38.
- Writer product diff: `scripts/publication-readiness/check_records.py` has 40 insertions and 1 deletion.
- Native assessment: `unassessable` (`native-assess-unavailable`, empty native output); independent verification required by the returned plan.
- Independent candidate checks: focused test passed, full self-test passed 53 tests with 38/38 validations, and `git diff --check` passed.
- Independent requirement result for the surrogate: incomplete. Budget, protected-path, stale-link, and instruction values were hardcoded safe values; only narrative delivery authority was parsed.
- Corrective RED: declared invalid record fields were ignored and returned `None` instead of semantic errors.
- Corrected writer focused GREEN: 2 tests, `OK`.
- Corrected writer full/runtime harness: 54 tests, `OK`; document validations 38/38.
- Corrected product/docs diff: 159 insertions and 1 deletion across the two authorized files; `git diff --check` passed.
- Repeated native assessment: `unassessable` (`native-assess-unavailable`, empty native output); independent reverification required.
- Independent corrected-candidate checks: 2 focused tests passed; 54 full tests and 38/38 validations passed; `git diff --check` passed; Evidence Index content is byte-identical to `HEAD`.
- Independent corrected-candidate result before follow-up: blocked because a duplicate later `### S09 Slice Records` section could escape validation.
- Duplicate-section RED: `AssertionError: None != 'multiple S09 slice record representations'`.
- Duplicate-section writer GREEN: 2 focused tests passed; full self-test passed 54 tests with 38/38 validations.
- Final pre-verification diff: 166 insertions and 1 deletion across two authorized files; `git diff --check` passed.
- Post-correction native assessment: `unassessable` (`native-assess-unavailable`, empty native output); final independent verification required.
- Final independent result: completed; B07-02 resolved with 2 focused tests, 54 full tests, 38/38 validations, exact scope, and byte-identical Evidence Index history.
- Parent spot check: `PYTHONDONTWRITEBYTECODE=1 python3 scripts/publication-readiness/check_records.py --self-test` passed 54 tests and 38/38 validations.
- Work-unit commit: `499a54cc1c42398c17c9553e56e32057d7ab2f9f`; product/docs authored lines `166+1=167`.

## Next Step

Persist the completed ODD-CHK-01 record, then delegate ODD-CHK-02. Add the duplicate-key test first, observe RED against `flat_yaml`, implement deterministic rejection, and rerun focused/full checks.
