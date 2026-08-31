# Contributing (Draft)

## Status

This is a **review draft** governance baseline, not the repository's live
root-level `CONTRIBUTING.md`. Both the fork (`e-Evolution/kyo-protoquill`)
and its parent (`zio/zio-protoquill`) have no `CONTRIBUTING.md` at any level
(read-only `git ls-tree` on both, 2026-08-27), so there is no upstream
template to inherit or adapt — this content is written from scratch. It is
destination-independent: it names no specific repository URL, contact
handle, or badge, and does not itself become the root-level file until the
owner/approver in the Governance Topic Matrix below resolve.

## Contribution Expectations

- Contributions are expected to reference an issue before a pull request,
  describe the change's scope, and include focused test coverage
  consistent with the project's `sbt test` conventions.
- Contributions are accepted under the same license already declared by the
  project (`LICENSE.txt`, Apache License 2.0); this draft does not restate
  or alter that license text.
- Pull requests are expected to pass the repository's existing CI checks
  (`.github/workflows/ci.yml`) before merge consideration. CI's role is
  verification only; it is never a publication path by itself — see
  `docs/publication-readiness/governance/release-controls.md` for the
  separate release-responsibility record covering the workflows that are
  publication-capable.
- No specific reviewer, maintainer contact, or response-time commitment is
  named here; see the Governance Topic Matrix below.

## Inherited Findings (not remediated here)

Two inherited artifacts under `.github/` name a foreign organization that
this fork does not belong to. Both are unedited by this draft; remediation
is routed to WU9:

- `.github/PULL_REQUEST_TEMPLATE.md` and `.github/ISSUE_TEMPLATE.md` both
  mention `@getquill/maintainers`, a GitHub team in the upstream Quill
  organization. On a published fork that mention either fails to resolve
  or notifies upstream Quill maintainers of this fork's activity.
- `.github/ISSUE_TEMPLATE.md` directs reporters to `getquill.io` and to an
  upstream scastie snippet by `fwbrasil` — upstream context, not this
  fork's.

## Governance Topic Matrix

| topic | status | owner | approver | evidence | gap | required disposition |
| --- | --- | --- | --- | --- | --- | --- |
| contribution expectations | unresolved | unassigned | unassigned | ER-040 | contribution ownership and approval are not recorded | Keep unresolved; named owner and approver must approve contribution governance. |

## Reference Links

| path | description |
| --- | --- |
| LICENSE.txt | Apache License 2.0 text already governing the repository; not restated here. |
| .github/PULL_REQUEST_TEMPLATE.md | Inherited template naming the foreign `@getquill/maintainers` team (see Inherited Findings). |
| .github/ISSUE_TEMPLATE.md | Inherited template naming the foreign `@getquill/maintainers` team and linking `getquill.io` (see Inherited Findings). |
| docs/publication-readiness/destination-decision.yaml | Destination decision gate; this draft's owner/approver fields resolve only after that record's owners act. |
| docs/publication-readiness/governance/release-controls.md | CI/release separation and release-responsibility record referenced above. |
