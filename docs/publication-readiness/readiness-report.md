# Publication Readiness Report

## Purpose

Aggregates WU1-WU11 evidence into one candidate-bound readiness report, per
tasks.md WU12 and spec.md's "Readiness Status Is Evidence-Based and
Preserves Workspace State" requirement. Status is derived below, never
asserted: this report does not, and cannot, conclude the package is ready
for publication.

## Candidate Binding

| Field | Value |
| --- | --- |
| `candidate_id` | `4d286f7d` |
| `candidate_tree` | `8924989ad8d3d676aff73c30ab93ad31878e7385` |
| `captured_at` | 2026-08-27 |
| Scope | WU12 only — aggregates WU1-WU11 evidence; adds no new source or verification evidence of its own beyond ER-061 to ER-064 below |
| Full `HEAD` (confirmed before and after this unit) | `4d286f7d9172f452fd3d0ae2458ef73c11376fea`, unchanged |

## Scope: Source-Publication Preparation vs. Maven Central and GitHub Release

This change prepares only destination-independent evidence and the proposed
source-publication destination `e-Evolution/kyo-protoquill` (GitHub
parent/source `zio/zio-protoquill`, ER-007). `destination-decision.yaml`'s
`selected_destination.source_publication.state` is `selected` but
`.authorized` is `not-authorized`; `decision_status` stays `pending`
(ER-059). Maven Central and GitHub Release remain `not-selected` and
`not-authorized` in both `selected_destination.*` and `authorization.*`;
neither this change nor the 2026-08-27 Rescope Record grants Sonatype,
signing, or GitHub Release authority. The local `5.0.0-kyo-RC6` ivy
publication (WU1) and the isolated `publishLocal` dry run (WU7) are local
evidence only, never a release. Any future Maven Central or GitHub Release
proposal requires a separate SDD change and separate authorization.

## Requirement and Claim Coverage Matrix

Every `### Requirement:` heading in `specs/publication-readiness/spec.md`
and every `CC-*` id in `evidence.md`'s `## Public Claim Coverage` table has
exactly one row below. `status` is `open-blocking`, `open-advisory`, or
`closed`, derived from the cited evidence's own `severity`/ownership state,
never asserted independently of it.

