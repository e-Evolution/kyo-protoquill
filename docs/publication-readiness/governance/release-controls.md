# Release Controls (Draft)

## Status

This is a **review draft** covering issue ownership, release
responsibilities, CI/release separation, approval evidence, and
abort/recovery ownership. It records findings; it changes no workflow
file. Structural CI remediation for anything found here is WU10's job, not
this unit's — this document is destination-independent governance
narrative, never a `.github/workflows/` edit.

## Issue Ownership

- No individual or team is confirmed as the issue triager for this fork.
  `docs/publication-readiness/destination-decision.yaml`'s
  `owners.repository_owner` and `owners.governance_approver` remain
  `unassigned`, and this draft names no substitute.
- The inherited `.github/ISSUE_TEMPLATE.md` and
  `.github/PULL_REQUEST_TEMPLATE.md` both mention `@getquill/maintainers`,
  a GitHub team in the upstream Quill organization this fork does not
  belong to; on a published fork that mention either fails to resolve or
  notifies the wrong maintainers. This is the same finding recorded in
  `docs/publication-readiness/governance/contributing.md`; remediation is
  routed to WU9, not performed here.

## Release Responsibilities

Two workflow paths in this repository are release-capable — that is, each
one can act on a repository event without a maintainer running a manual
publication command:

- **`sbt ci-release` in `.github/workflows/ci.yml` — unauthorized ER-054 evidence.** ER-054
  records that an ordinary push was reachable before the `release` job condition
  changed from `github.event_name != 'pull_request'` to
  `github.event_name == 'release'`, and is unreachable after that change. The
  cited workflow trigger remains limited to `types: [published]`; this draft
  grants no release authorization. `docs/publication-readiness/evidence.md`
  ER-033 records that no such credential currently exists on this machine,
  which is upload-guard evidence for local work, not a statement about
  what a future CI runner's secrets would contain.
- **`release-drafter/release-drafter@v6` in
  `.github/workflows/release-drafter.yml`.** It triggers `on: push` to
      `branches: ['master']` and uses `GITHUB_TOKEN`, so every ordinary push
      to `master` creates or updates a GitHub Release draft — a second
      publication-capable path distinct from the one above. No release-creation
      history or causal explanation is asserted here; creating one remains
      unauthorized, and the owner of this responsibility is unassigned.
- **`scala-steward-action@v2.75.0` in
  `.github/workflows/scala-steward.yml`.** It runs on a daily cron
  (`0 0 * * *`) and would open dependency-update pull requests
  automatically once published; the maintenance-ownership question for
  reviewing and merging those pull requests is recorded in
  `docs/publication-readiness/governance/support-maintenance.md` and is
  unassigned here as well.
    - This record does not attribute the current activation state of all
      automation solely to the fork never being pushed. The distinct triggers
      remain separately documented in `.github/workflows/ci.yml`,
      `.github/workflows/release-drafter.yml`, and
      `.github/workflows/scala-steward.yml` above.



## CI/Release Separation

