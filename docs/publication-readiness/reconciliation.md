# Publication Readiness — Release Manifest Reconciliation

## Purpose

Maps every field in `release-manifest.yaml` to each surface that field is
applicable to (`build_sbt`, `readme`, `changelog`, `local_tags`,
`remote_queries`, `generated_poms`, `archive_contents`), per WU6's task
text. One row per applicable `(field, surface)` pair; a `match_status` of
`source` marks the surface that is the origin of the manifest's `observed`
value, `match`/`mismatch` records independent corroboration or conflict,
and `unknown`/`unavailable` records an honest gap rather than skipping the
pair. `remote_queries` covers only explicitly authorized read-only queries
against the selected fork's GitHub repository/API and Maven Central; no
other remote endpoint was queried. No `build.sbt`, tag, coordinate,
`README.md`, or `CHANGELOG.md` edit was made while producing this record.

## Candidate Binding

| Field | Value |
| --- | --- |
| `candidate_id` | `4d286f7d` |
| `candidate_tree` | `8924989ad8d3d676aff73c30ab93ad31878e7385` |
| `captured_at` | 2026-08-27 |
| Scope | WU6 — release metadata proposal and reconciliation, no publication |

## Reconciliation Results

| field | surface | surface_observation | match_status | evidence_ref | disposition |
| --- | --- | --- | --- | --- | --- |
| version | build_sbt | `version := "5.0.0-kyo-RC6"`; `versionScheme := Some("always")` | source | ER-014 | Origin of `fields.version.observed`. |
| version | readme | Lines 47, 49, 51, 53, 55 advertise coordinates at version `5.0.0` (no RC6 suffix). | mismatch | ER-022 | Stale; remediation is WU9's, not performed here. |
| version | changelog | Newest entry is titled `Kyo Quill 5.0.0` but describes RC5 content; no RC6 entry exists. | mismatch | ER-030 | Remediation is WU9's, not performed here. |
| version | local_tags | Local tag `v5.0.0` points at `acc60185`, not an ancestor of `master`; `HEAD` (RC6 content) is untagged. | mismatch | ER-029 | Recorded as a release-identity fact; no tag proposed. |
| version | remote_queries | Maven Central `io/getquill/quill-sql_3/5.0.0/` and `.../5.0.0-kyo-RC6/` both return HTTP 404; `io.getquill` on Maven Central holds 45 published versions of `quill-sql_3` (latest 4.8.6), owned by the upstream Quill project. | mismatch | ER-031 | Neither string is published anywhere; the existing `io.getquill` namespace on Maven Central belongs to a different project entirely. |
| version | generated_poms | `5.0.0-kyo-RC6` resolves from `~/.ivy2/local/io.getquill/*/5.0.0-kyo-RC6/` for all 8 modules (orphaned under the old coordinate). | source | ER-023 | Local evidence only, never an approved release identity. |
| kyo_version | build_sbt | `kyoVersion = "1.0.0-RC6"` (build.sbt:90) | source | ER-014 | Origin of `fields.kyo_version.observed`. |
| kyo_version | readme | Line 42 declares `val kyoVersion = "1.0.0-RC5"`. | mismatch | ER-022 | Remediation is WU9's, not performed here. |
| kyo_version | changelog | Newest entry describes Kyo `1.0.0-RC5`; no RC6 entry exists. | mismatch | ER-030 | Same gap as `version`/`changelog`; not duplicated in disposition. |
| scala_version | build_sbt | `scalaVersion := "3.8.4"` (build.sbt:305); `-release:25` (build.sbt:319) | source | ER-014 | No other surface carries independent scala-version evidence for this candidate. |
| java_version | build_sbt | `-release:25` (build.sbt:319); `javacOptions ++= Seq("-source", "25", "-target", "25")` (build.sbt:322) | source | ER-014 | No other surface carries independent java-version evidence for this candidate. |
| tag | local_tags | `HEAD` untagged; `v5.0.0` -> `acc60185`, not an ancestor of `master`. | mismatch | ER-029 | Recorded as a release-identity fact; no tag created or moved. |
| tag | remote_queries | `git ls-remote --tags origin` returns empty; `origin` has no tags at all. | unavailable | ER-029 | No remote tag exists to compare against. |
| tag | changelog | CHANGELOG.md line 1 routes release notes to a GitHub Releases page for a nonexistent repository (see `scm`/`remote_queries`). | mismatch | ER-030 | No tag-to-changelog mapping is possible while both are broken. |
| coordinates | build_sbt | `organization := "com.e-evolution"` (build.sbt:7) | source | ER-014 | Origin of `fields.coordinates.observed`. |
| coordinates | remote_queries | `com.e-evolution` has zero artifacts on Maven Central; `io.getquill` on Maven Central is 45 published versions of `quill-sql_3` (latest 4.8.6) owned by the upstream Quill project, unrelated to this fork. | mismatch | ER-031 | Namespace authority is unassigned; not granted by domain control (`e-evolution.com` HTTP 200) alone. Routed to WU11. |
| coordinates | generated_poms | Orphaned local POMs under `~/.ivy2/local/io.getquill/*/5.0.0-kyo-RC6/` still embed the old groupId and a dead `scmInfo` connection string. No POM exists yet under `com.e-evolution`. | mismatch | ER-023 | A fresh local install under the new coordinate is WU7's work, not performed here. |
| modules | build_sbt | 9 modules across 4 sets (`baseModules`, `sqlTestModules`, `dbModules`, `bigdataModules`); 8 source-bearing/publishable, root aggregate `publishArtifact := false`. | source | ER-010 | Origin of `fields.modules.observed`, cross-referenced to `modules.md#module-inventory`. |
| modules | generated_poms | 8 orphaned POMs exist under the old `io.getquill` coordinate for `5.0.0-kyo-RC6`; 0 exist under `com.e-evolution`. | mismatch | ER-023 | Same gap as `coordinates`/`generated_poms`; not duplicated in disposition. |
| readme_coordinates | readme | Lines 47, 49, 51, 53, 55: `"io.getquill" %% "quill-*" % "5.0.0"`; line 42: `kyoVersion = "1.0.0-RC5"`. | source | ER-022 | Origin of `fields.readme_coordinates.observed`. |
| readme_coordinates | build_sbt | `organization` is `com.e-evolution`, `version` is `5.0.0-kyo-RC6`, `kyoVersion` is `1.0.0-RC6`; README reflects none of the three current values. | mismatch | ER-022 | Correction requires WU9's approved documentation edit authority. |
| changelog | build_sbt | Current build state (`com.e-evolution`, `5.0.0-kyo-RC6`, Kyo `1.0.0-RC6`) has no corresponding CHANGELOG.md entry. | mismatch | ER-030 | Remediation is WU9's, not performed here. |
| changelog | remote_queries | CHANGELOG.md line 1 links to `https://github.com/getkyo/kyo-protoquill/releases`; the authenticated `gh api repos/getkyo/kyo-protoquill` call returns HTTP 404 for the same repository. | mismatch | ER-030 | Same dead repository as `scm`/`homepage`; confirmed via the authenticated query per the method note there. |
| homepage | build_sbt | `homepage := Some(url("https://getkyo.io/kyo-quill"))` (build.sbt:8) | source | ER-024 | Origin of `fields.homepage.observed`. |
| homepage | remote_queries | HTTP GET/HEAD to `https://getkyo.io/kyo-quill` returns 404. | mismatch | ER-024 | Dead public link; build.sbt was not edited. |
| scm | build_sbt | `scmInfo := ScmInfo(url("https://github.com/getkyo/kyo-protoquill"), ...)` (build.sbt:14) | source | ER-024 | Origin of `fields.scm.observed`. |
| scm | remote_queries | Unauthenticated `curl` to the GitHub API returns 403 (rate limiting, not a result); authenticated `gh api repos/getkyo/kyo-protoquill` returns 404 (repository does not exist). `README.md` line 3's separate link to `https://github.com/getkyo/kyo` resolves correctly and is not part of this finding. | mismatch | ER-024 | Dead public link, confirmed only by the authenticated call; build.sbt was not edited. |
| license | build_sbt | `licenses := List(("Apache License 2.0", ...))` (build.sbt:9); `developers` lists only `deusaquilus` (build.sbt:11). | source | ER-014 | Origin of `fields.license.observed`. |
| license | archive_contents | `LICENSE.txt` carries the Apache-2.0 text with a copyright notice for Flavio Brasil (the upstream Quill author); no fork-maintainer copyright or notice exists. No source archive/tarball has been generated for this candidate (WU7 pending); this row reports the tracked file that would populate one. | mismatch | ER-009 | Attribution ownership remains blocked at ER-009 in `provenance.md`; not reopened or resolved here. |