| item | evidence_id | owner | approver | candidate_id | freshness | status | severity | disposition |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| REQ: Provenance Evidence Is Accurate and Reviewable | ER-009 | unassigned | unassigned | 4d286f7d | current | open-blocking | blocking | Copyright/notice/naming/trademark review is unresolved (ER-009); tracked `.bak`/`.bak2` test fixtures remain a routed cleanliness finding (ER-012). Historical-report mislabeling was already corrected (ER-052), but no named maintainer/legal owner has approved the provenance package. |
| REQ: Current Kyo Migration State Is Evidenced | ER-011 | unassigned | unassigned | 4d286f7d | current | closed | advisory | All nine `build.sbt` modules are inventoried (ER-010) and every discovered residual ZIO reference is classified with zero actionable migration debt (ER-011). The deferred `zio-json` codec migration is explicitly out of this change's scope and does not reopen this requirement. |
| REQ: Verification Is Reproducible and Candidate-Bound | ER-061 | unassigned | unassigned | 4d286f7d | stale | open-blocking | blocking | Tier 0-4 records (ER-014..019, ER-032..039, ER-053..058) are complete, candidate-bound, and freshness-checked, but are local-machine evidence only; no clean-checkout CI run exists for `4d286f7d` (ER-061). The pre-existing Oracle `DistinctJdbcSpec` failure (ER-018) is correctly classified product/pre-existing, not a fresh regression. |
| REQ: Publication Documentation and Governance Are Complete | ER-044 | unassigned | unassigned | 4d286f7d | stale | open-blocking | blocking | All governance topics across `contributing.md`, `security.md`, `code-of-conduct.md`, `support-maintenance.md`, and `release-controls.md` (ER-040..044) remain `disposition_type: unresolved` with `owner`/`approver: unassigned`; no waiver is recorded for any topic. |
| REQ: Release Metadata Has One Consistent Manifest | ER-028 | unassigned | unassigned | 4d286f7d | current | open-blocking | blocking | The manifest is internally consistent and fully reconciled (ER-027), but `fields.version`, `fields.tag`, `fields.coordinates`, `fields.readme_coordinates`, `fields.changelog`, `fields.homepage`, `fields.scm`, `fields.license`, and `semver_precedence` in `release-manifest.yaml` all remain `state: unresolved`, `severity: blocking` (ER-028, ER-029, ER-024, ER-031). |
| REQ: CI Separates Verification From Publication | ER-054 | unassigned | unassigned | 4d286f7d | current | open-blocking | blocking | The `release`-job reachability gap is structurally closed and proven closed by the static event-graph fixtures (ER-054, ER-058); the CI-safety property now holds, but no named owner/approver has signed off in `release-controls.md`'s still-`unresolved` CI/release-separation topic. |
| REQ: Destination Decision Is Explicit and Blocking | ER-059 | unassigned | unassigned | 4d286f7d | current | open-blocking | blocking | `decision_status` remains `pending`; source publication is `selected` but `not-authorized`; all seven owner fields and both dates remain `unassigned`/`pending` (ER-059); Maven Central and GitHub Release remain `not-selected`/`not-authorized`. |
| REQ: Readiness Status Is Evidence-Based and Preserves Workspace State | ER-062 | unassigned | unassigned | 4d286f7d | current | closed | advisory | This report implements the requirement directly: overall status is derived below, never asserted, and the final excluded-path/`build.sbt` reconfirmation against WU1 (ER-062) found no mismatch. The mechanism functions correctly; product readiness remains blocked by the seven rows above. |
| CC-001 | ER-047 | unassigned | unassigned | 4d286f7d | current | closed | advisory | `README.md` `kyoVersion` corrected `1.0.0-RC5` -> `1.0.0-RC6`. |
| CC-002 | ER-047 | unassigned | unassigned | 4d286f7d | current | closed | advisory | `README.md` install coordinates corrected to `com.e-evolution` / `5.0.0-kyo-RC6`. |
| CC-003 | ER-047 | unassigned | unassigned | 4d286f7d | current | closed | advisory | "Preserving all Quill functionality" claim qualified to the measured Verification Status section. |
| CC-004 | ER-048 | unassigned | unassigned | 4d286f7d | current | closed | advisory | Coordinate/import-namespace split newly documented in `README.md`. |
| CC-005 | ER-048 | unassigned | unassigned | 4d286f7d | current | closed | advisory | Measured `sqltest`/`db`/`bigdata` test-result summary added for public readers. |
| CC-006 | ER-049 | unassigned | unassigned | 4d286f7d | current | closed | advisory | `README.md` Requirements section Kyo version corrected `1.0.0-RC5` -> `1.0.0-RC6`. |
| CC-007 | ER-049 | unassigned | unassigned | 4d286f7d | current | closed | advisory | Stale `4.8.8` JDK-17 fallback claim corrected against measured Maven Central metadata. |
| CC-008 | ER-050 | unassigned | unassigned | 4d286f7d | current | closed | advisory | `CHANGELOG.md` dead release-notes link replaced with an accurate no-GitHub-Release statement. |
| CC-009 | ER-050 | unassigned | unassigned | 4d286f7d | current | closed | advisory | `CHANGELOG.md` `Unreleased` entry added for the RC6 bump and groupId change. |
| CC-010 | ER-051 | unassigned | unassigned | 4d286f7d | current | closed | advisory | `.github/ISSUE_TEMPLATE.md` foreign-organization mention and dead link removed. |
| CC-011 | ER-051 | unassigned | unassigned | 4d286f7d | current | closed | advisory | `.github/PULL_REQUEST_TEMPLATE.md` foreign-organization mention removed. |
| CC-012 | ER-052 | unassigned | unassigned | 4d286f7d | current | closed | advisory | `FINAL_STATUS_REPORT.md` banner strengthened; false "1,171 tests passing" figure named and corrected. |
| CC-013 | ER-052 | unassigned | unassigned | 4d286f7d | current | closed | advisory | `MIGRATION_PLAN.md` banner strengthened to name the superseded RC1/Scala 3.8.1/JDK 17 era. |
| CC-014 | ER-052 | unassigned | unassigned | 4d286f7d | current | closed | advisory | `MIGRATION_REPORT.md` banner strengthened to name the superseded era. |
| CC-015 | ER-052 | unassigned | unassigned | 4d286f7d | current | closed | advisory | `VALIDATION_REPORT_FINAL.md` banner strengthened to name the superseded era. |

## Blocking and Advisory Summary

