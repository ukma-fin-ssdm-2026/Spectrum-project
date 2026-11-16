"""Collects CI runs on `main` through the GitHub Actions API and computes the four DORA metrics.

Definitions (Lab 10):
- deployment: a green (successful) run of the CI workflow on `main`;
- change lead time: from the first commit of the merged pull request (or the commit itself for a
  direct push) to the end of the green `main` run with that commit; queue and execution are
  part of it and are measured separately;
- queue: from the creation of the run to the start of its first job (waiting for a runner);
- execution: from the start of the first job to the end of the last job;
- change fail rate: red `main` runs divided by all completed `main` runs;
- recovery time: from a red `main` run to the next green `main` run.

Usage (needs an authenticated GitHub CLI):
    python scripts/dora_metrics.py --since 2026-10-24 --until 2026-11-14
    python scripts/dora_metrics.py --since 2026-10-17 --until 2026-11-14 --name 4-weeks

Writes to `docs/ci-cd/evidence/`: the table rows for `metrics-raw.md` (`metrics-table*.md`),
the computed metrics (`dora-metrics*.json`) and the raw API answers (`github-api-runs*.json`).
"""

import argparse
import json
import statistics
import subprocess
from datetime import datetime, timezone
from pathlib import Path

REPO = "ukma-fin-ssdm-2026/Spectrum-project"
# "CI Test" (github-actions-test.yml) was merged into "CI" (ci.yml) on 24.10.2026.
CI_WORKFLOWS = {"CI", "CI Test"}
ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "docs" / "ci-cd"


def gh_api(path):
    result = subprocess.run(
        ["gh", "api", "-H", "Accept: application/vnd.github+json", path],
        check=True,
        capture_output=True,
        text=True,
        encoding="utf-8",
    )
    return json.loads(result.stdout)


def parse_time(value):
    return datetime.strptime(value, "%Y-%m-%dT%H:%M:%SZ").replace(tzinfo=timezone.utc)


def minutes(start, end):
    return round((end - start).total_seconds() / 60, 1)


def fetch_runs(since, until):
    runs, page = [], 1
    while True:
        answer = gh_api(
            f"repos/{REPO}/actions/runs?branch=main&event=push&per_page=100&page={page}"
            f"&created={since}..{until}"
        )
        runs.extend(answer["workflow_runs"])
        if len(answer["workflow_runs"]) < 100:
            return runs
        page += 1


def first_commit(sha):
    """Returns (pull request number or None, time of its first commit)."""
    pulls = gh_api(f"repos/{REPO}/commits/{sha}/pulls")
    merged = [p for p in pulls if p.get("merged_at") and p["base"]["ref"] == "main"]
    if merged:
        pr = merged[0]["number"]
        commits = gh_api(f"repos/{REPO}/pulls/{pr}/commits?per_page=100")
        times = [parse_time(c["commit"]["author"]["date"]) for c in commits]
        return pr, min(times), commits
    commit = gh_api(f"repos/{REPO}/commits/{sha}")
    return None, parse_time(commit["commit"]["author"]["date"]), [commit]


def collect(since, until):
    raw_runs = [r for r in fetch_runs(since, until) if r["name"] in CI_WORKFLOWS]
    raw_runs = [r for r in raw_runs if r["status"] == "completed"]
    raw_runs.sort(key=lambda r: r["created_at"])
    evidence = {"runs": raw_runs, "jobs": {}, "pulls": {}}
    rows = []
    for run in raw_runs:
        jobs = gh_api(f"repos/{REPO}/actions/runs/{run['id']}/jobs")["jobs"]
        evidence["jobs"][run["id"]] = jobs
        created = parse_time(run["created_at"])
        started = min(parse_time(j["started_at"]) for j in jobs)
        completed = max(parse_time(j["completed_at"]) for j in jobs if j["completed_at"])
        pr, first, commits = first_commit(run["head_sha"])
        evidence["pulls"][run["head_sha"]] = {"pr": pr, "commits": commits}
        rows.append(
            {
                "run_id": run["id"],
                "workflow": run["name"],
                "attempt": run["run_attempt"],
                "commit": run["head_sha"][:7],
                "title": run["display_title"].splitlines()[0],
                "pr": pr,
                "first_commit": first,
                "created": created,
                "completed": completed,
                "queue": minutes(created, started),
                "execution": minutes(started, completed),
                "lead": minutes(first, completed),
                "green": run["conclusion"] == "success",
                "conclusion": run["conclusion"],
            }
        )
    return rows, evidence