## Coverage

All 12 manifest fields are represented above across their declared
`applicable_surfaces`; 30 `(field, surface)` rows total. Coverage is
enforced mechanically by `scripts/publication-readiness/check_records.py`'s
`release manifest surface coverage` check, which compares this table
against `release-manifest.yaml`'s `fields.*.applicable_surfaces` and
reports any missing pair, any undeclared extra pair, and any duplicate row
rather than silently accepting a gap.

## Verification

- **RED**: the two new checker checks (`release manifest required fields
  and enums`, `release manifest surface coverage`) and the extension of
  `document candidate binding` to also scan `release-manifest.yaml` were
  added before this file or `release-manifest.yaml` existed.
  `python3 scripts/publication-readiness/check_records.py` exited `1`:
  the two new checks and the extended `document candidate binding` check
  reported `missing file` for `release-manifest.yaml`; all 12 prior checks
  stayed `PASS` unmodified.
- **GREEN**: after creating `release-manifest.yaml` and this file, the same
  command exited `0` with all 15 checks `PASS`.
- **Focused verification command and result**:
  `python3 scripts/publication-readiness/check_records.py` — exit `0`,
  `RESULT: PASS`, 15/15 checks passing against the real records.
    - **Runtime scenario**: `N/A` — metadata reconciliation is read-only; no
      application runtime boundary applies to this unit's deliverables.