- **7 of 8 specification requirements remain `open-blocking`**: Provenance,
  Verification-Reproducibility, Governance, Release-Metadata,
  CI-Separation-ownership-signoff, and Destination-Decision. Every one of
  them lacks a named owner and approver on its controlling record — the
  common root cause across this entire package, not seven unrelated gaps.
- **1 requirement (Current Kyo Migration State) and all 15 public claims are
  `closed`/advisory**: the RC6 migration inventory and every WU9 claim
  correction were completed and mechanically verified.
- **1 requirement (Readiness Status Is Evidence-Based) is `closed`/advisory**:
  this report itself is the satisfying mechanism, and its own excluded-path
  reconfirmation (below) found no mismatch.
- No blocking record anywhere in this package (ER-001 through ER-064) names
  an owner or an approver. Per `evidence.md`'s Derived Readiness States, a
  blocking-severity record that lacks required ownership/approval keeps
  overall status `not-ready` regardless of whether its underlying finding
  was itself technically fixed (for example ER-054's reachability fix).

## Excluded-Path and Checkout Reconfirmation (Final, vs. WU1)

| Item | WU1 preflight record | This unit (2026-08-27) | Match |
| --- | --- | --- | --- |
| `.codegraph/` | untracked, unmodified | untracked, unmodified (`git status --short`) | yes |
| `pi-session-2026-05-16T23-45-09-146Z_019e332d-f01a-7def-b488-5bc856c8bb20.html` | untracked, unmodified | untracked, unmodified (`git status --short`) | yes |
| `build.sbt` | committed under the Rescope Record; no further edit authorized by this change | no modification shown by `git status --short -- build.sbt` | yes |
| Local tag `v5.0.0` | annotated tag object resolving to commit `acc601856664f237a4f0244127fc7e630692839e`, not an ancestor of `master` | unchanged commit target; `git merge-base --is-ancestor v5.0.0 master` still reports not-an-ancestor | yes |
| `origin/master` divergence | 32 ahead / 0 behind at the reconciled candidate | 33 ahead / 0 behind at `4d286f7d` — exactly one additional commit, the groupId change already recorded by the rebinding | consistent, no unexpected divergence |
| Local and remote refs | none created or moved | read-only `git branch --list -a` and `git ls-remote` re-inspection: no unexpected local or remote ref found; full `HEAD` unchanged throughout WU2-WU12 | yes |

No mismatch was found. Per this unit's own instructions, any disagreement
above would be reported as a blocking invalidation rather than repaired
automatically; none was required.

## Review-Line Accounting (`feature-branch-chain`, 400-line budget)

| Unit | Measured authored lines | Slices | Within 400/slice | `size:exception` claimed |
| --- | --- | --- | --- | --- |
| WU1 | 0 documentation lines (history reconciliation; rebased content is measured separately in the review snapshot) | 1 | yes | no |
| WU2 | 560 | 2a (229) + 2b (331) | yes, both slices | no |
| WU3 | 381 | 1 | yes | no |
| WU4 | 261 | 1 | yes | no |
| WU5 | 175 | 1 | yes | no |
| WU6 | 381 | 1 | yes | no |
| WU7 | 140 | 1 | yes | no |
| WU8 | 506 | 8a (143) + 8b (363) | yes, both slices | no |
| WU9 | 230 | 1 | yes | no |
| WU10 | 337 | 1 | yes | no |
| WU11 | 38 | 1 | yes | no |
| WU12 | recorded in this unit's own Work Unit Evidence block in `tasks.md` | 1 | yes | no |

Every proposed PR slice measures within the 400-authored-line
`feature-branch-chain` budget; WU2 and WU8 each split into two child slices
under the maintainer-approved `auto-chain` decision. No work unit has ever
claimed `size:exception`.

## Overall Readiness Status

Status: not-ready

At least one `open-blocking` row maps to seven of the eight specification
requirements above, and none of the underlying blocking records names an
owner or approver. Per `evidence.md`'s Derived Readiness States, this
package is therefore **not-ready**. `destination-gated` and
`ready-for-destination-specific-planning` both require every
destination-independent blocker to be closed first, which has not
occurred; they are not reached. `ready-to-publish` is deliberately never
produced by this change. No blocker above has been closed or explicitly
accepted by an authorized maintainer with recorded authority, rationale,
scope, and date.

## Verification (RED/GREEN, WU12)

- **RED**: `check_records.py` was extended first with the
  `readiness report requirement and claim coverage` check — parsing every
  `### Requirement:` heading from `specs/publication-readiness/spec.md` and
  every `CC-*` id from `evidence.md`'s `## Public Claim Coverage` table —
  and this file was added to `document candidate binding`'s scanned paths,
  before this file existed. `python3 scripts/publication-readiness/check_records.py`
  exited `1`: the new check and `document candidate binding` both reported
  `missing file` for `readiness-report.md`; all 20 prior checks stayed
  `PASS` unmodified.
- **GREEN**: after creating this file with the full 8-requirement / 15-claim
  coverage matrix and the `Overall Readiness Status` section, the same
  command exited `0` with 21/21 `PASS`.
- **Focused verification command and result:** `python3 scripts/publication-readiness/check_records.py`
  — exit `0`, `RESULT: PASS`, 21/21 checks passing (20 prior WU2-WU11 checks
  unmodified plus this unit's 1 new check).
- **Runtime scenario:** `N/A` — this unit aggregates already-recorded
  runtime evidence (Tier 0-4 in `verification.md`); no new application
  runtime boundary applies to a coverage-matrix aggregation unit.
- **Rollback boundary:** this file and the final cross-links added to
  sibling `docs/publication-readiness/` documents only, per this unit's own
  Rollback boundary in `tasks.md`.

## S15 Final Fail-Closed Aggregation

The final disposition is **not-ready**. Closure requires fresh attributable
technical evidence **and** an authorized owner/approver decision; neither is
inferred from a successful command, static analysis, or this aggregation.

| blocker | status | current/stale evidence | gap | owner | approver | required disposition |
| --- | --- | --- | --- | --- | --- | --- |
| BL-001 provenance | open | ER-056 current | workflow origin remains unresolved | unassigned | unassigned | Keep unresolved pending attributable provenance decision. |
| BL-002 reproducibility | open | ER-065 current; ER-061 stale | no CI/clean-checkout record | unassigned | unassigned | Obtain fresh attributable CI or accepted-risk decision. |
| BL-003 governance | open | ER-067 current | governance ownership and approval absent | unassigned | unassigned | Named owner and approver must authorize disposition. |
| BL-004 release metadata | open | ER-028 stale | manifest fields remain unresolved | unassigned | unassigned | Resolve metadata with authorized decision. |
| BL-005 CI separation | open | ER-054, ER-070 current | technical guard lacks authorized sign-off | unassigned | unassigned | Record authorized CI/release decision. |
| BL-006 Temurin verification | open | ER-065 current, fresh | technical success is not closure authority | unassigned | unassigned | Fresh evidence plus authorized decision required. |
| BL-007 destination | open | ER-059 current | destination remains pending/not-authorized | unassigned | unassigned | Authorized destination decision required. |

Temurin 25 success supports BL-006 and may inform BL-007, but cannot
auto-close either blocker. `be8826eb`, `b45c3ea2`, Homebrew OpenJDK,
alternate checkouts, and invalidated inputs remain non-fresh. Provenance is
evidence-backed where cited and otherwise unresolved; the deferred `zio-json`
to Kyo Schema/JSON codec migration remains excluded.

## S15 Product-Slice Cross-Reference

| slice | budget | exclusions | authority | evidence |
| --- | --- | --- | --- | --- |
| S01 | <=400 | protected state | not-authorized | ER-001 |
| S02 | <=400 | protected state | not-authorized | ER-002 |
| S03 | <=400 | protected state | not-authorized | ER-056 |
| S04 | <=400 | protected state | not-authorized | ER-061 |
| S05 | <=400 | protected state | not-authorized | ER-065 |
| S06 | <=400 | protected state | not-authorized | ER-065 |
| S07 | <=400 | protected state | not-authorized | ER-066 |
| S08 | <=400 | protected state | not-authorized | ER-060 |
| S09 | <=400 | protected state | not-authorized | ER-063 |
| S10 | <=400 | protected state | not-authorized | ER-054 |
| S11 | <=400 | protected state | not-authorized | ER-067 |
| S12 | <=400 | protected state | not-authorized | ER-068 |
| S13 | <=400 | protected state | not-authorized | ER-069 |
| S14 | <=400 | protected state | not-authorized | ER-070 |
| S15 | <=400 | protected state | not-authorized | ER-071 |

Remediation history remains evidence, not additional product slices. The
publication comparison remains `plan_only: true` and `mutation_authorized:
false`; protected state and delivery/publication authority are untouched.
