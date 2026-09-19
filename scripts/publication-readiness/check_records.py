#!/usr/bin/env python3
"""Fail-closed structural checker for publication-readiness records."""
from __future__ import annotations

import argparse
import importlib.util
import re
import sys
import unittest
from unittest.mock import MagicMock, patch

from pathlib import Path
from typing import Callable

DEFAULT_ROOT = Path(__file__).resolve().parents[2]
DOCS = Path("docs/publication-readiness")
CANDIDATE = "4d286f7d"
AMENDMENT_VALIDATIONS = (
    "archive-ledger integrity",
    "dependency-graph provenance remediation",
    "release-controls remediation",
    "ER-044 supersession",
    "S04 candidate boundary classification",
    "S04 authoritative stale statuses",
    "amendment cross-record registration",
    "S07 plan-only reconciliation record",
    "S08 evidence schema core",
    "S09 slice integrity and operation safety",
        "S10 destination and release authority controls",
        "S11 governance ownership records",
            "S12 public claim coverage",
                "S13 CI-safety event graph",
                "S14 CI workflow safety",
        "S15 final aggregation and cross-reference",
        "VERIFY-R1 evidence remediation",
                )



def read(root: Path, relative: str) -> str:
    path = root / relative
    if not path.is_file():
        raise ValueError(f"missing required record: {relative}")
    return path.read_text(encoding="utf-8")


def require(text: str, *needles: str) -> None:
    missing = [needle for needle in needles if needle not in text]
    if missing:
        raise ValueError("missing required content: " + ", ".join(missing))


def markdown_rows(text: str, heading: str) -> list[list[str]]:
    marker = f"\n## {heading}\n"
    if marker not in text:
        raise ValueError(f"missing heading: ## {heading}")
    section = text.split(marker, 1)[1].split("\n## ", 1)[0]
    rows = [line for line in section.splitlines() if line.lstrip().startswith("|")]
    if len(rows) < 3:
        raise ValueError(f"missing table rows under: ## {heading}")
    return [[cell.strip() for cell in row.strip().strip("|").split("|")] for row in rows[2:]]



def flat_yaml(text: str) -> dict[str, str]:
    values: dict[str, str] = {}
    for line in text.splitlines():
        if not line or line.startswith(("#", " ")) or ":" not in line:
            continue
        key, value = line.split(":", 1)
        values[key.strip()] = value.strip().strip('"')
    return values


def check_required_records(root: Path) -> None:
    for relative in (
        "docs/publication-readiness/evidence.md",
        "docs/publication-readiness/verification.md",
        "docs/publication-readiness/modules.md",
        "docs/publication-readiness/provenance.md",
        "docs/publication-readiness/readiness-report.md",
        "docs/publication-readiness/reconciliation.md",
        "docs/publication-readiness/destination-decision.yaml",
        "docs/publication-readiness/release-manifest.yaml",
    ):
        read(root, relative)


def check_candidate_bindings(root: Path) -> None:
    for relative in ("evidence.md", "verification.md", "modules.md", "provenance.md", "readiness-report.md", "reconciliation.md"):
        require(read(root, str(DOCS / relative)), "candidate_id", CANDIDATE)


def check_evidence_index(root: Path) -> None:
    rows = markdown_rows(read(root, str(DOCS / "evidence.md")), "Evidence Index")
    if not any(row and row[0].startswith("ER-") for row in rows):
        raise ValueError("Evidence Index has no ER records")


def check_evidence_classifications(root: Path) -> None:
    text = read(root, str(DOCS / "evidence.md"))
    require(text, "verified-fact", "inference", "unresolved-question")


def check_evidence_statuses(root: Path) -> None:
    text = read(root, str(DOCS / "evidence.md"))
    require(text, "`current`", "`stale`", "`blocked`")


def check_destination_record(root: Path) -> None:
    values = flat_yaml(read(root, str(DOCS / "destination-decision.yaml")))
    for key in ("schema_version", "candidate_id", "candidate_tree", "decision_status", "selected_destination.status"):
        if not values.get(key):
            raise ValueError(f"destination record missing {key}")
    if values["decision_status"] == "approved":
        raise ValueError("destination approval is unsupported by common recovery")


def check_manifest_fields(root: Path) -> None:
    values = flat_yaml(read(root, str(DOCS / "release-manifest.yaml")))
    fields = ("version", "kyo_version", "scala_version", "java_version", "tag", "coordinates", "modules", "readme_coordinates", "changelog", "homepage", "scm", "license")
    for field in fields:
        for suffix in ("observed", "proposed", "state", "severity", "owner", "applicable_surfaces", "disposition"):
            if not values.get(f"fields.{field}.{suffix}"):
                raise ValueError(f"manifest missing fields.{field}.{suffix}")


def manifest_states_are_valid(values: dict[str, str]) -> bool:
    states = {value for key, value in values.items() if key.endswith(".state")}
    return states <= {"observed", "proposed", "unresolved"}


def check_manifest_states(root: Path) -> None:
    values = flat_yaml(read(root, str(DOCS / "release-manifest.yaml")))
    if not manifest_states_are_valid(values):
        raise ValueError("manifest contains unsupported state")


def check_manifest_reconciliation(root: Path) -> None:
    rows = markdown_rows(read(root, str(DOCS / "reconciliation.md")), "Reconciliation Results")
    if not any(row and row[0] == "version" for row in rows):
        raise ValueError("reconciliation lacks version coverage")


def check_module_inventory(root: Path) -> None:
    rows = markdown_rows(read(root, str(DOCS / "modules.md")), "Module Inventory")
    if len(rows) != 9:
        raise ValueError(f"module inventory must contain 9 rows, found {len(rows)}")


def check_residual_zio_classification(root: Path) -> None:
    rows = markdown_rows(read(root, str(DOCS / "modules.md")), "Residual ZIO Reference Classification")
    categories = {row[-1] for row in rows if row}
    if categories != {"bridge/codec usage", "historical text", "compatibility naming"}:
        raise ValueError("residual ZIO categories are incomplete or unsupported")


def check_verification_headings(root: Path) -> None:
    text = read(root, str(DOCS / "verification.md"))
    require(text, "## Environment", "## Tier 0 — Compile Evidence", "## Tier 1 — Core Test Evidence", "## Tier 2 — Database (`db`) Evidence")


def check_verification_records(root: Path) -> None:
    text = read(root, str(DOCS / "verification.md"))
    for record in ("VR-001", "VR-002", "VR-008", "VR-009"):
        if record not in text:
            raise ValueError(f"missing verification record: {record}")


def governance_files(root: Path) -> list[tuple[str, str]]:
    names = ("contributing.md", "security.md", "code-of-conduct.md", "support-maintenance.md", "release-controls.md")
    return [(name, read(root, str(DOCS / "governance" / name))) for name in names]


def check_governance_matrices(root: Path) -> None:
    for name, text in governance_files(root):
        if not markdown_rows(text, "Governance Topic Matrix"):
            raise ValueError(f"empty governance matrix: {name}")


def check_governance_topics(root: Path) -> None:
    required = {
        "contributing.md": "contribution expectations",
        "security.md": "private security-reporting guidance",
        "code-of-conduct.md": "code-of-conduct expectations",
        "support-maintenance.md": "support and maintenance boundaries",
        "release-controls.md": "ci/release separation",
    }
    for name, text in governance_files(root):
        require(text, required[name])


def check_governance_authority(root: Path) -> None:
    for name, text in governance_files(root):
        if "| unresolved | unassigned | unassigned |" not in text:
            raise ValueError(f"governance authority is not fail-closed: {name}")


def check_governance_references(root: Path) -> None:
    for name, text in governance_files(root):
        if not markdown_rows(text, "Reference Links"):
            raise ValueError(f"empty governance reference table: {name}")


S11_GOVERNANCE_TOPICS = {
    "contributing.md": {"contribution expectations": "ER-040"},
    "security.md": {"private security-reporting guidance": "ER-042"},
    "code-of-conduct.md": {"code-of-conduct expectations": "ER-041"},
    "support-maintenance.md": {"support and maintenance boundaries": "ER-043"},
    "release-controls.md": {
        "issue ownership": "ER-044", "release responsibilities": "ER-044",
        "ci/release separation": "ER-044", "approval evidence": "ER-044",
        "abort/recovery ownership": "ER-044",
    },
}
S11_GOVERNANCE_AREAS = {
    "contributing.md": "contribution", "security.md": "security",
    "code-of-conduct.md": "conduct", "support-maintenance.md": "maintenance",
    "release-controls.md": "release",
}


def check_s11_governance_ownership(documents: dict[str, str], evidence: str, readiness: str) -> None:
    require(readiness, "Status: not-ready", "Provenance", "Verification-Reproducibility", "Governance", "Release-Metadata", "CI-Separation-ownership-signoff", "Destination-Decision")
    for name, expected_topics in S11_GOVERNANCE_TOPICS.items():
        rows = markdown_rows(documents[name], "Governance Topic Matrix")
        if any(len(row) != 7 or any(not value for value in row) for row in rows):
            raise ValueError("missing required governance field")
        actual_topics = {row[0]: row for row in rows}
        if set(actual_topics) != set(expected_topics):
            raise ValueError("missing required governance topic")
        expected_disposition = f"Keep unresolved; named owner and approver must approve {S11_GOVERNANCE_AREAS[name]} governance."
        for topic, evidence_ref in expected_topics.items():
            _, status, owner, approver, actual_evidence, gap, disposition = actual_topics[topic]
            if status != "unresolved" or disposition != expected_disposition:
                raise ValueError("invalid governance status or disposition")
            if owner != "unassigned" or approver != "unassigned":
                raise ValueError("manufactured owner or approver")
            try:
                row_for(evidence, actual_evidence)
            except ValueError as error:
                raise ValueError("broken governance evidence reference") from error
            if actual_evidence != evidence_ref or not gap.endswith("are not recorded"):
                raise ValueError("cross-topic governance inconsistency")
    row_for(evidence, "ER-067")


def check_public_claim_coverage(root: Path) -> None:
    rows = markdown_rows(read(root, str(DOCS / "evidence.md")), "Public Claim Coverage")
    ids = {row[0] for row in rows if row}
    if len(ids) != 15 or not all(identifier.startswith("CC-") for identifier in ids):
        raise ValueError("public claim coverage must contain 15 CC records")

def check_provenance_questions(root: Path) -> None:
    rows = markdown_rows(read(root, str(DOCS / "provenance.md")), "Blocked Questions (unresolved-question — do not resolve here)")
    if len(rows) < 5 or not all("unassigned" in row[-1] for row in rows):
        raise ValueError("provenance blocked questions are incomplete")


def check_readiness_matrix(root: Path) -> None:
    text = read(root, str(DOCS / "readiness-report.md"))
    rows = markdown_rows(text, "Requirement and Claim Coverage Matrix")
    if not any(row and row[0].startswith("REQ:") for row in rows):
        raise ValueError("readiness report lacks requirement coverage")


def check_verify_r1_evidence_remediation(verification: str, readiness: str, evidence: str) -> None:
    s06_fields = {
        row[0].strip(): row[1].strip()
        for row in markdown_rows(verification, "S06 Fresh Temurin Normal-Clone Observation")
        if len(row) == 2
    }
    if s06_fields.get("scala_version") != "`3.8.4`":
        raise ValueError("S06 must record Scala version 3.8.4")
    scala_evidence = s06_fields.get("scala_version_evidence", "")
    require(
        scala_evidence,
        ".s06-collector-outputs/sbt-compile.log",
        "sha256:a608ec159c48c21fcacc3eadd1b57b38dfff68232fb29dcd10755b53bf554648",
        "target/scala-3.8.4/classes",
        "successful compile log",
        "not the failed auxiliary JVM probe",
    )
    matrix = {row[1]: row for row in markdown_rows(readiness, "Requirement and Claim Coverage Matrix") if len(row) == 9}
    for evidence_id in ("ER-061", "ER-044"):
        if evidence_id not in matrix or matrix[evidence_id][5] != "stale":
            raise ValueError(f"readiness matrix {evidence_id} must retain stale freshness")
        if "| stale |" not in row_for(evidence, evidence_id):
            raise ValueError(f"authoritative evidence {evidence_id} must retain stale status")
    check_er044_supersession(evidence)


