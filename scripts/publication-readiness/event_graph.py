#!/usr/bin/env python3
"""Fail-closed static GitHub Actions event-graph analyzer.

The analyzer reads workflow text only. It reports artifact-publication routes
separately from repository-mutating routes so dependency submission is never
misrepresented as artifact publication. It does not execute workflows, read
secrets, or grant workflow, release, or delivery authority.
"""
from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

DEFAULT_ROOT = Path(__file__).resolve().parents[2]
DEFAULT_WORKFLOWS_DIR = ".github/workflows"
PUBLICATION_COMMAND_PATTERNS = [
    re.compile(pattern, re.IGNORECASE) for pattern in (
        r"\bsbt\s+ci-release\b", r"\bsbt\s+publishSigned\b",
        r"\bsbt\s+sonatypeBundleRelease\b", r"\bsbt\s+publish\b(?!Local)",
        r"\bmvn\s+deploy\b", r"\bnpm\s+publish\b",
    )
]
PUBLICATION_SECRET_NAMES = {
    "SONATYPE_USERNAME", "SONATYPE_PASSWORD", "PGP_PASSPHRASE", "PGP_SECRET",
    "GPG_PASSPHRASE", "GPG_SECRET",
}
FIXTURES = {
    "pull_request": {"event_name": "pull_request"},
    "ordinary_push": {"event_name": "push", "ref_branch": "master"},
    "authorized_release": {"event_name": "release", "release_action": "published"},
    "unauthorized_release": {"event_name": "release", "release_action": "created"},
}
FIXTURE_EXPECTATIONS = {
    "unsafe-ordinary-push.yml": ("ordinary_push", True, ()),
    "pull-request.yml": ("pull_request", False, ()),
    "authorized-release.yml": ("authorized_release", True, ()),
    "unauthorized-release.yml": ("unauthorized_release", False, ()),
    "secret-scope.yml": ("ordinary_push", True, ()),
    "dependency-submission.yml": ("ordinary_push", False, ("dependency-submission",)),
}


class WorkflowParseError(Exception):
    """Raised when a workflow file does not match the supported static shape."""


def _strip_comment(line: str) -> str:
    return line.split("#", 1)[0]


def parse_triggers(text: str) -> dict[str, str]:
    lines = text.splitlines()
    start = next((index for index, line in enumerate(lines) if line.rstrip() == "on:"), None)
    if start is None:
        raise WorkflowParseError("no top-level 'on:' block found")
    triggers: dict[str, str] = {}
    index = start + 1
    while index < len(lines):
        match = re.match(r"^  ([A-Za-z_]+):", _strip_comment(lines[index]).rstrip())
        if not match:
            break
        event, end = match.group(1), index + 1
        while end < len(lines) and (_strip_comment(lines[end]).rstrip().strip() == "" or lines[end].startswith("    ")):
            end += 1
        triggers[event] = "\n".join(lines[index + 1:end])
        index = end
    return triggers


def _list_value(block: str, key: str) -> list[str]:
    match = re.search(rf"{key}:\s*\[(.*?)\]", block)
    return [item.strip().strip("'\"") for item in match.group(1).split(",")] if match else []


def trigger_fires(triggers: dict[str, str], fixture: dict[str, str]) -> bool:
    event = fixture["event_name"]
    if event not in triggers:
        return False
    if event == "push":
        branches = _list_value(triggers[event], "branches")
        return not branches or fixture.get("ref_branch") in branches
    if event == "release":
        types = _list_value(triggers[event], "types")
        return not types or fixture.get("release_action") in types
    return True


def split_jobs(text: str) -> dict[str, str]:
    lines = text.splitlines()
    start = next((index for index, line in enumerate(lines) if line.rstrip() == "jobs:"), None)
    if start is None:
        raise WorkflowParseError("no top-level 'jobs:' key found")
    starts = [(index, match.group(1)) for index, line in enumerate(lines[start + 1:], start + 1)
              if (match := re.match(r"^  ([A-Za-z0-9_.-]+):\s*$", _strip_comment(line).rstrip()))]
    return {name: "\n".join(lines[line:end]) for (line, name), (end, _) in zip(starts, starts[1:] + [(len(lines), "")])}


def job_condition(job_text: str) -> str | None:
    match = re.search(r"^\s+if:\s*(.+)$", job_text, re.MULTILINE)
    return match.group(1).strip() if match else None