- `docs/publication-readiness/evidence.md` ER-033/ER-034 records that
  local `publishLocal` evidence is isolated and never touched shared
  credentials; that is local-machine evidence, not a statement about the
  CI runner recorded above.
    - [ER-054](../evidence.md#evidence-index)'s static event-graph evidence establishes that `pull_request` was already unreachable to the `sbt ci-release` task and remains not authorized; ordinary push became unreachable after the `ci.yml` condition change. It does not resolve the separate push-triggered release-drafter finding or assign its owner; this document records that unresolved governance question and edits no workflow file itself.

- The candidate dependency-graph blob has no `actions/setup-java` step while
  the build requires `-release:25`. S03 records the path evidence and leaves
  inheritance, authorship, and any uncited amendment unresolved. This is a
  build-correctness/CI gap, not a publication-reachability one.

## Dependency-Graph Provenance (S03)

| classification | statement | citations |
| --- | --- | --- |
| verified-fact | `.github/workflows/dependency-graph.yml` is unchanged between the two comparison commits and its candidate blob contains no `actions/setup-java` step. | `.github/workflows/dependency-graph.yml`; `b45c3ea25a3de59b58329cf4c76e8938a1c7892c`; `4d286f7d9172f452fd3d0ae2458ef73c11376fea`; `ba9a124d344086013856cf37c02f2b18806d5273` |
| inference | The one targeted add for `.github/workflows/dependency-graph.yml` in history explains path presence but cannot establish a locally amended origin. | `.github/workflows/dependency-graph.yml`; `0162fd4705debcd92a418b80c76be932722d3abb`; `a6aad4b14c23c8244a14d59f72f18cf2e0dc8d81` |
| unresolved-question | CI/release controls retain the `.github/workflows/dependency-graph.yml` provenance blocker until a responsible owner supplies attributable origin or amendment evidence. | `.github/workflows/dependency-graph.yml`; `0162fd4705debcd92a418b80c76be932722d3abb`; `ba9a124d344086013856cf37c02f2b18806d5273` |

## Approval Evidence

- No release has ever been approved for this fork. `docs/publication-readiness/destination-decision.yaml`'s
  `decision_status` remains `pending`, and every `authorization.*` entry
  remains `not-selected`/`not-authorized`.
- `docs/publication-readiness/release-manifest.yaml`'s SemVer precedence
  finding (ER-028) is unresolved and carries no selected disposition; no
  release identity has approval evidence to record.

## Abort/Recovery Ownership

- No individual or team is confirmed as able to halt an in-flight release
  workflow run, revoke a mistakenly created GitHub Release draft, or
  recover from a partial publication. [ER-054](../evidence.md#evidence-index)
  establishes that ordinary push does not make `ci-release` reachable; no
  owner is confirmed for abort or recovery.
- Recovery for the reconciliation itself (unrelated to a future release)
  already has a recorded boundary: WU1's rollback boundary keeps the
  pre-rebase history reachable through tag `v5.0.0` and branch
  `update/upstream-sync-rc6`; that boundary does not extend to a future
  publication event and is cited here only for contrast, not as this
  topic's disposition.

## Governance Topic Matrix

| topic | status | owner | approver | evidence | gap | required disposition |
| --- | --- | --- | --- | --- | --- | --- |
| issue ownership | unresolved | unassigned | unassigned | ER-044 | issue-triage ownership and approval are not recorded | Keep unresolved; named owner and approver must approve release governance. |
| release responsibilities | unresolved | unassigned | unassigned | ER-044 | release responsibility ownership and approval are not recorded | Keep unresolved; named owner and approver must approve release governance. |
| ci/release separation | unresolved | unassigned | unassigned | ER-044 | CI/release separation sign-off ownership and approval are not recorded | Keep unresolved; named owner and approver must approve release governance. |
| approval evidence | unresolved | unassigned | unassigned | ER-044 | approval evidence ownership and approval are not recorded | Keep unresolved; named owner and approver must approve release governance. |
| abort/recovery ownership | unresolved | unassigned | unassigned | ER-044 | abort and recovery ownership and approval are not recorded | Keep unresolved; named owner and approver must approve release governance. |

## Reference Links

| path | description |
| --- | --- |
| .github/workflows/ci.yml | Source of the `sbt ci-release` unauthorized-reachability finding above. |
| .github/workflows/release-drafter.yml | Source of the push-triggered GitHub Release draft finding above. |
| .github/workflows/scala-steward.yml | Source of the daily-cron dependency-update automation finding above. |
| .github/workflows/dependency-graph.yml | Source of the missing JDK-25 pin finding above. |
| .github/ISSUE_TEMPLATE.md | Source of the `@getquill/maintainers` issue-ownership finding above. |
| .github/PULL_REQUEST_TEMPLATE.md | Source of the `@getquill/maintainers` issue-ownership finding above. |
| docs/publication-readiness/destination-decision.yaml | Source of the unassigned owner fields cited throughout this document. |
| docs/publication-readiness/release-manifest.yaml | Source of the unresolved SemVer precedence finding cited above. |
| docs/publication-readiness/evidence.md | Source of ER-028, ER-033, and ER-034 cited above. |

## S10 Authority-Control Readback

ER-054 confirms only the current `ci-release` reachability boundary; ER-065 is
fresh technical verification evidence and ER-066 is a plan-only comparison.
None assigns an owner, approver, destination, date, sign-off, release authority,
or publication authority. The destination decision remains `pending`, all
applicable owners remain `unassigned`, the authority sign-off and its effective
date remain `pending`, and every authorization remains `not-authorized`.