def validate_s15_aggregation(blockers: tuple[dict[str, object], ...], slices: tuple[dict[str, object], ...], context: dict[str, object]) -> None:
    expected_blockers = {f"BL-00{index}" for index in range(1, 8)}
    if {blocker.get("id") for blocker in blockers} != expected_blockers:
        raise ValueError("seven blockers are required")
    if context.get("candidate") != "master@4d286f7d9172f452fd3d0ae2458ef73c11376fea" or context.get("tree") != "8924989ad8d3d676aff73c30ab93ad31878e7385" or context.get("repository_kind") != "normal-local-clone":
        raise ValueError("candidate/tree/clone binding is incomplete")
    if context.get("temurin") != "25":
        raise ValueError("Temurin evidence cannot auto-close blockers 6/7")
    if context.get("plan_only") is not True or context.get("mutation_authorized") is not False or context.get("json_migration") != "deferred" or context.get("provenance") not in {"evidence-backed", "unresolved"}:
        raise ValueError("authority, migration, or provenance boundary is invalid")
    for blocker in blockers:
        required = {"status", "freshness", "evidence", "gap", "owner", "approver", "required_disposition"}
        if not required <= blocker.keys() or not blocker["evidence"] or not blocker["gap"] or not blocker["required_disposition"]:
            raise ValueError("blocker fields are incomplete")
        if blocker["status"] == "closed":
            if not blocker.get("authority_ref") or blocker["owner"] == "unassigned" or blocker["approver"] == "unassigned":
                raise ValueError("authority is required for closure")
            if blocker["freshness"] != "fresh":
                raise ValueError("fresh attributable evidence is required for closure")
        elif blocker["status"] != "open":
            raise ValueError("blocker must remain open or be explicitly closed")
    expected_slices = {f"S{index:02d}" for index in range(1, 16)}
    if {slice_.get("id") for slice_ in slices} != expected_slices:
        raise ValueError("exactly 15 product slices are required")
    for slice_ in slices:
        if int(slice_.get("additions", -1)) + int(slice_.get("deletions", -1)) > 400:
            raise ValueError("slice exceeds 400")
        if slice_.get("authority") != "not-authorized" or not slice_.get("evidence") or not slice_.get("exclusions"):
            raise ValueError("slice authority or evidence is invalid")


def check_s15_final_readback(root: Path) -> None:
    report = read(root, str(DOCS / "readiness-report.md"))
    blocker_rows = markdown_rows(report, "S15 Final Fail-Closed Aggregation")
    if len(blocker_rows) != 7 or {row[0].split()[0] for row in blocker_rows} != {f"BL-00{index}" for index in range(1, 8)}:
        raise ValueError("S15 requires exactly seven blockers")
    for row in blocker_rows:
        if len(row) != 7 or row[1] != "open" or row[4] != "unassigned" or row[5] != "unassigned" or "ER-" not in row[2] or not row[3] or not row[6]:
            raise ValueError("S15 blocker is incomplete or falsely closed")
    slice_rows = markdown_rows(report, "S15 Product-Slice Cross-Reference")
    if len(slice_rows) != 15 or {row[0] for row in slice_rows} != {f"S{index:02d}" for index in range(1, 16)}:
        raise ValueError("S15 requires exactly 15 product slices")
    if any(len(row) != 5 or row[1] != "<=400" or row[3] != "not-authorized" or not row[2] or "ER-" not in row[4] for row in slice_rows):
        raise ValueError("S15 slice budget, exclusion, authority, or evidence is invalid")
    require(report, "plan_only: true", "mutation_authorized:\nfalse", "zio-json", "unresolved")
    require(read(root, str(DOCS / "evidence.md")), "| ER-071 |")
    require(read(root, str(DOCS / "verification.md")), "## S15 Final Verification", "## S15 Cross-Reference Result", "master@4d286f7d9172f452fd3d0ae2458ef73c11376fea", "8924989ad8d3d676aff73c30ab93ad31878e7385", "normal-local-clone", "Temurin 25")


def check_not_ready_and_blockers(root: Path) -> None:
    text = read(root, str(DOCS / "readiness-report.md"))
    require(text, "Status: not-ready", "Provenance", "Verification-Reproducibility", "Governance", "Release-Metadata", "CI-Separation-ownership-signoff", "Destination-Decision")


def row_for(text: str, identifier: str) -> str:
    prefix = f"| {identifier} |"
    matches = [line for line in text.splitlines() if line.lstrip().startswith(prefix)]
    if len(matches) != 1:
        raise ValueError(f"expected one row for {identifier}, found {len(matches)}")
    return matches[0]


def check_archive_ledger_text(text: str) -> None:
    required = (
        "## S02 Canonicalization Audit Record",
        "| archived_artifact | `openspec/changes/archive/2026-08-27-prepare-kyo-quill-publication/proposal.md` |",
        "| original_value | `openspec/changes/prepare-kyo-quill-publication/proposal.md` |",
        "| replacement_value | `openspec/changes/archive/2026-08-27-prepare-kyo-quill-publication/proposal.md` |",
        "| s01_classification | `intentionally-historical` |",
        "| user_decision | **“Reescribir el enlace”",
        "| authorized_scope | Exactly the sole matching reference",
        "| verification_outcome | PASS — former value count changed from 1 to 0",
    )
    require(text, *required)


def check_provenance_remediation(provenance: str, controls: str) -> None:
    for text in (provenance, controls):
        rows = markdown_rows(text, "Dependency-Graph Provenance (S03)")
        if {row[0] for row in rows} != {"verified-fact", "inference", "unresolved-question"}:
            raise ValueError("dependency-graph provenance classifications are incomplete or noncanonical")
        if not all(".github/workflows/dependency-graph.yml" in row[-1] for row in rows):
            raise ValueError("dependency-graph provenance citations omit the workflow path")


def check_release_controls_remediation(text: str) -> None:
    require(text, "ordinary push became unreachable after the `ci.yml` condition change", "pull_request` was already unreachable to the `sbt ci-release` task")
    prohibited = (
        "ordinary push can activate `ci-release`",
        "automation is dormant solely because the fork was never pushed",
        "No release has been created because this fork has never pushed",
    )
    if any(claim in text for claim in prohibited):
        raise ValueError("release controls contain an unsupported current-state claim")


def check_er044_supersession(text: str) -> None:
    row = row_for(text, "ER-044")
    require(row, "| stale | blocking |", "Superseded by ER-054", "does not state current ordinary-push `ci-release` reachability")
    if "ordinary push can reach `ci-release`" in row:
        raise ValueError("ER-044 asserts ordinary-push ci-release reachability")


def check_s04_candidate_boundary(evidence: str, verification: str) -> None:
    require(evidence, "S04 Boundary Classification", "Eclipse Temurin `25.0.4+7`", "normal-local-clone", "S06-Temurin-normal-clone-TBD")
    for identifier in ("S04-ER-001", "S04-ER-003", "S04-ER-004", "S04-ER-005"):
        if "| fresh |" in row_for(evidence, identifier):
            raise ValueError(f"{identifier} falsely claims fresh evidence")
    require(evidence, "jdk-vendor-version-mismatch", "sbt-jvm-mismatch", "clone-kind-mismatch")
    require(verification, "The retained tier and rebinding verification records are **not fresh**", "no Temurin execution is claimed here")


def check_authoritative_stale_statuses(evidence: str, verification: str) -> None:
    evidence_ids = [f"ER-{number:03d}" for number in (*range(14, 20), *range(32, 40), 61)]
    verification_ids = [f"VR-{number:03d}" for number in range(1, 22)]
    for identifier, text in ((item, evidence) for item in evidence_ids) :
        if "| stale |" not in row_for(text, identifier):
            raise ValueError(f"{identifier} must retain authoritative stale status")
    for identifier, text in ((item, verification) for item in verification_ids):
        if "| stale |" not in row_for(text, identifier):
            raise ValueError(f"{identifier} must retain authoritative stale status")


S05_FOUNDATION_FIELDS = (
    "candidate_ref", "candidate_tree", "repository_kind", "clone_identity", "freshness",
    "os_arch", "actual_sbt_jvm_vendor", "actual_sbt_jvm_version", "java_release",
    "sbt_jvm_evidence", "scala_version", "sbt_version", "commands",
    "service_prerequisites", "started_at_utc", "completed_at_utc", "result", "totals",
    "workspace_assumption", "bounded_refs", "disposition", "owner", "approver",
)
S05_OPEN_FIELDS = (
    "clone_identity", "os_arch", "actual_sbt_jvm_vendor", "actual_sbt_jvm_version",
    "java_release", "sbt_jvm_evidence", "scala_version", "sbt_version", "commands",
    "service_prerequisites", "started_at_utc", "completed_at_utc", "totals",
    "workspace_assumption", "bounded_refs",
)


def foundation_fields(text: str) -> dict[str, str]:
    rows = markdown_rows(text, "S05 Fresh Temurin Verification Foundation")
    return {row[0].strip("`"): row[1].strip("`") for row in rows if len(row) == 2}


def check_fresh_verification_foundation(text: str) -> None:
    fields = foundation_fields(text)
    missing = [field for field in S05_FOUNDATION_FIELDS if field not in fields]
    if missing:
        raise ValueError("S05 foundation missing fields: " + ", ".join(missing))
    if fields["candidate_ref"] != "master@4d286f7d9172f452fd3d0ae2458ef73c11376fea":
        raise ValueError("S05 foundation candidate must be the exact master commit")
    if fields["candidate_tree"] != "8924989ad8d3d676aff73c30ab93ad31878e7385":
        raise ValueError("S05 foundation tree must be the exact candidate tree")
    if fields["repository_kind"] != "normal-local-clone":
        raise ValueError("S05 foundation requires a normal-local-clone")
    if fields["freshness"] != "open" or fields["result"] != "open" or fields["disposition"] != "open":
        raise ValueError("S05 foundation must remain open until a fresh observation exists")
    if fields["owner"] != "unassigned" or fields["approver"] != "unassigned":
        raise ValueError("S05 foundation cannot infer an owner or approver")
    non_open = [field for field in S05_OPEN_FIELDS if fields[field] != "open"]
    if non_open:
        raise ValueError("S05 foundation cannot claim fresh runtime evidence: " + ", ".join(non_open))


EVIDENCE_CLASSIFICATIONS = {"verified-fact", "inference", "unresolved-question"}
EVIDENCE_FRESHNESS = {"fresh", "historical", "comparative", "stale", "superseded"}
EVIDENCE_STATUSES = {"current", "stale", "unavailable", "failed", "blocked", "not-applicable", "open"}
AUTHORITATIVE_CANDIDATE_REFS = {
    "4d286f7d9172f452fd3d0ae2458ef73c11376fea",
    "b45c3ea2",
    "be8826eb",
}
AUTHORITATIVE_ENVIRONMENT_REFS = {
    "ENV-Temurin-25",
    "ENV-Homebrew-25",
    "ENV-prior",
}


S09_SLICE_FIELDS = (
    "outcome", "start_state", "end_state", "path_allowlist", "protected_non_targets",
    "dependencies", "evidence_links", "stale_evidence_links", "acceptance", "verification",
    "runtime", "additions", "deletions", "rollback", "delivery_authority", "instructions",
)
S09_PROTECTED_TARGETS = (".codegraph/", "pi-session-")
S09_MUTATION_COMMAND = re.compile(
    r"^\s*(?:\$\s*)?(?:git\s+(?:add|commit|push|checkout|switch|branch|reset|rebase|merge|tag)|"
    r"gh\s+(?:pr\s+create|release\s+create)|sbt\s+.*\bpublish|mvn\s+.*\bdeploy)\b",
    re.IGNORECASE,
)


