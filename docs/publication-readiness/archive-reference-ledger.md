# Archive Reference Ledger

## Decision

The archived publication-readiness record remains immutable. This ledger inventories
former active-change paths and preserves the current `not-ready` disposition and all
seven open blockers; it neither closes evidence nor authorizes an archive correction.

## Archived Active-Path Reference Inventory

| containing_artifact | former_target | archived_target | historical_meaning | disposition |
| --- | --- | --- | --- | --- |
| `openspec/changes/archive/2026-08-27-prepare-kyo-quill-publication/proposal.md` | `openspec/changes/prepare-kyo-quill-publication/proposal.md` | `openspec/changes/archive/2026-08-27-prepare-kyo-quill-publication/proposal.md` | The rollback statement described deleting the proposal while the change was active; it remains accurate historical context after archival. | intentionally-historical |

## Disposition Readback

- **correctable** requires separately authorized, file-level archive editing; no such
  correction is authorized by S01.
- **intentionally-historical** preserves a former active path because changing it would
  alter the record's original meaning.
- **unresolved** remains open until a bounded, attributable disposition is recorded.

The inventory contains one in-scope match. Its archived target resolves, no archived
bytes were changed, and S01 grants no commit, ref, remote, release, or publication
authority.

## S02 Canonicalization Audit Record

| Field | Value |
| --- | --- |
| archived_artifact | `openspec/changes/archive/2026-08-27-prepare-kyo-quill-publication/proposal.md` |
| original_value | `openspec/changes/prepare-kyo-quill-publication/proposal.md` |
| replacement_value | `openspec/changes/archive/2026-08-27-prepare-kyo-quill-publication/proposal.md` |
| s01_classification | `intentionally-historical` |
| user_decision | **“Reescribir el enlace”**, made with notice of semantic impact. |
| rationale | Restore present-day canonical path integrity while retaining S01's historical interpretation in this audit trail. |
| authorized_scope | Exactly the sole matching reference in `openspec/changes/archive/2026-08-27-prepare-kyo-quill-publication/proposal.md` and this one audit record in `docs/publication-readiness/archive-reference-ledger.md`. |
| verification_outcome | PASS — former value count changed from 1 to 0; canonical value count is 1 and resolves; this complete S02 record is unique; only the two authorized product paths changed; authored additions plus deletions are within 400. |

S02 preserves `not-ready`, all seven open blockers, and S01's intentionally-historical
interpretation. It grants no commit, branch, PR, ref, remote, release, publish, or
delivery authority.
