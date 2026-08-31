# Support and Maintenance (Draft)

## Status

This is a **review draft** governance baseline, not a root-level `SUPPORT.md`.
Neither the fork nor its parent `zio/zio-protoquill` has a `SUPPORT.md` at
any level, so there is no upstream template to adapt.

## Support and Maintenance Boundaries

- Support is best-effort only; no service-level agreement, response-time
  commitment, or guaranteed maintenance cadence is made by this draft,
  because no owner has confirmed one.
- Module-level maintenance boundaries follow the inventory already recorded
  in `docs/publication-readiness/modules.md`: modules with a passing
  verification record (`docs/publication-readiness/verification.md`) are
  the actively exercised surface; modules without one carry no maintenance
  claim beyond what that inventory states.
- Issue triage — who looks at an incoming issue first and how it is
  routed — is recorded as its own topic in
  `docs/publication-readiness/governance/release-controls.md` ("issue
  ownership") rather than duplicated here.
- The Quill-lineage dependency table in
  `docs/publication-readiness/provenance.md` records the two compile-scope
  `io.getquill` dependencies (`quill-engine`, `quill-util`) this project
  still relies on; maintenance of those dependencies is external to this
  repository and is not a boundary this draft can set.
- Dependency freshness is partially automated today by
  `.github/workflows/scala-steward.yml` (a daily cron opening
  dependency-update pull requests); who reviews and merges those pull
  requests once the fork is published is recorded as an open,
  `unassigned` maintenance question, not resolved here.

## Governance Topic Matrix

| topic | status | owner | approver | evidence | gap | required disposition |
| --- | --- | --- | --- | --- | --- | --- | --- |
| support and maintenance boundaries | unresolved | unassigned | unassigned | ER-043 | support and maintenance ownership and approval are not recorded | Keep unresolved; named owner and approver must approve maintenance governance. |

## Reference Links

| path | description |
| --- | --- |
| docs/publication-readiness/modules.md | Module inventory and verification-state basis for the maintenance boundary above. |
| docs/publication-readiness/provenance.md | Quill-lineage dependency table cited above. |
| docs/publication-readiness/verification.md | Verification records that define which modules are actively exercised. |
| docs/publication-readiness/governance/release-controls.md | Issue-ownership topic this document defers to instead of duplicating it. |
| .github/workflows/scala-steward.yml | Daily-cron dependency-update automation whose review ownership remains unassigned. |