def eval_condition(expression: str | None, context: dict[str, str]) -> bool:
    if expression is None:
        return True
    expression = expression.strip().removeprefix("${{").removesuffix("}}").strip()
    for separator, reducer in (("&&", all), ("||", any)):
        if separator in expression:
            return reducer(eval_condition(part, context) for part in expression.split(separator))
    match = re.fullmatch(r"(.+?)(==|!=)(.+)", expression)
    if not match:
        raise WorkflowParseError(f"unsupported condition expression: {expression!r}")
    left, operation, right = (part.strip().strip("'\"") for part in match.groups())
    return (context.get(left, left) == context.get(right, right)) == (operation == "==")


def route_findings(job_text: str) -> dict[str, object]:
    secrets = set(re.findall(r"secrets\.(\w+)", job_text)) & PUBLICATION_SECRET_NAMES
    mutations = []
    if "release-drafter/release-drafter@" in job_text:
        mutations.append("release-draft")
    if "sbt-dependency-submission@" in job_text:
        mutations.append("dependency-submission")
    return {
        "publication_command": any(pattern.search(job_text) for pattern in PUBLICATION_COMMAND_PATTERNS),
        "publication_secrets": sorted(secrets),
        "repository_mutations": mutations,
    }


def analyze_fixture(workflow_paths: list[Path], fixture_name: str) -> list[dict[str, object]]:
    fixture = FIXTURES[fixture_name]
    context = {
        "github.event_name": fixture["event_name"],
        "github.event.action": fixture.get("release_action", ""),
    }
    routes = []
    for path in workflow_paths:
        try:
            workflow = path.read_text(encoding="utf-8")
            triggers = parse_triggers(workflow)
            if not trigger_fires(triggers, fixture):
                continue
            for job, text in split_jobs(workflow).items():
                condition = job_condition(text)
                if eval_condition(condition, context):
                    finding = route_findings(text)
                    if any((finding["publication_command"], finding["publication_secrets"], finding["repository_mutations"])):
                        routes.append({"file": path.name, "job": job, "condition": condition, **finding})
        except WorkflowParseError:
            continue
    return routes


def run(workflows_dir: Path) -> dict[str, list[dict[str, object]]]:
    paths = sorted((*workflows_dir.glob("*.yml"), *workflows_dir.glob("*.yaml")))
    return {name: analyze_fixture(paths, name) for name in FIXTURES}


def publication_routes(routes: list[dict[str, object]]) -> list[dict[str, object]]:
    return [route for route in routes if route["publication_command"] or route["publication_secrets"]]


def check_fixture_dir(fixtures_dir: Path) -> list[str]:
    errors = []
    for name, (event, publication_expected, mutations_expected) in FIXTURE_EXPECTATIONS.items():
        routes = analyze_fixture([fixtures_dir / name], event)
        if bool(publication_routes(routes)) != publication_expected:
            errors.append(f"{name}: publication expectation failed")
        mutations = {mutation for route in routes for mutation in route["repository_mutations"]}
        if mutations != set(mutations_expected):
            errors.append(f"{name}: repository-mutation expectation failed")
    return errors


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--workflows-dir", default=str(DEFAULT_ROOT / DEFAULT_WORKFLOWS_DIR))
    parser.add_argument("--fixtures-dir", type=Path)
    args = parser.parse_args(argv)
    if args.fixtures_dir:
        errors = check_fixture_dir(args.fixtures_dir)
        for error in errors:
            print(f"FAIL: {error}")
        print(f"FIXTURES: {'PASS' if not errors else 'FAIL'}")
        return 0 if not errors else 1
    results = run(Path(args.workflows_dir))
    unsafe = {name: publication_routes(results[name]) for name in ("pull_request", "ordinary_push", "unauthorized_release")}
    for name, routes in results.items():
        print(f"[{name}] publication={len(publication_routes(routes))} mutations={sum(len(route['repository_mutations']) for route in routes)}")
    for name, routes in unsafe.items():
        for route in routes:
            print(f"UNAUTHORIZED PUBLICATION: {name} {route['file']}:{route['job']}")
    print("RESULT: PASS" if not any(unsafe.values()) else "RESULT: FAIL")
    return 0 if not any(unsafe.values()) else 1


if __name__ == "__main__":
    sys.exit(main())