def comma_values(value: str) -> list[str]:
    return [item.strip() for item in value.split(",") if item.strip()]


def validate_s09_slice_record(record: dict[str, str], evidence_statuses: dict[str, str]) -> None:
    missing = [
        field for field in S09_SLICE_FIELDS
        if field not in record or (field != "stale_evidence_links" and not record[field].strip())
    ]
    if missing:
        raise ValueError("slice missing fields: " + ", ".join(missing))
    try:
        additions, deletions = int(record["additions"]), int(record["deletions"])
    except ValueError as error:
        raise ValueError("slice line accounting must be integers") from error
    if additions < 0 or deletions < 0 or additions + deletions > 400:
        raise ValueError("slice exceeds hard 400-line budget")
    paths = comma_values(record["path_allowlist"])
    if any(protected in path for path in paths for protected in S09_PROTECTED_TARGETS):
        raise ValueError("slice captures a protected non-target")
    links = comma_values(record["evidence_links"])
    stale_links = set(comma_values(record["stale_evidence_links"]))
    if any(link not in evidence_statuses for link in links + list(stale_links)):
        raise ValueError("slice has dangling cross-reference")
    stale_references = {link for link in links if evidence_statuses[link] == "stale"}
    if stale_references != stale_links:
        raise ValueError("slice stale-link propagation is incomplete")
    if record["delivery_authority"] != "not-authorized":
        raise ValueError("slice cannot grant delivery authority")
    if any(S09_MUTATION_COMMAND.search(line) for line in record["instructions"].splitlines()):
        raise ValueError("slice contains a forbidden mutation-capable instruction")


S09_RECORDS_HEADING = "### S09 Slice Records"
S09_EMPTY_RECORD_POLICY = "Current empty-record policy: `no-s09-slice-records`."
S09_RECORD_BLOCK = re.compile(r"```s09-slice\n(.*?)\n```", re.DOTALL)


def parse_s09_slice_records(text: str) -> tuple[dict[str, str], ...]:
    headings = list(re.finditer(rf"(?m)^{re.escape(S09_RECORDS_HEADING)}$", text))
    if not headings:
        raise ValueError("missing S09 slice record representation")
    if len(headings) != 1:
        raise ValueError("multiple S09 slice record representations")
    section = text[headings[0].end():]
    section = re.split(r"\n\s*## ", section, maxsplit=1)[0].strip()
    if section == S09_EMPTY_RECORD_POLICY:
        return ()
    if S09_EMPTY_RECORD_POLICY in section:
        raise ValueError("S09 slice records conflict with the empty-record policy")
    if not section:
        raise ValueError("S09 slice record representation requires records or the empty-record policy")
    blocks = list(S09_RECORD_BLOCK.finditer(section))
    if S09_RECORD_BLOCK.sub("", section).strip():
        raise ValueError("undeclared S09 slice record content")
    records: list[dict[str, str]] = []
    for block in blocks:
        record: dict[str, str] = {}
        for line in block.group(1).splitlines():
            if not line or ":" not in line:
                raise ValueError("malformed S09 slice record")
            field, value = line.split(":", 1)
            field, value = field.strip(), value.strip()
            if not field or field in record:
                raise ValueError("malformed S09 slice record")
            record[field] = value
        unsupported = set(record) - set(S09_SLICE_FIELDS)
        if unsupported:
            raise ValueError("undeclared S09 slice record field: " + ", ".join(sorted(unsupported)))
        records.append(record)
    return tuple(records)


def pending_unassigned_signoff(destination: dict[str, str]) -> bool:
    return (
        destination.get("signoff.status") == "pending"
        and destination.get("signoff.effective_date") == "pending"
        and destination.get("signoff.authority_ref") == "unassigned"
    )


def authorization_prerequisites_are_complete(destination: dict[str, str], manifest: dict[str, str]) -> bool:
    owner_keys = (
        "owners.decision_owner",
        "owners.repository_owner",
        "owners.release_approver",
        "owners.governance_approver",
    )
    owners = [destination.get(key, "") for key in owner_keys]
    return (
        destination.get("decision_status") == "approved"
        and destination.get("selected_destination.status") == "approved"
        and all(owner and owner != "unassigned" for owner in owners)
        and len(set(owners)) == len(owners)
        and all(destination.get(key) not in (None, "", "pending") for key in ("dates.decision_date", "dates.approval_date"))
        and destination.get("signoff.status") == "complete"
        and destination.get("signoff.effective_date") not in (None, "", "pending")
        and manifest.get("manifest_status") == "resolved"
        and manifest.get("fields.version.state") == "observed"
        and manifest.get("fields.version.owner") not in (None, "", "unassigned")
    )


def check_s10_controls(destination: str, manifest: str, reconciliation: str, controls: str, evidence: str) -> None:
    destination_values = flat_yaml(destination)
    manifest_values = flat_yaml(manifest)
    if any(value != "not-authorized" for key, value in destination_values.items() if (key.startswith("authorization.") or key == "selected_destination.source_publication.authorized") and key.endswith(".authorized")) and not authorization_prerequisites_are_complete(destination_values, manifest_values):
        raise ValueError("authorization requires complete owner, approver, sign-off, and version prerequisites")

    if destination_values.get("decision_status") == "approved":
        raise ValueError("destination approval requires assigned owners and non-pending dates")
    required_destination = {
        "candidate_id": "4d286f7d",
        "candidate_tree": "8924989ad8d3d676aff73c30ab93ad31878e7385",
        "decision_status": "pending",
        "selected_destination.status": "proposed",
        "s04.freshness": "stale",
        "s04.superseding_id": "S06-Temurin-normal-clone-2026-08-30",
    }

    for key, expected in required_destination.items():
        if destination_values.get(key) != expected:
            raise ValueError(f"destination {key} is inconsistent with current evidence")
    if any(destination_values.get(key) != "unassigned" for key in destination_values if key.startswith("owners.")):
        raise ValueError("destination owners must remain unassigned")
    if any(destination_values.get(key) != "pending" for key in destination_values if key.startswith("dates.")):
        raise ValueError("destination approval dates must remain pending")
    if not pending_unassigned_signoff(destination_values):
        raise ValueError("authority sign-off must remain pending and unassigned")
    evidence_ids = ("ER-028", "ER-029", "ER-031", "ER-054", "ER-059", "ER-065", "ER-066")
    require(destination_values.get("evidence_links", ""), "ER-054", "ER-059", "ER-065", "ER-066")
    for identifier in evidence_ids:
        row_for(evidence, identifier)
    if manifest_values.get("candidate_id") != destination_values["candidate_id"] or manifest_values.get("candidate_tree") != destination_values["candidate_tree"]:
        raise ValueError("manifest candidate metadata is inconsistent with destination")
    if manifest_values.get("manifest_status") != "unresolved":
        raise ValueError("manifest cannot claim a resolved release")
    if manifest_values.get("fields.version.state") != "unresolved":
        raise ValueError("manifest version control must remain unresolved")
    if any(manifest_values.get(f"fields.{field}.owner") != "unassigned" for field in ("version", "kyo_version", "scala_version", "java_version", "tag", "coordinates", "modules", "readme_coordinates", "changelog", "homepage", "scm", "license")):
        raise ValueError("manifest cannot invent a release owner")
    require(
        manifest,
        's10.evidence_ids: "ER-028, ER-029, ER-031, ER-065, ER-066"',
        's10.version_control.state: "unresolved"',
        's10.version_control.authorized: "not-authorized"',
    )
    require(reconciliation, "## S10 Control-Record Reconciliation", "ER-028", "ER-029", "ER-031", "ER-065", "ER-066", "Authority sign-off", "not-ready", "not-authorized")
    require(controls, "## S10 Authority-Control Readback", "ER-054", "ER-065", "ER-066", "sign-off and its effective\ndate remain `pending`", "unassigned", "pending", "not-authorized")



def check_s12_public_claims(documents: dict[str, str], evidence: str) -> None:
    readme = documents["README.md"]
    if 'val kyoVersion = "1.0.0-RC5"' in readme:
        raise ValueError("stale candidate claim")
    if "is complete" in readme[readme.index("Deferred JSON codec migration"):]:
        raise ValueError("JSON migration completion claim")
    require(readme, 'val kyoVersion = "1.0.0-RC6"', '"com.e-evolution" %% "quill-jdbc" % "5.0.0-kyo-RC6"', "Maven Central publication is not authorized", "remains deferred")
    changelog = documents["CHANGELOG.md"]
    if "**Released.**" in changelog:
        raise ValueError("unsupported readiness claim")
    require(changelog, "Historical Note", "**Not released.**", "no GitHub Release has been published")
    for relative in ("FINAL_STATUS_REPORT.md", "MIGRATION_PLAN.md", "MIGRATION_REPORT.md", "VALIDATION_REPORT_FINAL.md"):
        require(documents[relative], "Historical Document — Superseded.", "historical reference only")
    for relative in (".github/ISSUE_TEMPLATE.md", ".github/PULL_REQUEST_TEMPLATE.md"):
        template = documents[relative]
        require(template, "no confirmed, named", "governance status")
        if "Do not tag `@getquill/maintainers`" not in template:
            raise ValueError("dead ownership claim")
    require(evidence, "## Public Claim Coverage", "ER-047", "ER-048", "ER-049", "ER-050", "ER-051", "ER-052", "ER-067", "ER-068", "deferred to the follow-up Kyo Schema/Json SDD change")


def check_s13_event_graph(root: Path, evidence: str | None = None, verification: str | None = None) -> None:
    evidence = read(root, str(DOCS / "evidence.md")) if evidence is None else evidence
    verification = read(root, str(DOCS / "verification.md")) if verification is None else verification
    if "## S13 CI-Safety Static Event-Graph Evidence" not in evidence or "ER-069" not in evidence or "not-ready" not in evidence:
        raise ValueError("CI-safety evidence is incomplete")
    require(verification, "## S13 CI-Safety Static Analyzer", "dependency submission is", "repository mutation, not artifact publication")
    module_path = root / "scripts/publication-readiness/event_graph.py"
    spec = importlib.util.spec_from_file_location("publication_readiness_event_graph", module_path)
    if spec is None or spec.loader is None:
        raise ValueError("CI-safety analyzer is unavailable")
    analyzer = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(analyzer)
    fixture_errors = analyzer.check_fixture_dir(root / "scripts/publication-readiness/fixtures")
    if fixture_errors:
        raise ValueError("CI-safety fixture failure: " + "; ".join(fixture_errors))
    results = analyzer.run(root / ".github/workflows")
    for event in ("ordinary_push", "pull_request", "unauthorized_release"):
        if analyzer.publication_routes(results[event]):
            raise ValueError(f"CI-safety unauthorized publication route: {event}")
    if not analyzer.publication_routes(results["authorized_release"]):
        raise ValueError("CI-safety authorized release route is missing")
    mutations = {mutation for route in results["ordinary_push"] for mutation in route["repository_mutations"]}
    if mutations != {"dependency-submission", "release-draft"}:
        raise ValueError("CI-safety current mutation classification is incomplete")


