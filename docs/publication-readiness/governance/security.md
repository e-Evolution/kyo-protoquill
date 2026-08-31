# Security (Draft)

## Status

This is a **review draft** governance baseline, not the repository's live
root-level `SECURITY.md`. Neither the fork nor its parent
`zio/zio-protoquill` has a `SECURITY.md` at any level, so there is no
upstream template to adapt.

## Private Security-Reporting Guidance

- A security vulnerability is expected to be reported privately, not as a
  public GitHub issue, so it is not disclosed before a fix is available.
- **No reporting address, person, or team is named here.** The
  maintainer's contact domain has been observed to be `e-evolution.com`
  (`docs/publication-readiness/evidence.md` ER-031 records that this
  domain resolves via reverse DNS), but a domain is not a person, a
  monitored inbox, or a confirmed security contact — it grants no
  reporting-channel authority by itself. `docs/publication-readiness/destination-decision.yaml`'s
  `owners.security_contact_owner` remains `unassigned`, and this draft
  stays consistent with that: it invents no address, no name, and no
  response-time commitment.
- Until a security contact owner is named and approved, a report made
  through this repository's ordinary public issue tracker is the only
  channel that exists, which itself is a governance gap this draft
  records rather than resolves.
- Supported-version guidance (which release lines receive a fix) is
  deferred to the destination-decision record and to
  `docs/publication-readiness/release-manifest.yaml`; no version is
  declared "supported" here that those records do not already evidence.

## Governance Topic Matrix

| topic | status | owner | approver | evidence | gap | required disposition |
| --- | --- | --- | --- | --- | --- | --- |
| private security-reporting guidance | unresolved | unassigned | unassigned | ER-042 | security contact ownership and approval are not recorded | Keep unresolved; named owner and approver must approve security governance. |

## Reference Links

| path | description |
| --- | --- |
| docs/publication-readiness/destination-decision.yaml | Source of the unassigned `owners.security_contact_owner` field this draft depends on. |
| docs/publication-readiness/evidence.md | ER-031 records the observed `e-evolution.com` domain and its explicit non-authority. |
| docs/publication-readiness/release-manifest.yaml | Source of the proposed, not-yet-approved version/coordinate fields referenced above. |