## S07 Publication-Branch Reconciliation Plan

**Decision:** this is a comparison record only. It does not move or otherwise
change a ref.

```yaml
plan_only: true
mutation_authorized: false
```

### Fixed comparison boundary

| Field | Value |
| --- | --- |
| source commit | `b45c3ea25a3de59b58329cf4c76e8938a1c7892c` |
| source tree | `9c162c4f3d0d36c141517da6b61cfe21fe598dc8` |
| target commit | `4d286f7d9172f452fd3d0ae2458ef73c11376fea` |
| target tree | `8924989ad8d3d676aff73c30ab93ad31878e7385` |
| merge-base | `b45c3ea25a3de59b58329cf4c76e8938a1c7892c` |
| ancestry | source is an ancestor of target |

### Read-only comparison evidence

The bounded commands below established only commit/tree and path facts:

```text
git rev-parse b45c3ea2^{commit} b45c3ea2^{tree} 4d286f7d9172f452fd3d0ae2458ef73c11376fea^{commit} 4d286f7d9172f452fd3d0ae2458ef73c11376fea^{tree}
git merge-base b45c3ea2 4d286f7d9172f452fd3d0ae2458ef73c11376fea
git merge-base --is-ancestor b45c3ea2 4d286f7d9172f452fd3d0ae2458ef73c11376fea
git diff --name-status b45c3ea2 4d286f7d9172f452fd3d0ae2458ef73c11376fea
git diff --numstat b45c3ea2 4d286f7d9172f452fd3d0ae2458ef73c11376fea
```

Results: the merge-base equals the source; the ancestry check exits `0`; the
commit-to-commit comparison has one modified path, `build.sbt`, with `1`
addition and `1` deletion. This record makes no assertion about unrelated
workspace state or an untracked package.

### Reconciliation decisions

| Area | Decision | Basis | Future disposition |
| --- | --- | --- | --- |
| baseline history | retain | Source is the merge-base and ancestor. | Keep the source history intact. |
| candidate delta | replace only after explicit authority | The sole committed delta is the `build.sbt` groupId change. | A future authorized delivery may choose the target content; this plan does not apply it. |
| publication-readiness package mapping | unresolved | This S07 comparison is limited to committed source/target tree facts. | Map only allowlisted package paths in a separately authorized work unit. |
| conflict expectation | unresolved, not waived | Ancestry avoids a committed merge conflict, but future delivery inputs are not authorized or inspected here. | Re-evaluate against the exact future candidate and authorized path set. |
| dependency-graph provenance | retain unresolved classification | S03 cited blob/history facts but did not establish authorship or inheritance. | Preserve the unresolved provenance blocker; do not infer origin. |

### Validation and authority gates

- Re-resolve the exact source/target commits, trees, merge-base, and bounded
  path comparison immediately before any future authorized operation.
- Require an explicit user authorization that names the proposed ref operation,
  its source and target, allowed paths, and rollback owner. This plan is not
  that authorization.
- Require the candidate-bound verification, stale/fresh evidence labels,
  `not-ready` disposition, and all seven blockers to remain visible. No gate
  closes a blocker automatically.
- Stop on identity drift, a non-ancestor relationship, an unresolved conflict,
  missing authority, or an over-400-line future slice.

### Rollback boundary

S07 changes only this plan record, its S07 evidence row, and the coupled
checker behavior. Revert only those S07 additions if the plan is withdrawn.
    Do not use checkout, reset, merge, rebase, branch, ref, index, remote, or
    history operations as rollback.

## S10 Control-Record Reconciliation

**Decision:** destination and release metadata remain incomplete. This readback
preserves `not-ready` and confers no delivery authority.

| Control | Current evidence | State |
| --- | --- | --- |
| Release identity and SemVer | ER-028 and ER-029 | `unresolved`; no version or tag is selected. |
| Namespace and artifact destination | ER-031 and ER-059 | `pending`; all owners and approvers remain `unassigned`. |
| Authority sign-off | destination decision record | `pending`; the authority reference remains `unassigned`, so no sign-off exists. |
| Candidate-bound verification | ER-065 | Fresh technical evidence supports blockers only; it is not approval. |
| Plan-only branch comparison | ER-066 | Comparison-only; ref mutation remains unauthorized. |

No owner, approver, destination approval, date, sign-off, release authority, or
publication authority is inferred. Every authorization remains `not-authorized`.