def check_s14_workflow_structure(ci: str, dependency_graph: str, drafter: str) -> None:
    release_start = ci.rfind("\n  release:\n")
    if release_start < 0:
        raise ValueError("missing release job")
    release_body = ci[release_start + len("\n  release:\n"):]
    required_release_keys = (
        "if: ${{ github.event_name == 'release' && github.event.action == 'published' }}",
        "runs-on: ubuntu-latest",
        "needs: build",
        "steps:",
    )
    for key in required_release_keys:
        if f"    {key}" not in release_body or f"      {key}" in release_body:
            raise ValueError("release job keys must be siblings")
    require(ci, "release:\n    types: [ published ]", "permissions:\n  contents: read", "distribution: temurin", "java-version: '25'", "module: [ sqltest, db, bigdata ]", "./build/build.sh ${{ matrix.module }}")
    secret_pattern = r"secrets\.(PGP_PASSPHRASE|PGP_SECRET|SONATYPE_PASSWORD|SONATYPE_USERNAME)"
    release_secret_occurrences = re.findall(secret_pattern, release_body)
    all_secret_occurrences = re.findall(secret_pattern, ci)
    if set(release_secret_occurrences) != {"PGP_PASSPHRASE", "PGP_SECRET", "SONATYPE_PASSWORD", "SONATYPE_USERNAME"} or all_secret_occurrences != release_secret_occurrences:
        raise ValueError("publication secrets must remain release-job scoped")
    require(dependency_graph, "permissions:\n  contents: write", "distribution: temurin", "java-version: '25'", "scalacenter/sbt-dependency-submission@v3", "artifact publication or a claim about workflow provenance")
    require(drafter, "permissions:\n  contents: write", "release-drafter/release-drafter@v6", "GITHUB_TOKEN")
    if "ci-release" in drafter or "SONATYPE_" in drafter or "PGP_" in drafter:
        raise ValueError("release drafter must not be classified as artifact publication")


def check_s14_workflow_safety(root: Path) -> None:
    ci = read(root, ".github/workflows/ci.yml")
    drafter = read(root, ".github/workflows/release-drafter.yml")
    dependency_graph = read(root, ".github/workflows/dependency-graph.yml")
    evidence = read(root, str(DOCS / "evidence.md"))
    verification = read(root, str(DOCS / "verification.md"))
    check_s14_workflow_structure(ci, dependency_graph, drafter)
    require(evidence, "## S14 CI Workflow Correction Evidence", "ER-070", "not-ready")
    require(verification, "## S14 CI Workflow Correction Verification", "repository mutation, not artifact publication")
    require(
        ci,
        "permissions:\n  contents: read",
        "module: [ sqltest, db, bigdata ]",
        "./build/build.sh ${{ matrix.module }}",
        "if: ${{ github.event_name == 'release' && github.event.action == 'published' }}",
        "PGP_PASSPHRASE", "PGP_SECRET", "SONATYPE_PASSWORD", "SONATYPE_USERNAME",
    )
    require(drafter, "permissions:\n  contents: write", "release-drafter/release-drafter@v6", "GITHUB_TOKEN")
    if "ci-release" in drafter or "SONATYPE_" in drafter or "PGP_" in drafter:
        raise ValueError("release drafter must not be classified as artifact publication")
    require(
        dependency_graph,
        "permissions:\n  contents: write",
        "distribution: temurin",
        "java-version: '25'",
        "scalacenter/sbt-dependency-submission@v3",
    )
    if "fork has never pushed" in dependency_graph:
        raise ValueError("dependency-graph provenance contains an unsupported causal claim")
    check_s13_event_graph(root)


def check_s09_checker_contract(root: Path) -> None:
    text = read(root, str(DOCS / "evidence.md"))
    require(
        text,
        "## S09 Slice Integrity and Operation-Safety Contract",
        "stale linked\n    record must also appear", "protected-non-target rejection", "exactly 400",
        "401", "mutation-capable instruction", "Plan-only Git wording is permitted",
    )
    evidence_statuses = {
        row[0]: row[6] for row in markdown_rows(text, "Evidence Index") if len(row) >= 7
    }
    for record in parse_s09_slice_records(text):
        validate_s09_slice_record(record, evidence_statuses)


def validate_evidence_core(record: dict[str, str], known_ids: set[str]) -> None:

    required = (
        "id", "classification", "freshness", "status", "candidate_ref",
        "environment_ref", "blocker_links", "owner", "approver", "authority_ref",
        "disposition", "invalidation_reason", "superseding_id",
    )
    optional = {"authority_ref", "invalidation_reason", "superseding_id"}
    missing = [field for field in required if not record.get(field, "").strip() and field not in optional]
    if missing:
        raise ValueError("evidence core missing fields: " + ", ".join(missing))
    if not record["id"].startswith("ER-") or not record["id"][3:].isdigit():
        raise ValueError("evidence core has malformed id")
    if record["classification"] not in EVIDENCE_CLASSIFICATIONS:
        raise ValueError("evidence core has unsupported classification")
    if record["freshness"] not in EVIDENCE_FRESHNESS:
        raise ValueError("evidence core has unsupported freshness")
    if record["status"] not in EVIDENCE_STATUSES:
        raise ValueError("evidence core has unsupported status")
    if record["candidate_ref"] not in AUTHORITATIVE_CANDIDATE_REFS:
        raise ValueError("evidence core has dangling candidate reference")
    if record["environment_ref"] not in AUTHORITATIVE_ENVIRONMENT_REFS:
        raise ValueError("evidence core has dangling environment reference")
    if record["freshness"] == "fresh":
        if record["candidate_ref"] != "4d286f7d9172f452fd3d0ae2458ef73c11376fea":
            raise ValueError("fresh evidence must use the exact candidate and environment reference")
        if record["environment_ref"] != "ENV-Temurin-25":
            raise ValueError("fresh evidence must use the exact candidate and environment reference")
        if record["status"] != "current":
            raise ValueError("fresh evidence must retain current status")
    else:
        if record["status"] == "current":
            raise ValueError("non-fresh evidence cannot claim current status")
        if not record["invalidation_reason"]:
            raise ValueError("non-fresh evidence requires invalidation metadata")
    if record["freshness"] == "superseded":
        successor = record["superseding_id"]
        if successor not in known_ids or successor == record["id"]:
            raise ValueError("superseded evidence has a dangling successor")
    links = [item.strip() for item in record["blocker_links"].split(",") if item.strip()]
    if not links or any(link not in known_ids for link in links):
        raise ValueError("evidence has dangling blocker links")
    named_authority = record["owner"] != "unassigned" or record["approver"] != "unassigned"
    if named_authority and not record["authority_ref"]:
        raise ValueError("evidence invents authority without an authority reference")
    if not record["disposition"].strip():
        raise ValueError("evidence requires a disposition")


def check_evidence_core_schema(root: Path) -> None:
    text = read(root, str(DOCS / "evidence.md"))
    rows = markdown_rows(text, "S08 Evidence Core Validation")
    fields = {row[0].strip("`") for row in rows if len(row) >= 2}
    required = {"id", "classification", "freshness", "status", "candidate_ref", "environment_ref", "blocker_links", "owner", "approver", "authority_ref", "disposition", "invalidation_reason", "superseding_id"}
    if fields != required:
        raise ValueError("S08 evidence core schema fields are incomplete or unsupported")
    evidence_rows = markdown_rows(text, "Evidence Index")
    known_ids = {row[0] for row in evidence_rows if row}
    for row in evidence_rows:
        record = {
            "id": row[0],
            "classification": row[2],
            "freshness": "fresh" if row[6] == "current" else "stale",
            "status": row[6],
            "candidate_ref": "4d286f7d9172f452fd3d0ae2458ef73c11376fea" if row[5] == "4d286f7d" else row[5],
            "environment_ref": "ENV-Temurin-25" if row[6] == "current" else "ENV-prior",
            "blocker_links": "ER-001",
            "owner": row[8],
            "approver": row[9],
            "authority_ref": "",
            "disposition": row[10],
            "invalidation_reason": "historical evidence index" if row[6] != "current" else "",
            "superseding_id": "",
        }
        validate_evidence_core(record, known_ids)


def check_s07_reconciliation_record(reconciliation: str, evidence: str) -> None:
    require(
        reconciliation,
        "## S07 Publication-Branch Reconciliation Plan",
        "plan_only: true",
        "mutation_authorized: false",
            "| source commit | `b45c3ea25a3de59b58329cf4c76e8938a1c7892c` |",
            "| source tree | `9c162c4f3d0d36c141517da6b61cfe21fe598dc8` |",
            "| target commit | `4d286f7d9172f452fd3d0ae2458ef73c11376fea` |",
            "| target tree | `8924989ad8d3d676aff73c30ab93ad31878e7385` |",
            "| merge-base | `b45c3ea25a3de59b58329cf4c76e8938a1c7892c` |",
            "### Read-only comparison evidence",
            "git diff --name-status b45c3ea2 4d286f7d9172f452fd3d0ae2458ef73c11376fea",
            "### Reconciliation decisions",
            "| baseline history | retain |",
            "| candidate delta | replace only after explicit authority |",
            "| publication-readiness package mapping | unresolved |",
            "| conflict expectation | unresolved, not waived |",
            "| dependency-graph provenance | retain unresolved classification |",
            "### Validation and authority gates",
            "Require an explicit user authorization that names the proposed ref operation",
            "### Rollback boundary",

    )
    require(
        evidence,
        "| ER-066 | S07 read-only publication-branch comparison:",
        "plan_only: true",
        "mutation_authorized: false",
        "all seven blockers and `not-ready` remain unchanged",
    )


def check_amendment_cross_record_registration(root: Path) -> None:

    evidence = read(root, str(DOCS / "evidence.md"))
    verification = read(root, str(DOCS / "verification.md"))
    check_provenance_remediation(read(root, str(DOCS / "provenance.md")), read(root, str(DOCS / "governance" / "release-controls.md")))
    check_release_controls_remediation(read(root, str(DOCS / "governance" / "release-controls.md")))
    check_er044_supersession(evidence)
    check_s04_candidate_boundary(evidence, verification)
    check_authoritative_stale_statuses(evidence, verification)
    check_fresh_verification_foundation(verification)
    check_not_ready_and_blockers(root)


AMENDMENT_CHECKS: tuple[tuple[str, Callable[[Path], None]], ...] = (
    ("archive-ledger integrity", lambda root: check_archive_ledger_text(read(root, str(DOCS / "archive-reference-ledger.md")))),
    ("dependency-graph provenance remediation", lambda root: check_provenance_remediation(read(root, str(DOCS / "provenance.md")), read(root, str(DOCS / "governance" / "release-controls.md")))),
    ("release-controls remediation", lambda root: check_release_controls_remediation(read(root, str(DOCS / "governance" / "release-controls.md")))),
    ("ER-044 supersession", lambda root: check_er044_supersession(read(root, str(DOCS / "evidence.md")))),
    ("S04 candidate boundary classification", lambda root: check_s04_candidate_boundary(read(root, str(DOCS / "evidence.md")), read(root, str(DOCS / "verification.md")))),
    ("S04 authoritative stale statuses", lambda root: check_authoritative_stale_statuses(read(root, str(DOCS / "evidence.md")), read(root, str(DOCS / "verification.md")))),
    ("amendment cross-record registration", check_amendment_cross_record_registration),
    ("S07 plan-only reconciliation record", lambda root: check_s07_reconciliation_record(read(root, str(DOCS / "reconciliation.md")), read(root, str(DOCS / "evidence.md")))),
        ("S08 evidence schema core", check_evidence_core_schema),
        ("S09 slice integrity and operation safety", check_s09_checker_contract),
            ("S10 destination and release authority controls", lambda root: check_s10_controls(
                read(root, str(DOCS / "destination-decision.yaml")),
                read(root, str(DOCS / "release-manifest.yaml")),
                read(root, str(DOCS / "reconciliation.md")),
                read(root, str(DOCS / "governance" / "release-controls.md")),
                read(root, str(DOCS / "evidence.md")),
            )),
            ("S11 governance ownership records", lambda root: check_s11_governance_ownership(
                dict(governance_files(root)),
                read(root, str(DOCS / "evidence.md")),
                read(root, str(DOCS / "readiness-report.md")),
            )),
                ("S12 public claim coverage", lambda root: check_s12_public_claims(
                    {relative: read(root, relative) for relative in ("README.md", "CHANGELOG.md", "FINAL_STATUS_REPORT.md", "MIGRATION_PLAN.md", "MIGRATION_REPORT.md", "VALIDATION_REPORT_FINAL.md", ".github/ISSUE_TEMPLATE.md", ".github/PULL_REQUEST_TEMPLATE.md")},
                    read(root, str(DOCS / "evidence.md")),
                )),
                ("S13 CI-safety event graph", check_s13_event_graph),
                ("S14 CI workflow safety", check_s14_workflow_safety),
                    ("S15 final aggregation and cross-reference", check_s15_final_readback),
                    ("VERIFY-R1 evidence remediation", lambda root: check_verify_r1_evidence_remediation(
                        read(root, str(DOCS / "verification.md")),
                        read(root, str(DOCS / "readiness-report.md")),
                        read(root, str(DOCS / "evidence.md")),
                    )),
                )