def summarize(rows, since, until):
    weeks = (parse_time(until + "T00:00:00Z") - parse_time(since + "T00:00:00Z")).days / 7
    green = [r for r in rows if r["green"]]
    red = [r for r in rows if not r["green"]]
    leads = [r["lead"] for r in green]
    # One incident per streak of red runs: from its first red run to the next green run.
    recoveries = []
    for previous, r in zip([None] + rows[:-1], rows):
        if r["green"] or (previous is not None and not previous["green"]):
            continue
        later = [g for g in green if g["completed"] > r["completed"]]
        if later:
            recoveries.append(
                {
                    "first_red": r["commit"],
                    "next_green": later[0]["commit"],
                    "minutes": minutes(r["completed"], later[0]["completed"]),
                }
            )
    total_lead = sum(leads)
    return {
        "period": f"{since}..{until}",
        "weeks": round(weeks, 2),
        "runs": len(rows),
        "green": len(green),
        "red": len(red),
        "deployment_frequency_per_week": round(len(green) / weeks, 2),
        "lead_time_median_h": round(statistics.median(leads) / 60, 1),
        "lead_time_mean_h": round(statistics.mean(leads) / 60, 1),
        "lead_time_min_h": round(min(leads) / 60, 2),
        "lead_time_max_h": round(max(leads) / 60, 1),
        "queue_share_pct": round(100 * sum(r["queue"] for r in green) / total_lead, 2),
        "execution_share_pct": round(100 * sum(r["execution"] for r in green) / total_lead, 2),
        "queue_median_min": statistics.median(r["queue"] for r in rows),
        "execution_median_min": statistics.median(r["execution"] for r in rows),
        "change_fail_rate_pct": round(100 * len(red) / len(rows), 1),
        "recoveries": recoveries,
    }


def write_table(rows):
    lines = [
        "| № | Commit | PR | First PR commit (UTC) | Run completed (UTC) | Queue (min) "
        "| Execution (min) | Status | Notes |",
        "|---|--------|----|-----------------------|---------------------|-------------"
        "|-----------------|--------|-------|",
    ]
    for i, r in enumerate(rows, 1):
        pr = f"#{r['pr']}" if r["pr"] else "direct push"
        status = "green" if r["green"] else f"red ({r['conclusion']})"
        note = f"{r['workflow']}, run {r['run_id']}, attempt {r['attempt']}: {r['title']}"
        lines.append(
            f"| {i} | `{r['commit']}` | {pr} | {r['first_commit']:%Y-%m-%d %H:%M} "
            f"| {r['completed']:%Y-%m-%d %H:%M} | {r['queue']} | {r['execution']} | {status} "
            f"| {note.replace('|', '/')} |"
        )
    return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--since", required=True, help="first day, YYYY-MM-DD")
    parser.add_argument("--until", required=True, help="last day, YYYY-MM-DD")
    parser.add_argument("--name", default="", help="suffix of the output files")
    args = parser.parse_args()
    suffix = f"-{args.name}" if args.name else ""

    rows, evidence = collect(args.since, args.until)
    summary = summarize(rows, args.since, args.until)
    (OUT / "evidence").mkdir(parents=True, exist_ok=True)
    (OUT / "evidence" / f"github-api-runs{suffix}.json").write_text(
        json.dumps(evidence, indent=2, default=str), encoding="utf-8"
    )
    serializable = [{k: (v.isoformat() if isinstance(v, datetime) else v) for k, v in r.items()} for r in rows]
    (OUT / "evidence" / f"dora-metrics{suffix}.json").write_text(
        json.dumps({"summary": summary, "runs": serializable}, indent=2), encoding="utf-8"
    )
    (OUT / "evidence" / f"metrics-table{suffix}.md").write_text(write_table(rows) + "\n", encoding="utf-8")
    print(json.dumps(summary, indent=2))


if __name__ == "__main__":
    main()
