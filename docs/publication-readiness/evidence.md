# Publication Readiness — Evidence, Decision, and Readiness Schemas

## Purpose

Defines the evidence record schema, verification record schema, freshness
rules, severity rules, and derived readiness states that every later
publication-readiness work unit (WU3-WU12) reuses. This is the WU2 schema
foundation, not the full provenance/verification/governance content that
belongs to later work units.

## Candidate Binding

| Field | Value |
| --- | --- |
| `candidate_id` | `4d286f7d` |
| `candidate_tree` | `8924989ad8d3d676aff73c30ab93ad31878e7385` |
| `captured_at` | 2026-08-27 |
| Scope | WU2 only — evidence, decision, and readiness schemas |
| `superseded_candidate_id` | `b45c3ea2` (tree `9c162c4f3d0d36c141517da6b61cfe21fe598dc8`) |
| Rebinding record | `verification.md#candidate-rebinding-record` |

Every evidence record here binds to this candidate. Per the Freshness Rules
below, a row becomes stale the moment a later work unit records evidence
against a different candidate.

**Candidate rebinding (2026-08-27).** The maintainer changed the publication
`organization` (groupId) from `io.getquill` to `com.e-evolution` in commit
`4d286f7d`, which created a new candidate and made every record bound to
`b45c3ea2` stale under those Freshness Rules. Rather than assert that a
publishing-coordinate change cannot affect test outcomes, the full tier
matrix was **re-measured** at `4d286f7d` and found identical. Every row in
the Evidence Index below is therefore rebound to `4d286f7d`, and
`verification.md#candidate-rebinding-record` states for each record whether
it was re-measured, re-verified, or carried forward with a stated
justification. Observed values and `captured_at` values were not rewritten
for observations that were not re-made; the re-measurement fell on the same
UTC date (2026-08-27) as the original measurements, so no `captured_at`
value changed in either direction.

## S04 Boundary Classification

S04 preserves prior observations but classifies them against the only future-fresh
boundary: full commit `4d286f7d9172f452fd3d0ae2458ef73c11376fea`, tree
`8924989ad8d3d676aff73c30ab93ad31878e7385`, Eclipse Temurin `25.0.4+7` as
the actual sbt JVM, a maintainer-supplied `normal-local-clone`, and the current
manifest and checker-input revisions. No row below is fresh verification; S06
must create new evidence rather than textually rebind these observations.

| record_id | subject | candidate_ref | tree_ref | environment_ref | freshness | invalidation_reason | superseding_id |
| --- | --- | --- | --- | --- | --- | --- | --- |
| S04-ER-001 | Historical verification-clone observation | `be8826eb` | tree not established for this amendment | prior clone/check-out assumptions; vendor and actual sbt JVM not proven Temurin | historical | commit-mismatch, tree-mismatch, clone-kind-mismatch, jdk-vendor-version-mismatch, sbt-jvm-mismatch | S06-Temurin-normal-clone-TBD |
| S04-ER-002 | Publication-branch comparison baseline | `b45c3ea2` | `9c162c4f3d0d36c141517da6b61cfe21fe598dc8` | comparison-only branch environment | comparative | commit-mismatch, tree-mismatch, clone-kind-mismatch | S07-plan-only-TBD |
| S04-ER-003 | Retained local tier and module results (ER-010, ER-014..ER-019, ER-032..ER-039, ER-061) | `4d286f7d9172f452fd3d0ae2458ef73c11376fea` | `8924989ad8d3d676aff73c30ab93ad31878e7385` | Homebrew OpenJDK 25.0.4 / actual sbt JVM Homebrew Java 25.0.4; prior local checkout | stale | jdk-vendor-version-mismatch, sbt-jvm-mismatch, clone-kind-mismatch | S06-Temurin-normal-clone-TBD |
| S04-ER-004 | Manifest-, dependency-, command-, and checker-derived record assertions | `4d286f7d9172f452fd3d0ae2458ef73c11376fea` | `8924989ad8d3d676aff73c30ab93ad31878e7385` | prior release-manifest and structural-checker inputs | stale | manifest-mismatch, checker-input-mismatch | S05-record-foundation-TBD |
| S04-ER-005 | Superseded candidate rebinding narrative and associated claimed-current evidence | `b45c3ea2` and `4d286f7d` | prior/current trees as recorded above | Homebrew actual sbt JVM and prior clone assumptions | superseded | commit-mismatch, tree-mismatch, jdk-vendor-version-mismatch, sbt-jvm-mismatch, clone-kind-mismatch | S06-Temurin-normal-clone-TBD |

## Evidence Record Schema