def validate_amendment_registration(checks: tuple[tuple[str, Callable[[Path], None]], ...]) -> None:
    names = tuple(name for name, _ in checks)
    if names != AMENDMENT_VALIDATIONS or len(set(names)) != len(AMENDMENT_VALIDATIONS):
        raise ValueError("amendment validation registration is incomplete or duplicated")


COMMON_CHECKS: tuple[tuple[str, Callable[[Path], None]], ...] = (
    ("required publication-readiness records", check_required_records),
    ("candidate bindings", check_candidate_bindings),
    ("evidence index", check_evidence_index),
    ("evidence classifications", check_evidence_classifications),
    ("evidence statuses", check_evidence_statuses),
    ("destination decision record", check_destination_record),
    ("release manifest required fields", check_manifest_fields),
    ("release manifest state enums", check_manifest_states),
    ("release manifest reconciliation", check_manifest_reconciliation),
    ("aggregate module coverage", check_module_inventory),
    ("residual ZIO reference classification", check_residual_zio_classification),
    ("verification record headings", check_verification_headings),
    ("verification record presence", check_verification_records),
    ("governance topic matrices", check_governance_matrices),
    ("governance required topics", check_governance_topics),
    ("governance topic authority", check_governance_authority),
    ("governance reference readback", check_governance_references),
    ("public claim coverage", check_public_claim_coverage),
    ("provenance blocked questions", check_provenance_questions),
    ("readiness report requirement coverage", check_readiness_matrix),
    ("not-ready seven-blocker aggregation", check_not_ready_and_blockers),
)


def check_documents(root: Path | None = None) -> list[tuple[str, str | None]]:
    base = DEFAULT_ROOT if root is None else root
    results: list[tuple[str, str | None]] = []
    for name, check in (*COMMON_CHECKS, *AMENDMENT_CHECKS):
        try:
            check(base)
            results.append((name, None))
        except (OSError, ValueError) as error:
            results.append((name, str(error)))
    return results


def run(root: Path | None = None) -> int:
    results = check_documents(root)
    for name, error in results:
        print(f"{'FAIL' if error else 'PASS'}: {name}" + (f" — {error}" if error else ""))
    print(f"VALIDATIONS: {len(results)}/{len(COMMON_CHECKS) + len(AMENDMENT_CHECKS)}")
    return 0 if all(error is None for _, error in results) else 1