Every observation is represented by an evidence record. Markdown tables may
render records for review, but the fields stay consistent across every
publication-readiness document. (Source: `design.md` "Evidence Model >
Evidence Record".)

| Field | Contract |
| --- | --- |
| `id` | Stable identifier unique within the readiness package. |
| `subject` | Claim, module, metadata surface, check, or governance requirement being evaluated. |
| `classification` | `verified-fact`, `inference`, or `unresolved-question`. |
| `source` | Repository-relative file and line/range, immutable candidate object, command record, or explicitly supplied read-only external source. |
| `captured_at` | UTC timestamp or date. |
| `candidate_id` | Immutable content identity to which the observation applies. |
| `environment` | OS, architecture, JDK, Java release, Scala, sbt, Kyo, services, and non-secret configuration when relevant. |
| `procedure` | Exact read-only inspection or verification command/scenario. |
| `result` | Exit status and bounded output summary; test totals where available. |
| `status` | `current`, `stale`, `unavailable`, `failed`, `blocked`, or `not-applicable`. |
| `severity` | `blocking` or `advisory`. |
| `owner` | Accountable person or `unassigned`; never inferred. |
| `approver` | Required approver or `unassigned`; never inferred. |
| `disposition` | Required next action, approved waiver, or reason no action applies. |
| `evidence_links` | Links to repository-relative records or explicitly approved external evidence. |

A fact is not an approval. An inference cannot satisfy a readiness
requirement. An unresolved owner or approver keeps a blocking record open.

The `## Evidence Index` table below renders a subset of these fields
(`id`, `subject`, `classification`, `source`, `captured_at`, `candidate_id`,
`status`, `severity`, `owner`, `approver`, `disposition`) for the structural
checker; `environment`, `procedure`, `result`, and `evidence_links` are
recorded in each row's disposition/source narrative where applicable.

## S08 Evidence Core Validation

New or amended structured evidence records MUST use this core contract. The
existing Evidence Index remains valid historical/stale evidence and is not
textually rebound by this schema.

| field | fail-closed contract |
| --- | --- |
| `id` | Unique `ER-<digits>` identifier. |
| `classification` | One of `verified-fact`, `inference`, or `unresolved-question`; an inference or question never supplies approval. |
| `freshness` | One of `fresh`, `historical`, `comparative`, `stale`, or `superseded`. |
| `status` | One of `current`, `stale`, `unavailable`, `failed`, `blocked`, `not-applicable`, or `open`. |
| `candidate_ref` | Exact candidate identity; `fresh` requires full `4d286f7d9172f452fd3d0ae2458ef73c11376fea`. |
| `environment_ref` | Non-empty bounded environment record reference; fresh evidence cannot omit it. |
| `blocker_links` | Comma-separated known blocker or evidence IDs; dangling links are rejected. |
| `owner` | `unassigned` unless an attributable authority record names an owner. |
| `approver` | `unassigned` unless an attributable authority record names an approver. |
| `authority_ref` | Required when `owner` or `approver` is named; a record never invents authority. |
| `disposition` | Required explicit next action or retained-history disposition. |
| `invalidation_reason` | Required for every non-fresh record. |
| `superseding_id` | Required for `superseded` evidence and must resolve to another known record. |

Fresh evidence must be `current`, bind both candidate and environment, and is
never manufactured by editing an older observation. Historical, comparative,
stale, and superseded observations remain valid when their invalidation
metadata is complete. This core contract does not close blockers or change the
aggregate `not-ready` disposition.

    ## S09 Slice Integrity and Operation-Safety Contract

    Every new slice record is fail-closed and contains: outcome, start state,
    end state, path allowlist, protected non-targets, dependencies, evidence
    links, stale-evidence links, acceptance criteria, verification, runtime
    scenario, additions, deletions, rollback boundary, delivery authority, and
    instructions. Each evidence link resolves to a known record. A stale linked
    record must also appear in `stale_evidence_links`; this stale-link
    propagation prevents historical evidence from silently supporting current
    work.

    The checker performs protected-non-target rejection when an allowlisted
    path captures `.codegraph/` or `pi-session-*.html`. It rejects a slice whose
    additions plus deletions exceed 400: exactly 400 is valid, while 401 is not.
    `delivery_authority` remains exactly `not-authorized`.

    Instructions are scanned as static text only. A mutation-capable instruction
    such as `$ git push origin main`, `git commit`, `gh pr create`, `sbt publish`,
    or `mvn deploy` is rejected. Plan-only Git wording is permitted when it names
    a future command without presenting an executable command line; it grants no
    Git, remote, publication, or delivery authority. Fixtures and checker tests
    are pure in-memory records: they execute no commands and inspect no protected
    state.

    ## Verification Record Schema


Verification records extend the evidence record with (source: `design.md`
"Evidence Model > Verification Record"): exact command and
working-directory contract; start and completion times; required services
and their versions/health evidence; non-secret configuration inputs; exit
result, suite/test totals, and bounded logs; failure class (`product`,
`infrastructure`, `unavailable`, or `ambiguous`); generated-output inventory
and digest when applicable.

    A service outage is never converted into a pass. Ambiguous or incomplete
    output is blocking. WU2 defines this schema; the first records that use it
    belong to WU4 (Tier 0/1) and WU5 (Tier 2).

    ## S05 Fresh Verification Foundation

    `verification.md#s05-fresh-temurin-verification-foundation` is an open
    contract for a future, maintainer-supplied `normal-local-clone` only. It is
    pinned to full commit `4d286f7d9172f452fd3d0ae2458ef73c11376fea` and tree
    `8924989ad8d3d676aff73c30ab93ad31878e7385`; it requires OS/architecture,
    actual sbt-JVM Temurin vendor/version and Java release, Scala/sbt versions,
    commands, service prerequisites, timing, result/totals, clone/workspace
    assumptions, and bounded references before an observation can be assessed.

    Every observation placeholder remains `open`. The foundation records no
    Temurin runtime evidence and no pass/fail outcome. Sbt-process evidence,
    not shell `java`, must establish the actual JVM. Existing Homebrew or
    earlier-clone observations remain stale and cannot satisfy this foundation.
    Missing, ambiguous, fake-fresh, or shell/sbt-JVM-mismatched evidence stays
    unsatisfied; blockers 6 and 7 remain open and the disposition stays
    `not-ready`.

    ## Freshness Rules


(Source: `design.md` "Evidence Model > Freshness Rules".)

Evidence becomes stale when any relevant input changes:

- candidate content identity;
- proposed release manifest;
- Scala, sbt, JDK/Java release, Kyo, or relevant dependency version;
- CI workflow or verification command;
- service image/configuration used by an integration suite;
- generated POM/artifact inputs;
- a public claim mapped to the evidence.

Stale evidence remains available as history but contributes no passing
readiness status. The structural checker enforces one instance of this
rule mechanically: every Evidence Index row whose `candidate_id` does not
match the pinned candidate above must carry `status: stale` or
`status: not-applicable`, never `status: current`.

## Severity Rules

Severity is derived, not asserted freely, from `design.md`'s Readiness
Calculation and spec.md's "Readiness Status Is Evidence-Based and Preserves
Workspace State" requirement:

- **`blocking`**: `status` is `unresolved`, `stale`, `unavailable`,
  `failed`, `blocked`, or `ambiguous` on a record mapping to a
  specification requirement or public claim; or the record is a required
  owner/approver/authority field still `unassigned`/`pending`; or it is a
  detected forbidden remote-mutation instruction.
- **`advisory`**: informative but does not itself gate readiness — retained
  historical RC1/RC5 evidence explicitly labeled historical, documentation
  refinement notes, or an open follow-up carried forward for triage by its
  owning work unit (for example, the two items proposal.md section 2.4
  routes to WU6 and WU10) until that unit closes or escalates it.

Only the owning work unit's disposition can close a blocking record;
narrative alone never downgrades it to advisory.

## Derived Readiness States

(Source: `design.md` "Evidence Model > Readiness Calculation".) Overall
status is derived, never manually asserted:

- `not-ready`: any blocking record is unresolved, failed, unavailable,
  ambiguous, stale, or lacks required ownership/approval;
- `destination-gated`: destination-independent blockers are closed, but the
  destination decision is pending or incomplete;
- `ready-for-destination-specific-planning`: all destination-independent
  blockers are closed and the destination decision record is fully
  approved;
- `ready-to-publish` is deliberately not produced by this change.

## Destination Decision Record Schema

`docs/publication-readiness/destination-decision.yaml` implements the hard
decision gate from `design.md` "Phase 7: Hard Destination Decision Gate"
and spec.md's "Destination Decision Is Explicit and Blocking" requirement,
as a flat mapping of dotted keys to scalar values (still valid YAML; no
block nesting is required to represent it). Required keys: `schema_version`,
`candidate_id`, `candidate_tree`, `captured_at`, `decision_status`,
`evidence_links`, `scope`, `conditions`; one or more
`approved_facts.<name>.{value,classification,source,captured_at}` groups;
`selected_destination.status` plus
`selected_destination.<entry>.{state,authorized}` for
`upstream_contribution`, `source_publication`, `maven_release`,
`github_release`; `owners.<key>` for `decision_owner`, `repository_owner`,
`artifact_namespace_owner`, `release_approver`, `governance_approver`,
`security_contact_owner`, `credential_owner`; `dates.decision_date` and
`dates.approval_date`; and `authorization.<entry>.{state,authorized}` for
`maven_central` and `github_release`.

`decision_status` MUST NOT be `approved` while any owner is `unassigned`,
any date is `pending`, `scope`/`conditions` are `pending`, or
`selected_destination.status` is `unresolved`. `authorization.*.state` and
`authorization.*.authorized` MUST stay `not-selected`/`not-authorized`
while `decision_status` is `pending` — a fact record for WU2, never an
approved not-applicable waiver; only the named authority in spec.md can
change it once the decision gate is genuinely approved.

**WU11 authority-matrix update.** Once a destination is explicitly proposed
(source_repository provenance closed and the destination named, per
design.md's "Phase 7: Hard Destination Decision Gate" — "selected
destination remains unresolved unless a proposal has been explicitly
recorded"), the matching `selected_destination.<entry>.state` moves from
`not-selected` to `selected` and `selected_destination.status` moves from
`unresolved` to `proposed`. Neither change relaxes the approval gate:
`decision_status` stays `pending`, `authorization.*` stays
`not-selected`/`not-authorized`, and every owner/date field stays
`unassigned`/`pending` until a named decision owner and every applicable
owner/approver complete this record, matching spec.md's "Decision record is
incomplete" scenario. `scope` and `conditions` MAY be populated with
non-`pending` narrative describing what a future approval would and would
not cover while the record is still `pending` — recording scope is not the
same as granting it.

## Release Manifest Record Schema

`docs/publication-readiness/release-manifest.yaml`, added by WU6, follows
the same flat-dotted-key YAML convention as `destination-decision.yaml`.
Required keys: `schema_version`, `candidate_id`, `candidate_tree`,
`captured_at`, `manifest_status` (`unresolved` or `resolved`); one
`fields.<name>.{observed,proposed,state,severity,owner,applicable_surfaces,disposition}`
group for each of the twelve required fields (`version`, `kyo_version`,
`scala_version`, `java_version`, `tag`, `coordinates`, `modules`,
`readme_coordinates`, `changelog`, `homepage`, `scm`, `license`); and one
`semver_precedence.{finding,severity,state,owner,proposed_dispositions,source}`
group. `fields.<name>.state` is constrained to `observed`, `proposed`, or
`unresolved` — never `resolved`/`approved` — enforcing that a field whose
disposition needs publication, tagging, Maven Central, signing, or GitHub
Release authority stays proposed or unresolved rather than claiming
approval. `fields.<name>.applicable_surfaces` is a comma-separated list
drawn from `build_sbt`, `readme`, `changelog`, `local_tags`,
`remote_queries`, `generated_poms`, `archive_contents`, and every declared
pair must have exactly one matching row in
`docs/publication-readiness/reconciliation.md`'s `## Reconciliation
Results` table (`match_status` one of `source`, `match`, `mismatch`,
`unknown`, `unavailable`) — the structural checker's
`release manifest surface coverage` check enforces this both ways: no
declared pair may be missing a row, and no row may declare a pair the
manifest does not list.

## Governance Document Schema

`docs/publication-readiness/governance/{contributing,security,code-of-conduct,support-maintenance,release-controls}.md`,
added by WU8, implement the destination-independent governance baseline
required by spec.md's "Publication Documentation and Governance Are
Complete" requirement. Each file carries narrative content plus two
structured sections the checker validates:

- A `## Governance Topic Matrix` table with columns `topic`,
  `disposition_type`, `owner`, `approver`, `rationale`, `scope`,
  `approval_date`, `evidence_ref`. `disposition_type` is constrained to
  `named`, `waived`, or `unresolved`. A `named` or `waived` row's `owner`
  and `approver` MUST NOT read `unassigned`, and its `evidence_ref` MUST
  cite an id that exists in the Evidence Index below — a confirmed owner
  or approver is never accepted on narrative assertion alone. A `waived`
  row additionally requires non-`pending` `rationale`, `scope`, and
  `approval_date`, matching spec.md's explicit waiver fields. An
  `unresolved` row's `owner` and `approver` MUST both read exactly
  `unassigned`, so a topic can never claim partial, unconfirmed authority.
  Each governance file's required `topic` values are fixed per file (for
  example `release-controls.md` requires `issue ownership`, `release
  responsibilities`, `ci/release separation`, `approval evidence`, and
  `abort/recovery ownership`) and are enforced by the `governance required
  topics` check.
- A `## Reference Links` table with columns `path`, `description`. Every
  `path` cell (backticks and any trailing `#anchor` stripped) MUST resolve
  to a real repository-relative file; the `governance reference readback`
  check enforces this mechanically.

These documents are review drafts under `docs/publication-readiness/governance/`,
never the repository's live root-level `CONTRIBUTING.md`/`SECURITY.md`/
`CODE_OF_CONDUCT.md`/`SUPPORT.md`, and they name no destination-specific
contact, badge, repository setting, Maven instruction, or GitHub Release
instruction.

## Public Claim Coverage

Maps every public claim changed by WU9 to the evidence record documenting
its correction (source: `design.md`'s claim-coverage requirement and
tasks.md WU9). `status` is one of `qualified` (scope narrowed to what
evidence supports), `corrected` (a factually wrong value replaced), or
`documented` (a previously undocumented fact made explicit). `file` is
constrained to WU9's maintainer-approved edit-authority paths; the
`public claim coverage` structural check rejects any row naming a file
outside that list or an `evidence_id` that is not an existing Evidence
Index id (below).

| id | file | claim | evidence_id | status |
| --- | --- | --- | --- | --- |
| CC-001 | README.md | `kyoVersion = "1.0.0-RC5"` sample declaration (line 42) | ER-047 | corrected |
| CC-002 | README.md | Install coordinates `"io.getquill" %% "quill-*" % "5.0.0"` (lines 47-55) | ER-047 | corrected |
| CC-003 | README.md | Intro claim: migrated to Kyo "while preserving all Quill functionality" | ER-047 | qualified |
| CC-004 | README.md | Consumer coordinate-groupId vs. Scala-package-namespace split was undocumented | ER-048 | documented |
| CC-005 | README.md | No measured test-result summary was present for public readers | ER-048 | documented |
| CC-006 | README.md | Requirements section declared Kyo `1.0.0-RC5` (3 occurrences) | ER-049 | corrected |
| CC-007 | README.md | "stay on `4.8.8`" fallback claim for JDK 17+ users | ER-049 | corrected |
| CC-008 | CHANGELOG.md | Historical note routed release notes to the nonexistent `github.com/getkyo/kyo-protoquill/releases` | ER-050 | corrected |
| CC-009 | CHANGELOG.md | No changelog entry existed for the Kyo RC6 bump or the groupId change | ER-050 | corrected |
| CC-010 | .github/ISSUE_TEMPLATE.md | `@getquill/maintainers` foreign-organization mention and `getquill.io`/scastie link | ER-051 | corrected |
| CC-011 | .github/PULL_REQUEST_TEMPLATE.md | `@getquill/maintainers` foreign-organization mention | ER-051 | corrected |
| CC-012 | FINAL_STATUS_REPORT.md | Reads as current status; origin of the false "1,171 tests passing" figure | ER-052 | qualified |
| CC-013 | MIGRATION_PLAN.md | Reads as current status for the superseded RC1/Scala 3.8.1/JDK 17 era | ER-052 | qualified |
| CC-014 | MIGRATION_REPORT.md | Reads as current status for the superseded RC1/Scala 3.8.1/JDK 17 era | ER-052 | qualified |
| CC-015 | VALIDATION_REPORT_FINAL.md | Reads as current status for the superseded RC1/Scala 3.8.1/JDK 17 era | ER-052 | qualified |

## Dependency-Graph Provenance (S03)

| classification | statement | citations |
| --- | --- | --- |
| verified-fact | `.github/workflows/dependency-graph.yml` has mode `100644` and blob `ba9a124d344086013856cf37c02f2b18806d5273` in both comparison commits. | `.github/workflows/dependency-graph.yml`; `b45c3ea25a3de59b58329cf4c76e8938a1c7892c`; `4d286f7d9172f452fd3d0ae2458ef73c11376fea`; `ba9a124d344086013856cf37c02f2b18806d5273` |
| inference | The `.github/workflows/dependency-graph.yml` history add supports a bounded path-introduction observation, not an inherited or locally amended origin conclusion. | `.github/workflows/dependency-graph.yml`; `0162fd4705debcd92a418b80c76be932722d3abb`; `a6aad4b14c23c8244a14d59f72f18cf2e0dc8d81` |
| unresolved-question | `.github/workflows/dependency-graph.yml` authorship, upstream inheritance, and any uncited setup-Java amendment remain unresolved. | `.github/workflows/dependency-graph.yml`; `0162fd4705debcd92a418b80c76be932722d3abb`; `ba9a124d344086013856cf37c02f2b18806d5273` |

## Evidence Index

| id | subject | classification | source | captured_at | candidate_id | status | severity | owner | approver | disposition |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ER-001 | Evidence record schema defined in this document | verified-fact | design.md#evidence-model | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Documented for WU3-WU12 reuse; no further action for WU2. |
| ER-002 | Verification record schema defined in this document | verified-fact | design.md#evidence-model | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Schema only; first populated records belong to WU4/WU5. |
| ER-003 | Freshness rules defined in this document | verified-fact | design.md#evidence-model | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Mechanically enforced by the structural checker's stale-reference check. |
| ER-004 | Severity rules and derived readiness states defined in this document | verified-fact | design.md#evidence-model | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Synthesized from design.md and spec.md; reused by WU12's readiness calculation. |
| ER-005 | destination-decision.yaml created; source publication and parent/source recorded as approved facts; every other field pending/unassigned | verified-fact | docs/publication-readiness/destination-decision.yaml | 2026-08-27 | 4d286f7d | current | blocking | unassigned | unassigned | decision_status remains pending; destination-specific work stays blocked until the named authority completes every field. |
| ER-006 | Structural checker created under scripts/publication-readiness/ and validated against RED (missing records), GREEN (valid records), and an injected invalid fixture (rejected) | verified-fact | scripts/publication-readiness/check_records.py | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Checker is reusable by WU3-WU12 for their own records; no blocking finding for WU2 itself. |
| ER-007 | e-Evolution/kyo-protoquill is a GitHub fork (fork:true) with parent/source zio/zio-protoquill | verified-fact | GitHub repository API, queried 2026-08-27 | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Documented in provenance.md; resolves proposal.md #2.1 inference item. |
| ER-008 | zio/zio-protoquill itself is not a GitHub fork (fork:false, parent/source null) of zio/zio-quill; its Quill relationship is code lineage only | verified-fact | GitHub repository API, queried 2026-08-27 | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Documented in provenance.md, kept distinct from ER-007's fork fact. |
| ER-009 | Copyright, notice, naming, trademark, and upstream-acceptance questions for the inherited ProtoQuill/Quill material | unresolved-question | docs/publication-readiness/provenance.md#blocked-questions | 2026-08-27 | 4d286f7d | blocked | blocking | unassigned | unassigned | Kept explicitly blocked; requires a named maintainer/legal/upstream owner before resolution. |
| ER-010 | Module inventory covering all nine modules in build.sbt's four module sets, with purpose, public Kyo surface, dependency scope, service prerequisite, and current verification state | verified-fact | docs/publication-readiness/modules.md#module-inventory | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Verification-state figures are local evidence only; per-module structured records are WU4/WU5 work. |
| ER-011 | Every discovered residual ZIO reference in Kyo-module source (13 rows) classified exactly once as bridge/codec usage, historical text, or compatibility naming; zero actionable migration debt found | verified-fact | docs/publication-readiness/modules.md#residual-zio-reference-classification | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | The one main-source `import zio.json` is codec usage, deferred to the follow-up Kyo Schema/Json SDD change. |
| ER-012 | Three tracked .bak/.bak2 files under quill-caliban/src/test would ship in a public source publication | verified-fact | quill-caliban/src/test/scala/io/getquill (tracked backup files) | 2026-08-27 | 4d286f7d | blocked | blocking | unassigned | unassigned | Routed to WU9 for disposition (remove vs. retain); not resolved in this unit; do not delete, move, or edit. |
| ER-013 | Four tracked historical report files describe the superseded Kyo RC1/Scala 3.8.1/JDK 17 era; FINAL_STATUS_REPORT.md is the origin of a stale "1,171 tests passing" claim | verified-fact | FINAL_STATUS_REPORT.md, MIGRATION_PLAN.md, MIGRATION_REPORT.md, VALIDATION_REPORT_FINAL.md | 2026-08-27 | 4d286f7d | blocked | blocking | unassigned | unassigned | Maintainer previously decided to keep these files; public-claims remediation is routed to WU9; not resolved in this unit. |
| ER-014 | Tier 0 compile evidence: `sbt compile` (all 9 modules, clean tree) exits 0 in 14s, zero errors | verified-fact | docs/publication-readiness/verification.md#tier-0-compile-evidence | 2026-08-27 | 4d286f7d | stale | blocking | unassigned | unassigned | Structured Tier 0 record for WU1's raw multi-tier evidence; superseded if a later candidate changes. |
| ER-015 | Tier 1 core test evidence: `quill-sql/test; quill-sql-tests/test` exits 0, 942 run / 942 succeeded / 0 failed / 1 ignored | verified-fact | docs/publication-readiness/verification.md#tier-1-core-test-evidence | 2026-08-27 | 4d286f7d | stale | blocking | unassigned | unassigned | quill-sql-tests figure (668/668/0/1 ignored) matches WU1's reference `sqltest` tier exactly; no discrepancy found. |
| ER-016 | sbt's actual pinned JDK is `Homebrew Java 25.0.4` (per sbt banner), not Eclipse Temurin, despite `/usr/bin/java` resolving to Temurin 25.0.4 on this machine | verified-fact | docs/publication-readiness/verification.md#environment | 2026-08-27 | 4d286f7d | stale | advisory | unassigned | unassigned | Both are OpenJDK 25.0.4; result is unaffected, but future records citing "Temurin JDK 25.0.4" for this candidate should cite this finding instead. |
| ER-017 | Triangulation: focused compiles of the four public-Kyo-surface modules and a non-publishing `package` task for `quill-sql`/`quill-kyo`/`quill-jdbc-kyo` all exit 0; a fixture with mismatched `candidate_id` and `status: current` is correctly rejected by the extended structural checker | verified-fact | docs/publication-readiness/verification.md#tier-1-triangulation | 2026-08-27 | 4d286f7d | stale | advisory | unassigned | unassigned | `check_records.py` extended with `verification record completeness` and `verification record staleness` checks (12 checks total, 10 prior + 2 new); RED/GREEN/staleness-rejection cycle recorded in verification.md's Verification section. |
| ER-018 | Tier 2 `db`-tier evidence: 535 run / 534 succeeded / 1 failed / 13 ignored; single failure `io.getquill.context.jdbc.oracle.DistinctJdbcSpec` "Ex 8 Distinct With Sort" classified product (pre-existing, not candidate-caused) | verified-fact | docs/publication-readiness/verification.md#tier-2-database-db-evidence | 2026-08-27 | 4d286f7d | stale | blocking | unassigned | unassigned | Matches the supplied reference full-matrix figure and the July RC5 baseline exactly; not converted to a pass; no candidate-blocking action for this change. |
| ER-019 | Tier 2 `bigdata`-tier evidence: 190 run / 190 succeeded / 0 failed, 21 suites, 0 aborted; six service containers queried read-only and confirmed healthy/reachable before execution; `-Doracle` confirmed a build no-op; active checkout and excluded paths confirmed unchanged after both runs | verified-fact | docs/publication-readiness/verification.md#tier-2-bigdata-bigdata-evidence | 2026-08-27 | 4d286f7d | stale | blocking | unassigned | unassigned | GREEN, no failures. `check_records.py` extended to check the new `## Tier 2 Environment and Service Health` heading and two Tier 2 tables (still 12 checks total; extends the two WU4 checks, adds none). |
| ER-020 | Candidate rebinding from superseded `b45c3ea2` to current `4d286f7d` (one-line `build.sbt` `organization` delta), backed by re-measurement of the full tier matrix at the current candidate (VR-010 `sqltest` exit 0, VR-011 `db` exit 1, VR-012 `bigdata` exit 0) with every figure matching the `b45c3ea2` measurements exactly; review lineage `review-01cb90ba583acd03` APPROVED and `pre-commit` gate `allow` | verified-fact | docs/publication-readiness/verification.md#candidate-rebinding-record | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Rebinding is measurement-backed, not inferred from the delta. Per-record re-measured / re-verified / carried-forward classification is in the same document's Candidate Rebinding Basis table; no `captured_at` was rewritten for an observation that was not re-made. |
| ER-021 | Scala package namespace remains `io.getquill` in all eight source-bearing modules while the publication coordinate is now `com.e-evolution`; consumers will depend on one string and import another | verified-fact | build.sbt (organization), quill-*/src/main/scala/io/getquill (package declarations) | 2026-08-27 | 4d286f7d | blocked | blocking | unassigned | unassigned | Needs explicit consumer documentation of the coordinate/namespace split. Routed to WU9; not resolved, and no source or coordinate was changed here. |
| ER-022 | `README.md` still advertises `io.getquill` coordinates at version `5.0.0` (lines 47, 49, 51, 53, 55) and declares `kyoVersion = "1.0.0-RC5"` (line 42), while the build is at `com.e-evolution`, version `5.0.0-kyo-RC6`, Kyo `1.0.0-RC6` | verified-fact | README.md lines 42 and 47-55 | 2026-08-27 | 4d286f7d | blocked | blocking | unassigned | unassigned | Stale public claim mapped to the coordinate change. Routed to WU9; `README.md` was not edited by this rebinding. |
| ER-023 | Locally installed artifacts exist for all eight modules under `~/.ivy2/local/io.getquill/<module>_3/5.0.0-kyo-RC6/` and are now orphaned: this build no longer produces that coordinate, and `~/.ivy2/local/com.e-evolution/` does not exist | verified-fact | ~/.ivy2/local (read-only directory listing), build.sbt organization setting | 2026-08-27 | 4d286f7d | blocked | blocking | unassigned | unassigned | A fresh local install under `com.e-evolution` is required before any consumer-resolution claim can be made; that is WU7's work and was deliberately not performed here. Corresponds to the approved review lineage's advisory `R3-groupid-change-unverified` finding. Routed to WU7. |
| ER-024 | `build.sbt` declares `scmInfo` `https://github.com/getkyo/kyo-protoquill` and `homepage` `https://getkyo.io/kyo-quill`; authenticated `gh api repos/getkyo/kyo-protoquill` returns HTTP 404 and the repository HTML page returns 404, and the homepage URL returns HTTP 404 | verified-fact | build.sbt lines 8 and 13-15; authenticated GitHub API and HTTP HEAD queries, 2026-08-27 | 2026-08-27 | 4d286f7d | blocked | blocking | unassigned | unassigned | Release metadata points at non-existent locations. Method note: the unauthenticated GitHub API request returns 403 (rate limiting), not 404; the 404 is from the authenticated call. Routed to WU6; `build.sbt` was not edited. |
| ER-025 | Untracked `scripts/publication-readiness/__pycache__/check_records.cpython-314.pyc` exists and `.gitignore` declares no `__pycache__` or `*.pyc` entry | verified-fact | scripts/publication-readiness/__pycache__ (untracked), .gitignore | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Publication-cleanliness follow-up: Python bytecode should be ignored before any source publication. `.gitignore` was not edited and the file was not deleted. Routed to WU9 as a cleanliness item. |
| ER-026 | Three readiness documents outside this rebinding's authorized path set still carry the superseded candidate binding: `destination-decision.yaml` (`candidate_id: "b45c3ea2"` plus the superseded `candidate_tree`), `provenance.md` (Candidate Binding table line 17), and `modules.md` (Candidate Binding table line 17 and the `verification_state (candidate b45c3ea2)` inventory column header). The decision record's substantive fields were separately re-verified unchanged at `4d286f7d` (decision_status pending, all owners unassigned, all dates pending, scope/conditions pending, selected_destination.status unresolved, both authorization entries not-selected/not-authorized) | verified-fact | docs/publication-readiness/destination-decision.yaml, docs/publication-readiness/provenance.md, docs/publication-readiness/modules.md | 2026-08-27 | 4d286f7d | blocked | blocking | unassigned | unassigned | Candidate bindings are stale in all three files. They lie outside this rebinding's authorized path set, so the mismatch is recorded rather than corrected; their owning units must rebind them. The structural checker compares only the Evidence Index and the Tier tables against the pin, so none of these three is caught mechanically today; a future strengthening should extend the pin comparison to every Candidate Binding table and to the decision record's own `candidate_id`, and is deliberately not added here because it would fail on files this unit may not edit. |
| ER-027 | `docs/publication-readiness/release-manifest.yaml` and `docs/publication-readiness/reconciliation.md` created; 12 manifest fields, 30 `(field, surface)` reconciliation rows, full surface-coverage validated mechanically | verified-fact | docs/publication-readiness/release-manifest.yaml, docs/publication-readiness/reconciliation.md | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | `check_records.py` extended with `release manifest required fields and enums` and `release manifest surface coverage` (15 checks total, 13 prior + 2 new), plus `document candidate binding` extended to also scan `release-manifest.yaml`. RED/GREEN/fixture-rejection cycle recorded in `reconciliation.md`'s Verification section. |
| ER-028 | SemVer precedence conflict: `version := "5.0.0-kyo-RC6"` sorts BELOW the already-published plain `5.0.0` under SemVer 2.0.0 section 11, despite containing strictly newer code; `versionScheme := Some("always")` does not change precedence; recorded as a blocking manifest conflict with three proposed dispositions, none selected | verified-fact | docs/publication-readiness/release-manifest.yaml#semver_precedence, build.sbt lines 16-17 | 2026-08-27 | 4d286f7d | blocked | blocking | unassigned | unassigned | Carried from the 2026-08-27 Judgment Day review per this unit's task text. No version is selected by this record; routed for a maintainer decision. |
| ER-029 | Release-identity facts: `HEAD` is untagged; local tag `v5.0.0` points at `acc60185`, no longer an ancestor of `master`; `git ls-remote --tags origin` returns empty (origin has no tags at all) | verified-fact | local git-tag state and `git ls-remote` (read-only), 2026-08-27 | 2026-08-27 | 4d286f7d | blocked | blocking | unassigned | unassigned | Recorded as observed release-identity facts per this unit's task text; no tag was created or moved. Tagging is gated on `fields.version` (ER-028) and on the still not-selected/not-authorized `authorization.github_release`. |
| ER-030 | CHANGELOG.md's newest entry (`# Kyo Quill 5.0.0`) describes RC5 content only, with no entry for RC6 or for the groupId change; CHANGELOG.md line 1 routes release notes to the same nonexistent `https://github.com/getkyo/kyo-protoquill/releases` repository confirmed dead by ER-024 | verified-fact | CHANGELOG.md line 1 and newest entry heading | 2026-08-27 | 4d286f7d | blocked | blocking | unassigned | unassigned | CHANGELOG.md was not edited by this unit; remediation is WU9's. |
| ER-031 | Maven Central coordinate state: `io.getquill`'s `quill-sql_3` has 45 published versions (latest `4.8.6`) owned by the upstream Quill project, unrelated to this fork; neither `5.0.0` nor `5.0.0-kyo-RC6` exists under it; `com.e-evolution` has zero Maven Central artifacts; `e-evolution.com` resolves (HTTP 200) as attributable reverse-DNS but grants no Sonatype namespace authority by itself | verified-fact | Maven Central repository listing (read-only), 2026-08-27 | 2026-08-27 | 4d286f7d | blocked | blocking | unassigned | unassigned | No publication or namespace claim was made or attempted. Namespace authority remains unassigned; routed to WU11. |
| ER-032 | Tier 3 local-publication and consumer-check records created; 3 verification headings added to `check_records.py`, extending the existing completeness/staleness checks (15 checks total, unchanged count — no new named check) | verified-fact | docs/publication-readiness/verification.md#tier-3-local-only-publication-dry-run-and-consumer-checks-wu7, scripts/publication-readiness/check_records.py | 2026-08-27 | 4d286f7d | stale | advisory | unassigned | unassigned | RED/GREEN/TRIANGULATE/REFACTOR cycle recorded in verification.md's `## WU7 Verification (Tier 3)` section. |
| ER-033 | This machine cannot execute a signed or credentialed publication: no `credentials`/`sonatype` file under `~/.sbt`, no `~/.sbt/1.0/*.sbt`, no `SONATYPE_*`/`PGP_*`/`GPG_*` environment variable, and `gpg`/`gpg2` are not installed at all (stronger than "zero secret keys"); `sbt-ci-release` 1.11.1 is installed, so the publication tasks exist and only credentials/signing are absent | verified-fact | ~/.sbt (read-only), env (read-only), gpg/gpg2 lookup, project/plugins.sbt:9 | 2026-08-27 | 4d286f7d | stale | blocking | unassigned | unassigned | Upload-guard evidence for this unit; only `publishLocal` was run. No credential was created, read, or used. |
| ER-034 | `publishLocal` for all 8 published modules succeeded into a newly created, empty, isolated `sbt.ivy.home` directory; the shared `~/.ivy2/local/com.e-evolution` remained absent and `~/.ivy2/local/io.getquill`'s mtime was unchanged before/after, confirming zero writes to the shared cache | verified-fact | docs/publication-readiness/verification.md#tier-3-local-publication-isolated-publishlocal-evidence (VR-013) | 2026-08-27 | 4d286f7d | stale | blocking | unassigned | unassigned | Defeats the false-green hazard named in this unit's task text: the pre-existing orphaned `io.getquill` install cannot be mistaken for evidence about the new coordinate. |
| ER-035 | Dependency-scope audit of all 8 generated POMs: every JDBC driver test-scoped correctly; zero compile-scope leak found; `io.getquill:quill-engine_3`/`quill-util_3` confirmed compile-scoped in `quill-sql` only, as legitimate upstream Quill dependencies | verified-fact | docs/publication-readiness/verification.md#tier-3-artifact-identity-dependency-scope-license-and-content-inspection (VR-015) | 2026-08-27 | 4d286f7d | stale | blocking | unassigned | unassigned | GREEN; no candidate-blocking action. Closes the specific hazard this unit's task text named. |
| ER-036 | Generated POM license/organization/SCM metadata reproduces `build.sbt`'s dead `https://getkyo.io/kyo-quill` homepage and dead `https://github.com/getkyo/kyo-protoquill` SCM connection (ER-024), and the `deusaquilus`-only `<developers>` attribution gap (ER-009), confirming both propagate unchanged into every publishable artifact | verified-fact | docs/publication-readiness/verification.md#tier-3-artifact-identity-dependency-scope-license-and-content-inspection (VR-016) | 2026-08-27 | 4d286f7d | stale | blocking | unassigned | unassigned | Not a new defect; confirms propagation into artifacts, previously unverified. Routed to WU6/WU9; `build.sbt`/`LICENSE.txt` not edited. |
| ER-037 | Archive-content and secret/path scan across all 8 generated POMs and jars found zero unrelated files and zero absolute local paths or credentials | verified-fact | docs/publication-readiness/verification.md#tier-3-artifact-identity-dependency-scope-license-and-content-inspection (VR-017, VR-018) | 2026-08-27 | 4d286f7d | stale | blocking | unassigned | unassigned | GREEN; no candidate-blocking action. |
| ER-038 | Consumer isolation proof: a fixture resolved against the default, unmodified `~/.ivy2` fails with `ResolveException` citing both the empty shared-cache path and a Maven Central 404 for `com.e-evolution` (matching ER-031); the same fixture resolved against the isolated `publishLocal` output compiles and runs, proving resolution came only from the isolated repository | verified-fact | docs/publication-readiness/verification.md#tier-3-consumer-resolution-and-compilerun-evidence (VR-019, VR-020) | 2026-08-27 | 4d286f7d | stale | blocking | unassigned | unassigned | GREEN. The isolation hazard named in this unit's task text is defeated by a negative control, not asserted. |
| ER-039 | Two representative consumer fixtures with different dependency graphs and query shapes (`quill-jdbc-kyo` transitive graph plus a compile-only `io.getquill.context.qkyo.KyoJdbcContext` type reference; `quill-sql`-only direct graph) both compile and run successfully depending on `com.e-evolution` while importing `io.getquill`, exercising the coordinate/package split recorded at ER-021 | verified-fact | docs/publication-readiness/verification.md#tier-3-consumer-resolution-and-compilerun-evidence (VR-020, VR-021) | 2026-08-27 | 4d286f7d | stale | advisory | unassigned | unassigned | Triangulation: the result generalizes across two distinct fixtures, not one. |
| ER-040 | `docs/publication-readiness/governance/contributing.md` created; contribution expectations topic recorded `unresolved`, owner/approver `unassigned`; inherited `.github/ISSUE_TEMPLATE.md`/`PULL_REQUEST_TEMPLATE.md` `@getquill/maintainers` foreign-team mention and `getquill.io` link recorded as findings, not remediated | verified-fact | docs/publication-readiness/governance/contributing.md | 2026-08-27 | 4d286f7d | current | blocking | unassigned | unassigned | decision remains unresolved; readiness stays blocked until a named owner/approver or a complete waiver is recorded. Remediation of the inherited template findings is routed to WU9. |
| ER-041 | `docs/publication-readiness/governance/code-of-conduct.md` created; code-of-conduct expectations topic recorded `unresolved`, owner/approver `unassigned`; defers private reporting to security.md rather than inventing a second channel | verified-fact | docs/publication-readiness/governance/code-of-conduct.md | 2026-08-27 | 4d286f7d | current | blocking | unassigned | unassigned | decision remains unresolved; readiness stays blocked until a named owner/approver or a complete waiver is recorded. |
| ER-042 | `docs/publication-readiness/governance/security.md` created; private security-reporting guidance topic recorded `unresolved`, owner/approver `unassigned`; no security contact address, name, or response-time commitment invented — `e-evolution.com` recorded only as an observed domain (ER-031), not a confirmed contact | verified-fact | docs/publication-readiness/governance/security.md | 2026-08-27 | 4d286f7d | current | blocking | unassigned | unassigned | decision remains unresolved; readiness stays blocked until a named owner/approver or a complete waiver is recorded. |
| ER-043 | `docs/publication-readiness/governance/support-maintenance.md` created; support and maintenance boundaries topic recorded `unresolved`, owner/approver `unassigned`; no SLA or maintenance-cadence commitment invented; `scala-steward.yml`'s daily-cron dependency-update review ownership recorded as an open question | verified-fact | docs/publication-readiness/governance/support-maintenance.md | 2026-08-27 | 4d286f7d | current | blocking | unassigned | unassigned | decision remains unresolved; readiness stays blocked until a named owner/approver or a complete waiver is recorded. |
| ER-044 | `docs/publication-readiness/governance/release-controls.md` created; issue ownership, release responsibilities, CI/release separation, approval evidence, and abort/recovery ownership all recorded `unresolved`, owner/approver `unassigned`. Historical unauthorized-reachability observation: `ci.yml` formerly used the `release` job condition `github.event_name != 'pull_request'`; `release-drafter.yml` triggers on every push to `master` and creates/updates a GitHub Release draft via `GITHUB_TOKEN`; `dependency-graph.yml` has no JDK-25 pin | verified-fact | docs/publication-readiness/governance/release-controls.md, .github/workflows/ci.yml, .github/workflows/release-drafter.yml, .github/workflows/scala-steward.yml, .github/workflows/dependency-graph.yml | 2026-08-27 | 4d286f7d | stale | blocking | unassigned | unassigned | Historical observation retained. Superseded by ER-054 for current reachability: ER-054 records the condition change and its static proof; this record does not state current ordinary-push `ci-release` reachability. Governance topics remain unresolved; no workflow file was edited by this unit. |
| ER-045 | Governance structural checks (`governance required topics`, `governance topic authority`, `governance reference readback`) added to `check_records.py`, validated against RED (missing governance directory), GREEN (5 conforming drafts), and 4 injected invalid fixtures covering a missing topic, a `named` row with `owner: unassigned`, an `unresolved` row with a named owner, and a dangling reference-link path (all rejected, then removed) | verified-fact | scripts/publication-readiness/check_records.py | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Checker mechanism record for WU8, matching the ER-006/ER-017/ER-027 pattern from prior units; no blocking finding for the checker itself. |
| ER-046 | `public claim coverage` check added to `check_records.py`, validating a new `## Public Claim Coverage` table against the Evidence Index, WU9's maintainer-approved documentation-path allowlist, and a `status` enum; WU9's approved documentation paths added to the existing forbidden-remote-mutation/excluded-path-overlap scan (19 checks total, 18 prior + 1 new) | verified-fact | scripts/publication-readiness/check_records.py | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Checker mechanism record for WU9, matching the ER-006/ER-017/ER-027/ER-032/ER-045 pattern from prior units; no blocking finding for the checker itself. |
| ER-047 | `README.md` corrected: `kyoVersion` `1.0.0-RC5` -> `1.0.0-RC6` (line 42); install coordinates `io.getquill %% quill-* % 5.0.0` -> `com.e-evolution %% quill-* % 5.0.0-kyo-RC6` (lines 47-55) with an added note that the coordinate resolves only from a local `sbt publishLocal` build, never Maven Central; the intro claim of "preserving all Quill functionality" qualified to reference the measured Verification Status section instead of an unbounded parity claim | verified-fact | README.md (edited, maintainer-approved WU9 path) | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Closes the README portion of ER-022's blocking finding and qualifies the parity claim flagged by this unit's own audit; no `build.sbt`, tag, or publication action taken. |
| ER-048 | `README.md` extended: a package-namespace/coordinate-split note (`com.e-evolution` dependency coordinate vs. the unchanged `io.getquill` import namespace) added to the install section, and a new `## Verification Status` section added citing the measured `sqltest` 668/668, `db` 535/534 (1 pre-existing failure), and `bigdata` 190/190 baselines and the Homebrew OpenJDK 25.0.4 environment finding | verified-fact | README.md (edited, maintainer-approved WU9 path) | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Documents the coordinate/namespace split proven at ER-021/ER-038/ER-039 and the measured baselines at ER-015/ER-018/ER-019/ER-020 for a public audience; no new measurement was taken by this unit. |
| ER-049 | `README.md` Requirements section corrected: Kyo `1.0.0-RC5` -> `1.0.0-RC6` (3 occurrences); the "stay on `4.8.8`" JDK-17 fallback claim corrected after a read-only Maven Central metadata query found `io.getquill`'s `quill-jdbc_3` latest published version is `4.8.6`, not `4.8.8`, and reworded to clarify this fork does not publish that legacy coordinate at all | verified-fact | README.md (edited, maintainer-approved WU9 path); Maven Central `io/getquill/quill-jdbc_3/maven-metadata.xml`, queried 2026-08-27 | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | New finding, not carried from a prior work unit: the previous README claim exceeded even the upstream project's own published evidence. |
| ER-050 | `CHANGELOG.md` corrected: the dead-repository release-notes link (matching ER-024's finding) replaced with an accurate statement that this fork is `e-Evolution/kyo-protoquill` and has no published GitHub Release; a new `Unreleased` entry added for the Kyo RC6 bump and groupId change, explicitly marked not tagged, not published, and not release-authorized | verified-fact | CHANGELOG.md (edited, maintainer-approved WU9 path) | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Closes the CHANGELOG portion of ER-024's and ER-030's blocking findings; no GitHub Release, tag, or Maven Central action was taken or promised. |
| ER-051 | `.github/ISSUE_TEMPLATE.md` and `.github/PULL_REQUEST_TEMPLATE.md` corrected: `@getquill/maintainers` foreign-organization mention and the `getquill.io`/`fwbrasil` scastie link removed and replaced with neutral, non-invented language routing to `governance/release-controls.md`; no owner, approver, contact address, or response-time commitment invented | verified-fact | .github/ISSUE_TEMPLATE.md (edited, maintainer-approved WU9 path), .github/PULL_REQUEST_TEMPLATE.md (edited, maintainer-approved WU9 path) | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Closes the template-remediation portion of ER-040's blocking finding; disposition remains `unresolved`/`unassigned` in `contributing.md`'s Governance Topic Matrix, unchanged by this unit. |
| ER-052 | Historical banners strengthened on `FINAL_STATUS_REPORT.md`, `MIGRATION_PLAN.md`, `MIGRATION_REPORT.md`, `VALIDATION_REPORT_FINAL.md`: each now names the superseded era (Kyo `1.0-RC1`, Scala `3.8.1`, JDK `17+`) and points to `verification.md`/`modules.md` for current evidence; `FINAL_STATUS_REPORT.md`'s banner additionally names and corrects the false `"1,171 tests passing"` figure against the measured `sqltest`/`db`/`bigdata` baselines | verified-fact | FINAL_STATUS_REPORT.md (edited, maintainer-approved WU9 path), MIGRATION_PLAN.md (edited, maintainer-approved WU9 path), MIGRATION_REPORT.md (edited, maintainer-approved WU9 path), VALIDATION_REPORT_FINAL.md (edited, maintainer-approved WU9 path) | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Closes ER-013's blocking finding per the maintainer's standing decision to keep these files and add a banner rather than delete or rewrite their bodies; no body content was changed. |
| ER-053 | `scripts/publication-readiness/event_graph.py` created (static event -> job graph walk, no third-party dependency) and `check_records.py` extended with the `event graph reachability` check plus a `## Tier 4 — Publication Reachability Fixture Evidence` heading appended to `VERIFICATION_TABLE_HEADINGS` (20 checks total, 19 prior + 1 new); RED (exit 1 against the unfixed workflows), GREEN (exit 0 against the fixed workflows), and two mutation-injection tests (an injected publication step correctly detected in a scratchpad copy of `release-drafter.yml`; the new named check itself correctly fails against a scratchpad copy of the original `ci.yml`) all recorded in `verification.md`'s `## WU10 Verification (Tier 4)` section | verified-fact | scripts/publication-readiness/event_graph.py, scripts/publication-readiness/check_records.py | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Checker mechanism record for WU10, matching the ER-006/017/027/032/045/046 pattern from prior units; no blocking finding for the checker itself. |
| ER-054 | `.github/workflows/ci.yml`'s `release` job condition changed from `github.event_name != 'pull_request'` to `github.event_name == 'release'`; the static event-graph proof (VR-050 to VR-053) shows `ordinary_push` was reachable to sbt's `ci-release` task with all four Sonatype/PGP secrets before this fix and is unreachable after it, while `pull_request` was already unreachable and an unauthorized `release` sub-event is blocked earlier still by the workflow's own `types: [published]` trigger filter | verified-fact | .github/workflows/ci.yml, docs/publication-readiness/verification.md#tier-4-publication-reachability-fixture-evidence | 2026-08-27 | 4d286f7d | current | blocking | unassigned | unassigned | Closes the `ci.yml` portion of ER-044's unauthorized-reachability finding. No replacement publication workflow was added or enabled; no build-coverage step was removed. |
| ER-055 | Confirmed the `GETQUILL_SONATYPE_TOKEN_*` -> `SONATYPE_*` secret rename (already applied by the 2026-08-27 reconciliation, not by this unit) creates no new reachable path and no silently-unset-secret path: both name forms resolve to an empty string via GitHub's `secrets.<NAME>` syntax when the named repository secret does not exist, so reachability is gated exclusively by ER-054's `if:` condition fix, independent of the secret names in scope | verified-fact | .github/workflows/ci.yml (lines declaring `SONATYPE_PASSWORD`/`SONATYPE_USERNAME`), docs/publication-readiness/verification.md#tier-4-ci-verification-vs-publication-reachability-wu10 | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Confirmation only, per this unit's audit checklist; no secret name was changed by this unit. |
| ER-056 | S03 supersedes the unsupported JDK-25 amendment claim: the cited candidate blob has no `actions/setup-java` step. The workflow's origin and any amendment outside the cited object history remain unresolved. | verified-fact | docs/publication-readiness/provenance.md#dependency-graph-provenance-s03 | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | No workflow file changed; this record corrects provenance wording only. |
| ER-057 | Least-privilege `permissions:` blocks added: `ci.yml` workflow-level `contents: read` and `release-drafter.yml` workflow-level `contents: write`, replacing each workflow's previous implicit default; `dependency-graph.yml`'s existing `contents: write` confirmed already minimal for `scalacenter/sbt-dependency-submission@v3` and left unchanged; `scala-steward.yml` left unchanged (already GitHub-App-scoped, the most restrictive option available) | verified-fact | .github/workflows/ci.yml, .github/workflows/release-drafter.yml, .github/workflows/dependency-graph.yml, .github/workflows/scala-steward.yml | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | REFACTOR step of this unit's cycle; no build-coverage or reachability change. |
| ER-058 | CI-group parity confirmed: `ci.yml`'s `build` job `strategy.matrix.module` list (`sqltest`, `db`, `bigdata`) and its `./build/build.sh` step invocation are byte-for-byte unchanged by this unit; only the `release` job's `if:` condition and the workflow-level `permissions:` block changed, so no `sqltest` rerun was required under this unit's own verification clause | verified-fact | .github/workflows/ci.yml (build job), docs/publication-readiness/verification.md#ci-group-parity-record | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | `./build/build.sh` was not invoked by this unit at all. |
| ER-059 | `destination-decision.yaml` updated: `selected_destination.source_publication.state` moved `not-selected` -> `selected` (destination proposed) while `.authorized` stays `not-authorized`; `selected_destination.status` moved `unresolved` -> `proposed`; `upstream_contribution`/`maven_release`/`github_release` and both `authorization.*` entries stay `not-selected`/`not-authorized`; all seven `owners.*` stay `unassigned` and both `dates.*` stay `pending` (no person named anywhere in this change); `scope`/`conditions` populated with the source-publication-only boundary and the requirement that any future Maven Central or GitHub Release proposal needs a separate SDD change and separate authorization | verified-fact | docs/publication-readiness/destination-decision.yaml | 2026-08-27 | 4d286f7d | current | blocking | unassigned | unassigned | `decision_status` remains `pending`; destination-specific release work (Maven Central, GitHub Release, upstream contribution) stays blocked until a named `decision_owner` and every applicable owner/approver complete this record, per spec.md's Destination Decision Is Explicit and Blocking requirement. Routed to a future destination-specific SDD change; not resolved here. |
| ER-060 | Five negative-test fixtures derived from the WU11-updated `destination-decision.yaml` were run individually via `check_records.py --yaml-file <fixture>` and outside tracked paths: (1) `owners.security_contact_owner` deleted -> `destination-decision required fields` rejects with `owners.security_contact_owner missing`; (2) `evidence_links` deleted -> the same check rejects with `missing field: evidence_links`; (3) `decision_status` flipped to `approved` with every owner/date left at its default -> `destination-decision unresolved defaults` rejects with all 7 `owners.*` and both `dates.*` violations; (4) `authorization.maven_central.state`/`.authorized` flipped to `selected`/`authorized` while `decision_status` stays `pending` -> the same check rejects with both violations; (5) `decision_status` set to the disallowed value `complete` -> `destination-decision allowed enum values` rejects. All five exited `1` with exactly the predicted violation list and zero false positives; the real file was re-verified clean (exit `0`, 20/20 `PASS`) both before and after; no fixture was staged, committed, or placed under a tracked path | verified-fact | scripts/publication-readiness/check_records.py (`check_required_fields`, `check_enums`, `check_unresolved_defaults`) | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Negative-test mechanism record for WU11, matching the ER-006/017/027/032/045/046/053 pattern from prior units; no existing check was weakened, deleted, or bypassed. |
| ER-061 | No clean-checkout or CI-run verification evidence exists for candidate `4d286f7d`; every Tier 0-4 record in `verification.md` (ER-014..019, ER-032..039, ER-053..058) is local-machine evidence collected against a normal local clone, not an isolated clean checkout | verified-fact | docs/publication-readiness/verification.md (Tier 0-4 sections, all local-machine execution) | 2026-08-27 | 4d286f7d | stale | blocking | unassigned | unassigned | This gap is itself a blocking record per spec.md's "Verification Is Reproducible and Candidate-Bound" requirement; closing it requires either a CI run against this exact candidate or an explicit, attributed, accepted-risk waiver from a named maintainer. Not resolved by this unit. |
| ER-062 | WU12 final excluded-path and checkout reconfirmation against the WU1 preflight record found no mismatch: `.codegraph/` and `pi-session-2026-05-16T23-45-09-146Z_019e332d-f01a-7def-b488-5bc856c8bb20.html` remain untracked/unmodified; `build.sbt` shows no further modification; local tag `v5.0.0` still resolves to the same non-ancestor commit; `origin/master` divergence is 33 ahead / 0 behind at `4d286f7d` (one additional commit beyond WU1's 32-ahead record, matching the recorded groupId change); no new local or remote ref was found | verified-fact | docs/publication-readiness/readiness-report.md#excluded-path-and-checkout-reconfirmation-final-vs-wu1 | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Positive reconfirmation, not a repair; per this unit's instructions any mismatch would have been recorded as a blocking invalidation instead. |
| ER-063 | Review-line accounting confirmed for every proposed PR slice across WU1-WU11: each slice measures within the 400-authored-line `feature-branch-chain` budget (WU2 and WU8 each split into two slices), and no work unit has ever claimed `size:exception` | verified-fact | docs/publication-readiness/readiness-report.md#review-line-accounting-feature-branch-chain-400-line-budget, openspec/changes/prepare-kyo-quill-publication/tasks.md (WU2-WU11 evidence blocks) | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Mechanism/accounting record for WU12; no new split or exception was introduced. |
| ER-064 | `readiness report requirement and claim coverage` check added to `check_records.py` (21 checks total, 20 prior + 1 new), validated against RED (missing `readiness-report.md`, exit `1`) and GREEN (full 8-requirement/15-claim coverage matrix present, exit `0`, 21/21 `PASS`); `document candidate binding` extended to also scan `readiness-report.md` | verified-fact | scripts/publication-readiness/check_records.py | 2026-08-27 | 4d286f7d | current | advisory | unassigned | unassigned | Checker mechanism record for WU12, matching the ER-006/017/027/032/045/046/053/060 pattern from prior units; no existing check was weakened, deleted, or bypassed. |

## WU2 Schema/Readback Verification Results

- **RED**: `python3 scripts/publication-readiness/check_records.py` executed
  before this file and `destination-decision.yaml` existed; it exited `1`
  with 6 of 8 checks reporting `missing file`.
- **GREEN**: after creating this file and `destination-decision.yaml`, the
  same command exited `0` with all 8 checks reporting `PASS`.
- **Fixture rejection**: an invalid `destination-decision.yaml` fixture was
  written outside tracked paths (under the session scratchpad) with
  `decision_status: approved` while every owner stayed `unassigned` and one
  `authorization` entry was flipped to `selected`/`authorized`. Running
  `check_records.py --yaml-file <fixture path>` against it exited `1` and
  reported both the `unresolved defaults` violation and the
  `not-selected`/`not-authorized` violation. The fixture was then removed;
  it was never staged, committed, or placed under a tracked path.
- **Runtime scenario**: `N/A` — schema validation has no application
  runtime boundary; the structural checker itself is the verification
  instrument for this work unit.
    | ER-065 | Fresh S06 Temurin 25 normal-clone observation: the supplied independent clone remained detached at `4d286f7d9172f452fd3d0ae2458ef73c11376fea` / tree `8924989ad8d3d676aff73c30ab93ad31878e7385` with zero remotes before and after each command; Eclipse Adoptium Temurin 25.0.4+7 was proven by the JDK probe and SBT banner; `sbt compile` and `quill-sql/test; quill-sql-tests/test` both exited 0 (942 core tests succeeded, 1 ignored) | verified-fact | docs/publication-readiness/verification.md#s06-fresh-temurin-normal-clone-observation | 2026-08-30 | 4d286f7d | current | blocking | unassigned | unassigned | Fresh evidence supports blockers 6 and 7 only; it does not close either without explicit owner/approver disposition. The auxiliary SBT proof command's invalid `show javaVersion` key exited 1 but emitted the definitive Eclipse Adoptium banner; no result is invented. |
        | ER-066 | S07 read-only publication-branch comparison: `b45c3ea2` is the merge-base and ancestor of `4d286f7d`; the committed delta is one `build.sbt` modification with 1 addition and 1 deletion. | verified-fact | docs/publication-readiness/reconciliation.md#s07-publication-branch-reconciliation-plan | 2026-08-30 | 4d286f7d | current | blocking | unassigned | unassigned | Comparative planning evidence only. `plan_only: true` and `mutation_authorized: false`; all seven blockers and `not-ready` remain unchanged, and a future ref operation requires explicit user authority. |
        | ER-067 | S11 governance ownership matrix: contribution, security, conduct, support/maintenance, and release-governance topics retain explicit unresolved ownership and approval gaps. | verified-fact | docs/publication-readiness/governance/{contributing,security,code-of-conduct,support-maintenance,release-controls}.md | 2026-08-30 | 4d286f7d | current | blocking | unassigned | unassigned | Governance records remain unresolved; each topic requires a named owner and approver decision. Aggregate `not-ready` and all seven blockers remain open. |
            | ER-068 | S12 public-claim readback: current README and changelog claims remain evidence-bounded; historical RC1 reports are visibly superseded; templates retain unassigned governance; and the `zio-json` to Kyo Schema/JSON codec migration remains deferred. | verified-fact | README.md, CHANGELOG.md, FINAL_STATUS_REPORT.md, MIGRATION_PLAN.md, MIGRATION_REPORT.md, VALIDATION_REPORT_FINAL.md, .github/ISSUE_TEMPLATE.md, .github/PULL_REQUEST_TEMPLATE.md | 2026-08-30 | 4d286f7d | current | blocking | unassigned | unassigned | Public documentation does not grant readiness, ownership, approval, release, publication, or JSON-migration completion. Aggregate `not-ready` and all seven blockers remain open. |

## S13 CI-Safety Static Event-Graph Evidence

`event_graph.py` reads workflow text only. Its fixtures prove that ordinary push,
pull request, unauthorized release, and unscoped publication secrets are rejected;
an authorized published-release route is classified; and dependency submission is a
repository mutation, not artifact publication. Current-workflow readback reports no
ordinary-push, pull-request, or unauthorized-release artifact-publication route;
ordinary push retains the separately classified `dependency-submission` and
`release-draft` mutations. This static evidence grants no workflow, secret, release,
publication, remote, or delivery authority. Aggregate `not-ready` and all seven
blockers remain unchanged.

| id | subject | classification | source | captured_at | candidate_id | status | severity | owner | approver | disposition |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ER-069 | S13 static CI event-graph fixture and current-workflow classification | verified-fact | scripts/publication-readiness/event_graph.py, scripts/publication-readiness/fixtures/, .github/workflows/*.yml | 2026-08-30 | 4d286f7d | current | blocking | unassigned | unassigned | Static classification only; it distinguishes dependency submission from artifact publication and preserves `not-ready` with all seven blockers open. |
| ER-070 | S14 bounded CI workflow correction: explicit published-release guard and truthful dependency-graph setup comment | verified-fact | .github/workflows/ci.yml, .github/workflows/release-drafter.yml, .github/workflows/dependency-graph.yml, docs/publication-readiness/verification.md | 2026-08-30 | 4d286f7d | current | blocking | unassigned | unassigned | Static correction only: ordinary push, pull request, and unauthorized release remain non-publication routes; dependency submission and release drafting remain repository mutations, not artifact publication. `not-ready` and all seven blockers remain open. |

## S14 CI Workflow Correction Evidence

S14 narrows the release-job guard to an explicit published-release event/action
pair and removes an unsupported dependency-graph comment that linked build setup
to fork-push history. The build matrix, Temurin 25 setup, permissions, secret
scope, release-drafter classification, and dependency-submission classification
remain bounded as recorded by S13. This is static evidence only: it creates no
owner, approver, sign-off, delivery, release, publication, or secret authority.
Aggregate status remains `not-ready` with all seven blockers open.

## S15 Final Aggregation Evidence

| id | subject | classification | source | captured_at | candidate_id | status | severity | owner | approver | disposition |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ER-071 | S15 fail-closed seven-blocker aggregation and cross-reference readback | verified-fact | docs/publication-readiness/readiness-report.md#s15-final-fail-closed-aggregation, docs/publication-readiness/verification.md#s15-final-verification | 2026-08-30 | 4d286f7d | current | blocking | unassigned | unassigned | Aggregation preserves seven open blockers and `not-ready`; `plan_only: true`, `mutation_authorized: false`, deferred JSON migration, and protected delivery/publication authority remain unchanged. |