class RecoveryContractTests(unittest.TestCase):
    def test_common_contracts_are_source_readable(self) -> None:
        results = check_documents()
        self.assertEqual(len(results), 38)
        self.assertEqual([error for _, error in results], [None] * 38)

    def test_amendment_contract_is_fully_registered(self) -> None:
        validate_amendment_registration(AMENDMENT_CHECKS)
        self.assertEqual(len(AMENDMENT_VALIDATIONS), 17)


    def test_cli_returns_zero_when_all_validations_pass(self) -> None:

        self.assertEqual(run(), 0)

    def test_main_uses_default_root_for_checker_execution(self) -> None:
        self.assertEqual(main([]), 0)

    def test_main_runs_self_test_suite(self) -> None:
        suite = object()
        runner = MagicMock()
        runner.run.return_value.wasSuccessful.return_value = True
        with patch.object(unittest.defaultTestLoader, "loadTestsFromTestCase", return_value=suite), patch.object(
            unittest, "TextTestRunner", return_value=runner
        ):
            self.assertEqual(main(["--self-test"]), 0)
        runner.run.assert_called_once_with(suite)




    def test_malformed_common_input_is_not_skipped(self) -> None:
        self.assertEqual(check_documents(DEFAULT_ROOT / "missing-root")[0][1], "missing required record: docs/publication-readiness/evidence.md")

    def test_manifest_state_rejects_unsupported_value(self) -> None:
        self.assertFalse(manifest_states_are_valid({"fields.version.state": "resolved"}))
        self.assertTrue(manifest_states_are_valid({"fields.version.state": "unresolved"}))

    def test_archive_ledger_accepts_complete_canonical_audit(self) -> None:
        check_archive_ledger_text(read(DEFAULT_ROOT, str(DOCS / "archive-reference-ledger.md")))

    def test_archive_ledger_rejects_missing_audit_field(self) -> None:
        ledger = read(DEFAULT_ROOT, str(DOCS / "archive-reference-ledger.md"))
        with self.assertRaises(ValueError):
            check_archive_ledger_text(ledger.replace("| s01_classification |", "| classification |", 1))

    def test_provenance_remediation_rejects_uncited_or_noncanonical_classification(self) -> None:
        provenance = read(DEFAULT_ROOT, str(DOCS / "provenance.md"))
        controls = read(DEFAULT_ROOT, str(DOCS / "governance" / "release-controls.md"))
        with self.assertRaises(ValueError):
            check_provenance_remediation(provenance.replace("| inference |", "| reasoned-inference |", 1), controls)

    def test_release_controls_remediation_rejects_unsupported_current_claim(self) -> None:
        controls = read(DEFAULT_ROOT, str(DOCS / "governance" / "release-controls.md"))
        with self.assertRaises(ValueError):
            check_release_controls_remediation(controls + "\nordinary push can activate `ci-release`\n")

    def test_er044_supersession_accepts_retained_stale_history(self) -> None:
        check_er044_supersession(read(DEFAULT_ROOT, str(DOCS / "evidence.md")))

    def test_er044_supersession_rejects_current_or_reachability_claim(self) -> None:
        evidence = read(DEFAULT_ROOT, str(DOCS / "evidence.md"))
        er044 = row_for(evidence, "ER-044")
        with self.assertRaises(ValueError):
            check_er044_supersession(evidence.replace(er044, er044.replace("| stale | blocking |", "| current | blocking |"), 1))

    def test_s04_boundary_accepts_exact_nonfresh_classification(self) -> None:
        check_s04_candidate_boundary(read(DEFAULT_ROOT, str(DOCS / "evidence.md")), read(DEFAULT_ROOT, str(DOCS / "verification.md")))

    def test_s04_boundary_rejects_fresh_or_missing_invalidation(self) -> None:
        evidence = read(DEFAULT_ROOT, str(DOCS / "evidence.md"))
        verification = read(DEFAULT_ROOT, str(DOCS / "verification.md"))
        with self.assertRaises(ValueError):
            check_s04_candidate_boundary(evidence.replace("| stale | jdk-vendor-version-mismatch", "| fresh | jdk-vendor-version-mismatch", 1), verification)

    def test_authoritative_stale_statuses_accept_all_named_rows(self) -> None:
        check_authoritative_stale_statuses(read(DEFAULT_ROOT, str(DOCS / "evidence.md")), read(DEFAULT_ROOT, str(DOCS / "verification.md")))

    def test_authoritative_stale_statuses_reject_current_named_row(self) -> None:
        evidence = read(DEFAULT_ROOT, str(DOCS / "evidence.md"))
        verification = read(DEFAULT_ROOT, str(DOCS / "verification.md"))
        with self.assertRaises(ValueError):
            check_authoritative_stale_statuses(evidence.replace("| stale | blocking |", "| current | blocking |", 1), verification)

    def test_amendment_registration_accepts_all_seven_unique_checks(self) -> None:
        validate_amendment_registration(AMENDMENT_CHECKS)

    def test_amendment_registration_rejects_missing_or_duplicate_check(self) -> None:
        with self.assertRaises(ValueError):
            validate_amendment_registration(AMENDMENT_CHECKS[:-1])


    def test_fresh_verification_foundation_rejects_missing_required_field(self) -> None:
        with self.assertRaises(ValueError):
            check_fresh_verification_foundation("## S05 Fresh Temurin Verification Foundation\n")

    def test_fresh_verification_foundation_rejects_shell_java_sbt_jvm_mismatch(self) -> None:
        record = "\n".join((
            "\n## S05 Fresh Temurin Verification Foundation",
            "| field | required placeholder |",
            "| --- | --- |",
            "| candidate_ref | `master@4d286f7d9172f452fd3d0ae2458ef73c11376fea` |",
            "| candidate_tree | `8924989ad8d3d676aff73c30ab93ad31878e7385` |",
            "| repository_kind | `normal-local-clone` |",
            "| freshness | `open` |",
            "| actual_sbt_jvm_vendor | `Eclipse Temurin` |",
            "| actual_sbt_jvm_version | `25` |",
            "| sbt_jvm_evidence | `Temurin sbt banner` |",
            "| shell_java_vendor | `Homebrew` |",
            "| shell_java_version | `25` |",
            "| clone_identity | `TBD` |",
            "| os_arch | `TBD` |",
            "| java_release | `TBD` |",
            "| scala_version | `TBD` |",
            "| sbt_version | `TBD` |",
            "| commands | `TBD` |",
            "| service_prerequisites | `TBD` |",
            "| started_at_utc | `TBD` |",
            "| completed_at_utc | `TBD` |",
            "| result | `open` |",
            "| totals | `TBD` |",
            "| workspace_assumption | `TBD` |",
            "| bounded_refs | `TBD` |",
            "| disposition | `open` |",
            "| owner | `unassigned` |",
            "| approver | `unassigned` |",
        ))
        with self.assertRaisesRegex(ValueError, "cannot claim fresh runtime evidence"):
            check_fresh_verification_foundation(record)

    def test_fresh_verification_foundation_accepts_only_open_contract(self) -> None:
        check_fresh_verification_foundation(read(DEFAULT_ROOT, str(DOCS / "verification.md")))

    def test_verify_r1_rejects_missing_or_wrong_s06_scala_version(self) -> None:
        verification = read(DEFAULT_ROOT, str(DOCS / "verification.md"))
        readiness = read(DEFAULT_ROOT, str(DOCS / "readiness-report.md"))
        evidence = read(DEFAULT_ROOT, str(DOCS / "evidence.md"))
        for invalid in (
            verification.replace("| scala_version | `3.8.4` |\n", "", 1),
            verification.replace("| scala_version | `3.8.4` |", "| scala_version | `3.7.0` |", 1),
        ):
            with self.subTest(invalid=invalid):
                with self.assertRaises(ValueError):
                    check_verify_r1_evidence_remediation(invalid, readiness, evidence)

    def test_verify_r1_rejects_missing_s06_scala_citation(self) -> None:
        verification = read(DEFAULT_ROOT, str(DOCS / "verification.md"))
        readiness = read(DEFAULT_ROOT, str(DOCS / "readiness-report.md"))
        evidence = read(DEFAULT_ROOT, str(DOCS / "evidence.md"))
        missing_citation = verification.replace(".s06-collector-outputs/sbt-compile.log", "missing-compile-log", 1)
        with self.assertRaises(ValueError):
            check_verify_r1_evidence_remediation(missing_citation, readiness, evidence)

    def test_verify_r1_rejects_false_current_readiness_rows(self) -> None:
        verification = read(DEFAULT_ROOT, str(DOCS / "verification.md"))
        readiness = read(DEFAULT_ROOT, str(DOCS / "readiness-report.md"))
        evidence = read(DEFAULT_ROOT, str(DOCS / "evidence.md"))
        for evidence_id in ("ER-061", "ER-044"):
            false_current = readiness.replace(f"| {evidence_id} | unassigned | unassigned | 4d286f7d | stale |", f"| {evidence_id} | unassigned | unassigned | 4d286f7d | current |", 1)
            with self.subTest(evidence_id=evidence_id):
                with self.assertRaises(ValueError):
                    check_verify_r1_evidence_remediation(verification, false_current, evidence)

    def test_verify_r1_rejects_lost_er044_supersession(self) -> None:
        verification = read(DEFAULT_ROOT, str(DOCS / "verification.md"))
        readiness = read(DEFAULT_ROOT, str(DOCS / "readiness-report.md")).replace("| 4d286f7d | current | open-blocking | blocking | Tier 0-4", "| 4d286f7d | stale | open-blocking | blocking | Tier 0-4", 1).replace("| 4d286f7d | current | open-blocking | blocking | All governance", "| 4d286f7d | stale | open-blocking | blocking | All governance", 1)
        evidence = read(DEFAULT_ROOT, str(DOCS / "evidence.md")).replace("Superseded by ER-054", "No successor recorded", 1)
        corrected = verification.replace(
            "| actual SBT JVM | SBT banner in every SBT log: `sbt 1.12.4 (Eclipse Adoptium Java 25.0.4)`; Scala version was not independently emitted by the successful commands |",
            "| actual SBT JVM | SBT banner in every SBT log: `sbt 1.12.4 (Eclipse Adoptium Java 25.0.4)` |\n| scala_version | `3.8.4` |\n| scala_version_evidence | `.s06-collector-outputs/sbt-compile.log`; `sha256:a608ec159c48c21fcacc3eadd1b57b38dfff68232fb29dcd10755b53bf554648`; `target/scala-3.8.4/classes`; successful compile log, not the failed auxiliary JVM probe |",
            1,
        )
        with self.assertRaises(ValueError):
            check_verify_r1_evidence_remediation(corrected, readiness, evidence)

    def test_s07_reconciliation_accepts_complete_plan_only_record(self) -> None:
        check_s07_reconciliation_record(
            read(DEFAULT_ROOT, str(DOCS / "reconciliation.md")),
            read(DEFAULT_ROOT, str(DOCS / "evidence.md")),
        )


    def test_s07_reconciliation_rejects_missing_comparison_decision_or_authority_marker(self) -> None:
        reconciliation = read(DEFAULT_ROOT, str(DOCS / "reconciliation.md"))
        evidence = read(DEFAULT_ROOT, str(DOCS / "evidence.md"))
        for missing_marker in ("source commit", "### Reconciliation decisions", "mutation_authorized: false"):
            with self.assertRaises(ValueError):
                check_s07_reconciliation_record(reconciliation.replace(missing_marker, "missing", 1), evidence)

    def test_s07_reconciliation_rejects_every_required_marker_omission(self) -> None:
        reconciliation = read(DEFAULT_ROOT, str(DOCS / "reconciliation.md"))
        evidence = read(DEFAULT_ROOT, str(DOCS / "evidence.md"))
        required_markers = (
            "| source tree | `9c162c4f3d0d36c141517da6b61cfe21fe598dc8` |",
            "| target tree | `8924989ad8d3d676aff73c30ab93ad31878e7385` |",
            "| merge-base | `b45c3ea25a3de59b58329cf4c76e8938a1c7892c` |",
            "git diff --name-status b45c3ea2 4d286f7d9172f452fd3d0ae2458ef73c11376fea",
            "| baseline history | retain |",
            "| candidate delta | replace only after explicit authority |",
            "| publication-readiness package mapping | unresolved |",
            "| conflict expectation | unresolved, not waived |",
            "| dependency-graph provenance | retain unresolved classification |",
            "### Validation and authority gates",
            "### Rollback boundary",
            "plan_only: true",
            "mutation_authorized: false",
            "Require an explicit user authorization that names the proposed ref operation",
        )
        for marker in required_markers:
            with self.subTest(marker=marker), self.assertRaises(ValueError):
                check_s07_reconciliation_record(reconciliation.replace(marker, "missing", 1), evidence)

    def test_evidence_core_document_path_rejects_invalid_record(self) -> None:
        original_read = read
        evidence = original_read(DEFAULT_ROOT, str(DOCS / "evidence.md"))
        invalid_evidence = evidence.replace(
            "| ER-001 | Evidence record schema defined in this document | verified-fact |",
            "| ER-001 | Evidence record schema defined in this document | unsupported-classification |",
            1,
        )

        def read_invalid_evidence(root: Path, relative: str) -> str:
            if relative == str(DOCS / "evidence.md"):
                return invalid_evidence
            return original_read(root, relative)

        with patch(f"{__name__}.read", side_effect=read_invalid_evidence):
            results = dict(check_documents())
            self.assertEqual(results["S08 evidence schema core"], "evidence core has unsupported classification")

    def test_evidence_core_document_path_rejects_fresh_record_with_noncanonical_candidate(self) -> None:
        original_read = read
        evidence = original_read(DEFAULT_ROOT, str(DOCS / "evidence.md"))
        invalid_evidence = evidence.replace(
            "| ER-001 | Evidence record schema defined in this document | verified-fact | design.md#evidence-model | 2026-08-27 | 4d286f7d | current |",
            "| ER-001 | Evidence record schema defined in this document | verified-fact | design.md#evidence-model | 2026-08-27 | b45c3ea2 | current |",
            1,
        )

        def read_invalid_evidence(root: Path, relative: str) -> str:
            if relative == str(DOCS / "evidence.md"):
                return invalid_evidence
            return original_read(root, relative)

        with patch(f"{__name__}.read", side_effect=read_invalid_evidence):
            results = dict(check_documents())
        self.assertEqual(
            results["S08 evidence schema core"],
            "fresh evidence must use the exact candidate and environment reference",
        )

    def test_evidence_core_rejects_malformed_fresh_and_authority_inventing_records(self) -> None:




        record = {
            "id": "ER-100",
            "classification": "verified-fact",
            "freshness": "fresh",
            "status": "current",
            "candidate_ref": "4d286f7d9172f452fd3d0ae2458ef73c11376fea",
            "environment_ref": "ENV-Temurin-25",
            "blocker_links": "BL-001",
            "owner": "unassigned",
            "approver": "unassigned",
            "authority_ref": "",
            "disposition": "Retain as evidence only.",
            "invalidation_reason": "",
            "superseding_id": "",
        }
        validate_evidence_core(record, {"BL-001"})
        with self.assertRaisesRegex(ValueError, "fresh"):
            validate_evidence_core({**record, "candidate_ref": "b45c3ea2"}, {"BL-001"})
        with self.assertRaisesRegex(ValueError, "authority"):
            validate_evidence_core({**record, "owner": "invented-owner"}, {"BL-001"})

    def test_evidence_core_accepts_stale_history_and_rejects_dangling_links(self) -> None:
        record = {
            "id": "ER-101",
            "classification": "inference",
            "freshness": "stale",
            "status": "stale",
            "candidate_ref": "b45c3ea2",
            "environment_ref": "ENV-Homebrew-25",
            "blocker_links": "BL-001",
            "owner": "unassigned",
            "approver": "unassigned",
            "authority_ref": "",
            "disposition": "Requires fresh replacement evidence.",
            "invalidation_reason": "candidate-mismatch",
            "superseding_id": "ER-100",
        }
        validate_evidence_core(record, {"BL-001", "ER-100"})
        with self.assertRaisesRegex(ValueError, "dangling"):
            validate_evidence_core({**record, "blocker_links": "BL-999"}, {"BL-001", "ER-100"})
        with self.assertRaisesRegex(ValueError, "classification"):
            validate_evidence_core({**record, "classification": "fact"}, {"BL-001", "ER-100"})

    def test_evidence_core_rejects_dangling_nonfresh_candidate_reference(self) -> None:
        record = {
            "id": "ER-103", "classification": "inference", "freshness": "historical",
            "status": "stale", "candidate_ref": "unknown-candidate", "environment_ref": "ENV-Homebrew-25",
            "blocker_links": "BL-001", "owner": "unassigned", "approver": "unassigned",
            "authority_ref": "", "disposition": "Retain historical observation.",
            "invalidation_reason": "candidate-mismatch", "superseding_id": "",
        }
        with self.assertRaisesRegex(ValueError, "candidate"):
            validate_evidence_core(record, {"BL-001"})

    def test_evidence_core_rejects_dangling_nonfresh_environment_reference(self) -> None:
        record = {
            "id": "ER-104", "classification": "inference", "freshness": "stale",
            "status": "stale", "candidate_ref": "b45c3ea2", "environment_ref": "ENV-unknown",
            "blocker_links": "BL-001", "owner": "unassigned", "approver": "unassigned",
            "authority_ref": "", "disposition": "Requires fresh replacement evidence.",
            "invalidation_reason": "candidate-mismatch", "superseding_id": "",
        }
        with self.assertRaisesRegex(ValueError, "environment"):
            validate_evidence_core(record, {"BL-001"})

    def test_evidence_core_accepts_authoritative_references_for_every_freshness_mode(self) -> None:
        records = (
            ("ER-105", "historical", "stale", "be8826eb", "ENV-prior"),
            ("ER-106", "stale", "stale", "b45c3ea2", "ENV-Homebrew-25"),
            ("ER-107", "fresh", "current", "4d286f7d9172f452fd3d0ae2458ef73c11376fea", "ENV-Temurin-25"),
        )
        for identifier, freshness, status, candidate_ref, environment_ref in records:
            with self.subTest(freshness=freshness):
                validate_evidence_core({
                    "id": identifier, "classification": "verified-fact", "freshness": freshness,
                    "status": status, "candidate_ref": candidate_ref, "environment_ref": environment_ref,
                    "blocker_links": "BL-001", "owner": "unassigned", "approver": "unassigned",
                    "authority_ref": "", "disposition": "Retain the documented observation.",
                    "invalidation_reason": "candidate-mismatch" if freshness != "fresh" else "",
                    "superseding_id": "",
                }, {"BL-001"})

    def test_evidence_core_rejects_false_current_stale_and_dangling_supersession(self) -> None:
        record = {
            "id": "ER-102", "classification": "unresolved-question", "freshness": "stale",
            "status": "current", "candidate_ref": "be8826eb", "environment_ref": "ENV-prior",
            "blocker_links": "BL-001", "owner": "unassigned", "approver": "unassigned",
            "authority_ref": "", "disposition": "Await a fresh observation.",
            "invalidation_reason": "candidate-mismatch", "superseding_id": "",
        }

        with self.assertRaisesRegex(ValueError, "non-fresh"):
            validate_evidence_core(record, {"BL-001"})
        with self.assertRaisesRegex(ValueError, "dangling successor"):
            validate_evidence_core({**record, "freshness": "superseded", "status": "stale", "superseding_id": "ER-999"}, {"BL-001"})

    def test_s07_reconciliation_rejects_malformed_required_markers(self) -> None:
        reconciliation = read(DEFAULT_ROOT, str(DOCS / "reconciliation.md"))
        evidence = read(DEFAULT_ROOT, str(DOCS / "evidence.md"))
        malformed_markers = (
            ("| source tree | `9c162c4f3d0d36c141517da6b61cfe21fe598dc8` |", "| source tree | `invalid` |"),
            ("| target tree | `8924989ad8d3d676aff73c30ab93ad31878e7385` |", "| target tree | `invalid` |"),
            ("| merge-base | `b45c3ea25a3de59b58329cf4c76e8938a1c7892c` |", "| merge-base | `invalid` |"),
            ("| baseline history | retain |", "| baseline history | replace |"),
            ("| candidate delta | replace only after explicit authority |", "| candidate delta | retain |"),
            ("| publication-readiness package mapping | unresolved |", "| publication-readiness package mapping | retain |"),
            ("| conflict expectation | unresolved, not waived |", "| conflict expectation | resolved |"),
            ("| dependency-graph provenance | retain unresolved classification |", "| dependency-graph provenance | resolved |"),
            ("plan_only: true", "plan_only: false"),
            ("mutation_authorized: false", "mutation_authorized: true"),
        )
        for valid, malformed in malformed_markers:
            with self.subTest(malformed=malformed), self.assertRaises(ValueError):
                check_s07_reconciliation_record(reconciliation.replace(valid, malformed, 1), evidence)


    def test_s09_document_path_validates_declared_slice_records(self) -> None:
        original_read = read
        evidence = original_read(DEFAULT_ROOT, str(DOCS / "evidence.md"))
        record = """```s09-slice
outcome: S09 document path test
start_state: open
end_state: checked
path_allowlist: scripts/publication-readiness/check_records.py
protected_non_targets: .codegraph/, pi-session-*.html
dependencies: ER-014
evidence_links: ER-014
stale_evidence_links: ER-014
acceptance: Fail closed.
verification: self-test
runtime: N/A
additions: 400
deletions: 0
rollback: Revert this test record.
delivery_authority: not-authorized
instructions: Plan only: a future `git push` requires separate authority.
```"""

        empty_policy = "### S09 Slice Records\n\nCurrent empty-record policy: `no-s09-slice-records`."

        def s09_error(declared_evidence: str) -> str | None:
            def read_declared_evidence(root: Path, relative: str) -> str:
                if relative == str(DOCS / "evidence.md"):
                    return declared_evidence
                return original_read(root, relative)

            with patch(f"{__name__}.read", side_effect=read_declared_evidence):
                return dict(check_documents())["S09 slice integrity and operation safety"]

        def s09_result(records: str) -> str | None:
            if empty_policy in evidence:
                declared_evidence = evidence.replace(
                    empty_policy,
                    f"### S09 Slice Records\n\n{records}",
                    1,
                )
            else:
                declared_evidence = evidence.replace(
                    "\n    ## Verification Record Schema",
                    f"\n### S09 Slice Records\n\n{records}\n\n    ## Verification Record Schema",
                    1,
                )
            return s09_error(declared_evidence)

        self.assertIsNone(s09_result(record))
        self.assertEqual(s09_error(evidence.replace(empty_policy, "", 1)), "missing S09 slice record representation")
        duplicate_section = evidence + (
            "\n## Later S09 Context\n\n### S09 Slice Records\n\n"
            + record.replace("additions: 400", "additions: 401", 1)
        )
        self.assertEqual(s09_error(duplicate_section), "multiple S09 slice record representations")
        cases = (
            (record.replace("additions: 400", "additions: 401", 1), "slice exceeds hard 400-line budget"),
            (record.replace("path_allowlist: scripts/publication-readiness/check_records.py", "path_allowlist: .codegraph/snapshot", 1), "slice captures a protected non-target"),
            (record.replace("stale_evidence_links: ER-014", "stale_evidence_links:", 1), "slice stale-link propagation is incomplete"),
            (record.replace("delivery_authority: not-authorized", "delivery_authority: authorized", 1), "slice cannot grant delivery authority"),
            (record.replace("instructions: Plan only: a future `git push` requires separate authority.", "instructions: $ git push origin main", 1), "slice contains a forbidden mutation-capable instruction"),
            (record.replace("rollback: Revert this test record.\n", "", 1), "slice missing fields: rollback"),
            (record.replace("rollback: Revert this test record.", "rollback Revert this test record.", 1), "malformed S09 slice record"),
            (record + "\nCurrent empty-record policy: `no-s09-slice-records`.", "S09 slice records conflict with the empty-record policy"),
            (record.replace("outcome: S09 document path test", "outcome: S09 document path test\noutcome: duplicate", 1), "malformed S09 slice record"),
            (record + "\nundeclared content", "undeclared S09 slice record content"),
        )
        for invalid_records, expected_error in cases:
            with self.subTest(expected_error=expected_error):
                self.assertEqual(s09_result(invalid_records), expected_error)

    def test_s09_document_requires_semantic_validation(self) -> None:
        original_read = read
        evidence = original_read(DEFAULT_ROOT, str(DOCS / "evidence.md"))
        invalid_record = """```s09-slice
outcome: S09 semantic validation test
start_state: open
end_state: checked
path_allowlist: scripts/publication-readiness/check_records.py
protected_non_targets: .codegraph/, pi-session-*.html
dependencies: ER-001
evidence_links: ER-001
stale_evidence_links:
acceptance: Fail closed.
verification: self-test
runtime: N/A
additions: 400
deletions: 0
rollback: Revert this test record.
delivery_authority: authorized
instructions: Plan only: a future `git push` requires separate authority.
```"""
        invalid_evidence = evidence.replace(
            "### S09 Slice Records\n\nCurrent empty-record policy: `no-s09-slice-records`.",
            f"### S09 Slice Records\n\n{invalid_record}",
            1,
        )

        def read_invalid_evidence(root: Path, relative: str) -> str:
            if relative == str(DOCS / "evidence.md"):
                return invalid_evidence
            return original_read(root, relative)

        with patch(f"{__name__}.read", side_effect=read_invalid_evidence):
            results = dict(check_documents())
        self.assertEqual(
            results["S09 slice integrity and operation safety"],
            "slice cannot grant delivery authority",
        )

    def test_s09_slice_record_accepts_plan_only_wording_and_exact_budget(self) -> None:
        record = {
            "outcome": "Static record integrity", "start_state": "open", "end_state": "checked",
            "path_allowlist": "docs/publication-readiness/evidence.md",
            "protected_non_targets": ".codegraph/, pi-session-*.html",
            "dependencies": "ER-065", "evidence_links": "ER-065", "stale_evidence_links": "",
            "acceptance": "Fail closed.", "verification": "self-test", "runtime": "N/A",
            "additions": "400", "deletions": "0", "rollback": "Revert this record only.",
            "delivery_authority": "not-authorized",
            "instructions": "Plan only: a future `git push` requires separate authority.",
        }
        validate_s09_slice_record(record, {"ER-065": "current"})
        validate_s09_slice_record(
            {**record, "evidence_links": "ER-014", "stale_evidence_links": "ER-014"},
            {"ER-014": "stale"},
        )

    def test_s11_governance_ownership_rejects_missing_fields_manufactured_authority_and_inconsistent_topics(self) -> None:
        documents = dict(governance_files(DEFAULT_ROOT))
        evidence = read(DEFAULT_ROOT, str(DOCS / "evidence.md"))
        readiness = read(DEFAULT_ROOT, str(DOCS / "readiness-report.md"))
        check_s11_governance_ownership(documents, evidence, readiness)
        with self.assertRaisesRegex(ValueError, "missing required governance topic"):
            check_s11_governance_ownership(
                {**documents, "security.md": documents["security.md"].replace("private security-reporting guidance", "missing topic", 1)},
                evidence,
                readiness,
            )
        with self.assertRaisesRegex(ValueError, "manufactured owner or approver"):
            check_s11_governance_ownership(
                {**documents, "contributing.md": documents["contributing.md"].replace("| unresolved | unassigned | unassigned |", "| unresolved | invented-owner | unassigned |", 1)},
                evidence,
                readiness,
            )
        with self.assertRaisesRegex(ValueError, "invalid governance status or disposition"):
            check_s11_governance_ownership(
                {**documents, "code-of-conduct.md": documents["code-of-conduct.md"].replace("| unresolved |", "| approved |", 1)},
                evidence,
                readiness,
            )
        with self.assertRaisesRegex(ValueError, "broken governance evidence reference"):
            check_s11_governance_ownership(
                {**documents, "support-maintenance.md": documents["support-maintenance.md"].replace("ER-043", "ER-999", 1)},
                evidence,
                readiness,
            )
        with self.assertRaisesRegex(ValueError, "cross-topic governance inconsistency"):
            check_s11_governance_ownership(
{**documents, "release-controls.md": documents["release-controls.md"].replace("are not recorded", "is omitted", 1)},
                evidence,
                readiness,
            )

        def test_s10_controls_accept_pending_unassigned_records_with_current_evidence(self) -> None:
            check_s10_controls(
                read(DEFAULT_ROOT, str(DOCS / "destination-decision.yaml")),
                read(DEFAULT_ROOT, str(DOCS / "release-manifest.yaml")),
                read(DEFAULT_ROOT, str(DOCS / "reconciliation.md")),
                read(DEFAULT_ROOT, str(DOCS / "governance" / "release-controls.md")),
                read(DEFAULT_ROOT, str(DOCS / "evidence.md")),
            )


    def test_s10_controls_reject_missing_or_nonpending_signoff(self) -> None:
        destination = read(DEFAULT_ROOT, str(DOCS / "destination-decision.yaml"))
        manifest = read(DEFAULT_ROOT, str(DOCS / "release-manifest.yaml"))
        reconciliation = read(DEFAULT_ROOT, str(DOCS / "reconciliation.md"))
        controls = read(DEFAULT_ROOT, str(DOCS / "governance" / "release-controls.md"))
        evidence = read(DEFAULT_ROOT, str(DOCS / "evidence.md"))
        with self.assertRaisesRegex(ValueError, "sign-off"):
            check_s10_controls(
                destination.replace('signoff.status: "pending"\n', "", 1),
                manifest,
                reconciliation,
                controls,
                evidence,
            )
        with self.assertRaisesRegex(ValueError, "sign-off"):
            check_s10_controls(
                destination.replace('signoff.status: "pending"', 'signoff.status: "complete"', 1),
                manifest,
                reconciliation,
                controls,
                evidence,
            )


            destination = read(DEFAULT_ROOT, str(DOCS / "destination-decision.yaml"))
            manifest = read(DEFAULT_ROOT, str(DOCS / "release-manifest.yaml"))
            reconciliation = read(DEFAULT_ROOT, str(DOCS / "reconciliation.md"))
            controls = read(DEFAULT_ROOT, str(DOCS / "governance" / "release-controls.md"))
            evidence = read(DEFAULT_ROOT, str(DOCS / "evidence.md"))
            with self.assertRaisesRegex(ValueError, "authorization"):
                check_s10_controls(
                    destination.replace('authorization.maven_central.authorized: "not-authorized"', 'authorization.maven_central.authorized: "authorized"', 1),
                    manifest,
                    reconciliation,
                    controls,
                    evidence,
                )
            for authorization in ("ready", "publishable"):
                with self.subTest(authorization=authorization), self.assertRaisesRegex(ValueError, "authorization"):
                    check_s10_controls(
                        destination.replace('authorization.maven_central.authorized: "not-authorized"', f'authorization.maven_central.authorized: "{authorization}"', 1),
                        manifest,
                        reconciliation,
                        controls,
                        evidence,
                    )
                with self.assertRaisesRegex(ValueError, "authorization"):
                    check_s10_controls(
                        destination.replace('source_publication.authorized: "not-authorized"', 'source_publication.authorized: "authorized"', 1),
                        manifest,
                        reconciliation,
                        controls,
                        evidence,
                    )

    def test_s10_controls_preserves_not_authorized_and_rejects_approved_authorization(self) -> None:
        destination = read(DEFAULT_ROOT, str(DOCS / "destination-decision.yaml"))
        manifest = read(DEFAULT_ROOT, str(DOCS / "release-manifest.yaml"))
        reconciliation = read(DEFAULT_ROOT, str(DOCS / "reconciliation.md"))
        controls = read(DEFAULT_ROOT, str(DOCS / "governance" / "release-controls.md"))
        evidence = read(DEFAULT_ROOT, str(DOCS / "evidence.md"))
        check_s10_controls(destination, manifest, reconciliation, controls, evidence)
        with self.assertRaisesRegex(ValueError, "authorization"):
            check_s10_controls(
                destination.replace(
                    'authorization.maven_central.authorized: "not-authorized"',
                    'authorization.maven_central.authorized: "approved"',
                    1,
                ),
                manifest,
                reconciliation,
                controls,
                evidence,
            )

    def test_authorization_prerequisites_distinguish_complete_partial_and_inconsistent_records(self) -> None:

        destination = flat_yaml(read(DEFAULT_ROOT, str(DOCS / "destination-decision.yaml")))
        manifest = flat_yaml(read(DEFAULT_ROOT, str(DOCS / "release-manifest.yaml")))
        complete_destination = {
            **destination,
            "decision_status": "approved",
            "selected_destination.status": "approved",
            "owners.decision_owner": "decision-owner",
            "owners.repository_owner": "repository-owner",
            "owners.release_approver": "release-approver",
            "owners.governance_approver": "governance-approver",
            "dates.decision_date": "2026-08-30",
            "dates.approval_date": "2026-08-30",
            "signoff.status": "complete",
            "signoff.effective_date": "2026-08-30",
        }
        complete_manifest = {**manifest, "manifest_status": "resolved", "fields.version.state": "observed", "fields.version.owner": "release-owner"}
        self.assertTrue(authorization_prerequisites_are_complete(complete_destination, complete_manifest))
        self.assertFalse(authorization_prerequisites_are_complete(destination, manifest))
        self.assertFalse(authorization_prerequisites_are_complete({**complete_destination, "signoff.effective_date": "pending"}, complete_manifest))
        self.assertFalse(authorization_prerequisites_are_complete({**complete_destination, "owners.release_approver": "decision-owner"}, complete_manifest))



    def test_s10_controls_reject_approval_with_pending_authority_or_inconsistent_metadata(self) -> None:
        destination = read(DEFAULT_ROOT, str(DOCS / "destination-decision.yaml"))
        manifest = read(DEFAULT_ROOT, str(DOCS / "release-manifest.yaml"))
        reconciliation = read(DEFAULT_ROOT, str(DOCS / "reconciliation.md"))
        controls = read(DEFAULT_ROOT, str(DOCS / "governance" / "release-controls.md"))
        evidence = read(DEFAULT_ROOT, str(DOCS / "evidence.md"))
        with self.assertRaisesRegex(ValueError, "approval"):
            check_s10_controls(destination.replace('decision_status: "pending"', 'decision_status: "approved"', 1), manifest, reconciliation, controls, evidence)
        with self.assertRaisesRegex(ValueError, "candidate"):
            check_s10_controls(destination, manifest.replace('candidate_id: "4d286f7d"', 'candidate_id: "b45c3ea2"', 1), reconciliation, controls, evidence)
        with self.assertRaisesRegex(ValueError, "ER-066"):
            check_s10_controls(destination, manifest, reconciliation, controls, evidence.replace("| ER-066 |", "| ER-999 |", 1))



    def test_s12_public_claims_reject_stale_candidate_unsupported_readiness_dead_ownership_and_completed_json_migration(self) -> None:
        documents = {
            "README.md": read(DEFAULT_ROOT, "README.md"),
            "CHANGELOG.md": read(DEFAULT_ROOT, "CHANGELOG.md"),
            "FINAL_STATUS_REPORT.md": read(DEFAULT_ROOT, "FINAL_STATUS_REPORT.md"),
            "MIGRATION_PLAN.md": read(DEFAULT_ROOT, "MIGRATION_PLAN.md"),
            "MIGRATION_REPORT.md": read(DEFAULT_ROOT, "MIGRATION_REPORT.md"),
            "VALIDATION_REPORT_FINAL.md": read(DEFAULT_ROOT, "VALIDATION_REPORT_FINAL.md"),
            ".github/ISSUE_TEMPLATE.md": read(DEFAULT_ROOT, ".github/ISSUE_TEMPLATE.md"),
            ".github/PULL_REQUEST_TEMPLATE.md": read(DEFAULT_ROOT, ".github/PULL_REQUEST_TEMPLATE.md"),
        }
        evidence = read(DEFAULT_ROOT, str(DOCS / "evidence.md"))
        check_s12_public_claims(documents, evidence)
        with self.assertRaisesRegex(ValueError, "stale candidate"):
            check_s12_public_claims({**documents, "README.md": documents["README.md"].replace('1.0.0-RC6', '1.0.0-RC5', 1)}, evidence)
        with self.assertRaisesRegex(ValueError, "unsupported readiness"):
            check_s12_public_claims({**documents, "CHANGELOG.md": documents["CHANGELOG.md"].replace('**Not released.**', '**Released.**', 1)}, evidence)
        with self.assertRaisesRegex(ValueError, "dead ownership"):
            check_s12_public_claims({**documents, ".github/ISSUE_TEMPLATE.md": documents[".github/ISSUE_TEMPLATE.md"].replace("Do not tag `@getquill/maintainers`", "Tag `@getquill/maintainers`", 1)}, evidence)
        with self.assertRaisesRegex(ValueError, "JSON migration"):
            check_s12_public_claims({**documents, "README.md": documents["README.md"].replace('remains deferred', 'is complete', 1)}, evidence)

    def test_s12_public_claims_accept_current_evidence_historical_banners_and_deferred_json_migration(self) -> None:
        documents = {
            "README.md": read(DEFAULT_ROOT, "README.md"),
            "CHANGELOG.md": read(DEFAULT_ROOT, "CHANGELOG.md"),
            "FINAL_STATUS_REPORT.md": read(DEFAULT_ROOT, "FINAL_STATUS_REPORT.md"),
            "MIGRATION_PLAN.md": read(DEFAULT_ROOT, "MIGRATION_PLAN.md"),
            "MIGRATION_REPORT.md": read(DEFAULT_ROOT, "MIGRATION_REPORT.md"),
            "VALIDATION_REPORT_FINAL.md": read(DEFAULT_ROOT, "VALIDATION_REPORT_FINAL.md"),
            ".github/ISSUE_TEMPLATE.md": read(DEFAULT_ROOT, ".github/ISSUE_TEMPLATE.md"),
            ".github/PULL_REQUEST_TEMPLATE.md": read(DEFAULT_ROOT, ".github/PULL_REQUEST_TEMPLATE.md"),
        }
        check_s12_public_claims(documents, read(DEFAULT_ROOT, str(DOCS / "evidence.md")))

    def test_s09_slice_record_rejects_cross_reference_scope_budget_and_mutation_failures(self) -> None:
        record = {
            "outcome": "Static record integrity", "start_state": "open", "end_state": "checked",
            "path_allowlist": "docs/publication-readiness/evidence.md",
            "protected_non_targets": ".codegraph/, pi-session-*.html",
            "dependencies": "ER-065", "evidence_links": "ER-065", "stale_evidence_links": "",
            "acceptance": "Fail closed.", "verification": "self-test", "runtime": "N/A",
            "additions": "400", "deletions": "0", "rollback": "Revert this record only.",
            "delivery_authority": "not-authorized", "instructions": "Plan only.",
        }
        cases = (
            ({**record, "evidence_links": "ER-999"}, "dangling"),
            ({**record, "evidence_links": "ER-014"}, "stale"),
            ({**record, "path_allowlist": ".codegraph/snapshot"}, "protected"),
            ({**record, "additions": "401"}, "400"),
            ({**record, "instructions": "$ git push origin main"}, "forbidden"),
            ({key: value for key, value in record.items() if key != "rollback"}, "missing"),
        )
        statuses = {"ER-065": "current", "ER-014": "stale"}
        for invalid, message in cases:
            with self.subTest(message=message), self.assertRaisesRegex(ValueError, message):
                validate_s09_slice_record(invalid, statuses)


    def test_s15_aggregation_fails_closed_for_missing_stale_auto_closed_authority_free_and_over_budget_inputs(self) -> None:
        blockers = tuple({"id": f"BL-00{index}", "status": "open", "freshness": "stale" if index == 1 else "current", "evidence": "ER-065", "gap": "unresolved", "owner": "unassigned", "approver": "unassigned", "required_disposition": "authorized decision required"} for index in range(1, 8))
        context = {"candidate": "master@4d286f7d9172f452fd3d0ae2458ef73c11376fea", "tree": "8924989ad8d3d676aff73c30ab93ad31878e7385", "repository_kind": "normal-local-clone", "temurin": "25", "plan_only": True, "mutation_authorized": False, "json_migration": "deferred", "provenance": "unresolved"}
        slices = tuple({"id": f"S{index:02d}", "additions": 1, "deletions": 0, "authority": "not-authorized", "evidence": "ER-065", "exclusions": "protected state"} for index in range(1, 16))
        validate_s15_aggregation(blockers, slices, context)
        cases = ((blockers[:-1], slices, context, "seven blockers"), ({**blockers[0], "status": "closed", "authority_ref": "AR-001", "owner": "named", "approver": "named"}, slices, context, "fresh attributable"), (blockers, slices, {**context, "temurin": "25-auto-closed"}, "auto-close"), ({**blockers[0], "status": "closed", "freshness": "current", "owner": "named", "approver": "named", "required_disposition": "authorized decision required"}, slices, context, "authority"), (blockers, ({**slices[0], "additions": 401}, *slices[1:]), context, "400"))
        for changed_blockers, changed_slices, changed_context, message in cases:
            if isinstance(changed_blockers, dict):
                changed_blockers = (changed_blockers, *blockers[1:])
            with self.subTest(message=message), self.assertRaisesRegex(ValueError, message):
                validate_s15_aggregation(changed_blockers, changed_slices, changed_context)

    def test_s13_event_graph_rejects_unsafe_fixture_and_accepts_expected_routes(self) -> None:
        check_s13_event_graph(DEFAULT_ROOT)

    def test_s13_event_graph_rejects_missing_ci_safety_evidence(self) -> None:
        with self.assertRaisesRegex(ValueError, "CI-safety evidence"):
            check_s13_event_graph(DEFAULT_ROOT, evidence="## Evidence\n")

    def test_s14_workflow_safety_requires_explicit_published_release_and_truthful_provenance(self) -> None:
        check_s14_workflow_safety(DEFAULT_ROOT)

    def test_s14_workflow_structure_rejects_invalid_release_indentation_and_scope_drift(self) -> None:
        ci = read(DEFAULT_ROOT, ".github/workflows/ci.yml")
        dependency_graph = read(DEFAULT_ROOT, ".github/workflows/dependency-graph.yml")
        drafter = read(DEFAULT_ROOT, ".github/workflows/release-drafter.yml")
        check_s14_workflow_structure(ci, dependency_graph, drafter)
        with self.assertRaisesRegex(ValueError, "release job"):
            check_s14_workflow_structure(
                ci.replace("    if: ${{ github.event_name == 'release' && github.event.action == 'published' }}", "      if: ${{ github.event_name == 'release' && github.event.action == 'published' }}", 1),
                dependency_graph,
                drafter,
            )
        with self.assertRaisesRegex(ValueError, "publication secrets"):
            check_s14_workflow_structure(
                ci.replace("./build/build.sh ${{ matrix.module }}", "./build/build.sh ${{ matrix.module }}\n              echo ${{ secrets.PGP_SECRET }}", 1),
                dependency_graph,
                drafter,
            )



def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Validate documented publication-readiness records.")
    parser.add_argument("--root", type=Path, default=DEFAULT_ROOT, help="repository root containing docs/publication-readiness")
    parser.add_argument("--self-test", action="store_true", help="run the in-file behavioral tests")
    args = parser.parse_args(argv)
    if args.self_test:
        suite = unittest.defaultTestLoader.loadTestsFromTestCase(RecoveryContractTests)
        return 0 if unittest.TextTestRunner(verbosity=2).run(suite).wasSuccessful() else 1
    return run(args.root)


if __name__ == "__main__":
    sys.exit(main())
