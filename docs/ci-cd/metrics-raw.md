## CI/CD metrics: raw data

Go back to the [Main README](../../README.md). Analysis: [DORA summary](dora-summary.md).

**Period:** 24.10.2026 – 14.11.2026 (3 weeks), runs on `main` of the CI workflow: `CI`
(`.github/workflows/ci.yml`) and, until 24.10, its predecessor `CI Test`
(`github-actions-test.yml`, merged into `ci.yml` in PR #84). SonarQube Cloud, Generate and
Deploy API Docs and Dependabot runs are not counted (see the
[deployment definition](dora-summary.md#deployment-definition-and-counted-workflows)).

**Columns:**

- **First PR commit:** the earliest author date of a commit in the merged pull request that
  brought the change to `main`; for a direct push, the commit itself.
- **Run completed:** the end of the last job of the run.
- **Queue:** from the creation of the run to the start of its first job (waiting for a
  runner). **Execution:** from the start of the first job to the end of the last job.
- Times are UTC. Every row links to a run id; the API answers for each run, its jobs and
  its pull request are in [evidence](evidence/).

**How the data was collected:** with [`scripts/dora_metrics.py`](../../scripts/dora_metrics.py)
through the GitHub Actions API, and checked by hand with `gh run list` /
`gh run view` ([gh-run-list.json](evidence/gh-run-list.json)); see
[differences](dora-summary.md#automatic-metric-calculation).

### Runs on `main`, 24.10 – 14.11.2026

| № | Commit | PR | First PR commit (UTC) | Run completed (UTC) | Queue (min) | Execution (min) | Status | Notes |
|---|--------|----|-----------------------|---------------------|-------------|-----------------|--------|-------|
| 1 | `b9e6e64` | #69 | 2026-10-18 21:08 | 2026-10-24 17:53 | 0.1 | 0.9 | green | CI Test, run 40118124922, attempt 1: Merge pull request #69 from ukma-fin-ssdm-2026/docs/lab02-fr-prioriti… |
| 2 | `e4380f3` | #70 | 2026-10-18 21:16 | 2026-10-24 17:55 | 0.1 | 0.9 | green | CI Test, run 40118229843, attempt 1: Merge pull request #70 from ukma-fin-ssdm-2026/docs/lab03-sequence-di… |
| 3 | `e8c0430` | #71 | 2026-10-18 22:43 | 2026-10-24 17:57 | 0.6 | 1.1 | green | CI Test, run 40118374893, attempt 1: Merge pull request #71 from ukma-fin-ssdm-2026/docs/lab03-matrix-scen… |
| 4 | `71c9687` | #72 | 2026-10-18 22:50 | 2026-10-24 17:57 | 0.0 | 1.2 | green | CI Test, run 40118427413, attempt 1: Merge pull request #72 from ukma-fin-ssdm-2026/docs/lab05-progress |
| 5 | `adb6ae7` | #73 | 2026-10-18 22:51 | 2026-10-24 17:58 | 0.1 | 1.1 | green | CI Test, run 40118473742, attempt 1: Merge pull request #73 from ukma-fin-ssdm-2026/docs/lab05-threat-anal… |
| 6 | `5a1a27e` | #74 | 2026-10-18 22:53 | 2026-10-24 17:59 | 0.0 | 1.0 | green | CI Test, run 40118533912, attempt 1: Merge pull request #74 from ukma-fin-ssdm-2026/docs/lab05-quality-policy |
| 7 | `1fa25bd` | #75 | 2026-10-18 22:55 | 2026-10-24 18:00 | 0.1 | 0.9 | green | CI Test, run 40118616941, attempt 1: Merge pull request #75 from ukma-fin-ssdm-2026/ci/lab06-tests-on-pull… |
| 8 | `bdc733c` | #81 | 2026-10-24 09:49 | 2026-10-24 18:14 | 0.0 | 0.9 | green | CI Test, run 40119706117, attempt 1: fix: register request password logging (#81) |
| 9 | `97be4c2` | #78 | 2026-10-19 18:17 | 2026-10-24 20:49 | 0.0 | 1.0 | green | CI Test, run 40131468117, attempt 1: docs: initial test plan (#78) |
| 10 | `c793941` | #84 | 2026-10-24 16:21 | 2026-10-24 21:47 | 0.0 | 2.1 | green | CI, run 40135570709, attempt 1: feat: db support (#84) |
| 11 | `575e68b` | #85 | 2026-10-24 21:42 | 2026-10-25 04:49 | 0.0 | 1.7 | green | CI, run 40163198262, attempt 1: chore: lab07 Demchyna (#85) |
| 12 | `a86b43c` | #88 | 2026-10-25 19:23 | 2026-10-25 20:01 | 0.0 | 1.9 | green | CI, run 40228600363, attempt 1: Add SonarQube Cloud analysis workflow (#88) |
| 13 | `9a9a16c` | #87 | 2026-10-25 05:33 | 2026-10-30 17:20 | 0.2 | 2.4 | green | CI, run 41097621528, attempt 1: feat: protect hotel creation, fix JWT verification and hotel sorting … |
| 14 | `7585214` | #92 | 2026-10-27 17:23 | 2026-10-31 16:44 | 0.0 | 2.5 | green | CI, run 41220949886, attempt 1: feat: room functionality |
| 15 | `9d88f0d` | #94 | 2026-10-31 19:01 | 2026-10-31 22:41 | 0.1 | 2.4 | green | CI, run 41249105601, attempt 1: docs: init sonarcloud-report.md [REFACTOR] (#94) |
| 16 | `60cdcf1` | #96 | 2026-10-31 22:50 | 2026-10-31 23:02 | 0.0 | 3.0 | green | CI, run 41250564660, attempt 1: docs: finish sonarcloud-report.md (#96) |
| 17 | `d61efce` | #97 | 2026-10-31 23:14 | 2026-10-31 23:24 | 0.1 | 2.1 | green | CI, run 41252283575, attempt 1: chore: ci budge and video08 Demchyna |
| 18 | `26f34b9` | #19 | 2026-09-27 23:59 | 2026-11-01 13:26 | 7.5 | 3.2 | green | CI, run 41310419010, attempt 1: docs: add my signature. Lead time 829 h: PR #19 was opened on 27.09 and merged on 01.11. |
| 19 | `16ac59c` | #98 | 2026-11-01 12:35 | 2026-11-01 19:29 | 9.8 | 3.3 | green | CI, run 41340915252, attempt 1: docs: improvements based on feedback. Queue 9.8 min: course runners were busy on Sunday evening. |
| 20 | `df81534` | #101 | 2026-11-02 07:24 | 2026-11-06 21:37 | 0.0 | 2.2 | green | CI, run 42297935896, attempt 1: docs: reliablity (#101) |
| 21 | `80ec284` | #104 | 2026-11-07 15:37 | 2026-11-07 19:13 | 0.0 | 4.7 | green | CI, run 42404735703, attempt 1: feat: transaction timeouts |

### Last red streak on `main` (17.10 – 19.10.2026, before the period)

There are no red runs on `main` in the period. The last incident is one week earlier; it is
used for the recovery time and the postmortem in the [DORA summary](dora-summary.md).

| № | Commit | PR | First PR commit (UTC) | Run completed (UTC) | Queue (min) | Execution (min) | Status | Notes |
|---|--------|----|-----------------------|---------------------|-------------|-----------------|--------|-------|
| 1 | `e616b98` | #53 | 2026-10-12 06:04 | 2026-10-17 22:10 | 0.0 | 0.0 | red (failure) | CI Test, run 39073988426, attempt 1: feat: ttd (#53). Cause: job not started, organization Actions billing (log annotation). |
| 2 | `0adb345` | #55 | 2026-10-12 06:21 | 2026-10-17 22:31 | 0.0 | 0.0 | red (failure) | CI Test, run 39075410869, attempt 1: test: page response (#55). Cause: job not started, organization Actions billing (log annotation). |
| 3 | `942592d` | #58 | 2026-10-12 06:21 | 2026-10-17 22:34 | 0.0 | 0.0 | red (failure) | CI Test, run 39075653504, attempt 1: test: auth (#58). Cause: job not started, organization Actions billing (log annotation). |
| 4 | `2934144` | #60 | 2026-10-12 06:21 | 2026-10-17 22:39 | 0.0 | 0.1 | red (failure) | CI Test, run 39075943742, attempt 1: feat: tdd hotel sorting (#60). Cause: job not started, organization Actions billing (log annotation). |
| 5 | `5102438` | #62 | 2026-10-12 06:21 | 2026-10-17 22:45 | 0.0 | 0.0 | red (failure) | CI Test, run 39076316521, attempt 1: docs: testing strategy (#62). Cause: job not started, organization Actions billing (log annotation). |
| 6 | `10b4d78` | #64 | 2026-10-12 06:21 | 2026-10-17 22:48 | 0.0 | 0.0 | red (failure) | CI Test, run 39076573107, attempt 1: feat: workflow test with coverage (#64). Cause: job not started, organization Actions billing (log annotation). |
| 7 | `3098f86` | #66 | 2026-10-17 23:03 | 2026-10-17 23:05 | 0.0 | 0.0 | red (failure) | CI Test, run 39077640554, attempt 1: chore: lab06 video Demchyna (#66). Cause: job not started, organization Actions billing (log annotation). |
| 8 | `ee40ff4` | #67 | 2026-10-18 05:24 | 2026-10-18 05:25 | 0.0 | 0.0 | red (failure) | CI Test, run 39100472620, attempt 1: test: find hotel by name ignoring letter case [RED]. Cause: job not started, organization Actions billing (log annotation). |
| 9 | `3098f86` | #66 | 2026-10-17 23:03 | 2026-10-18 05:32 | 0.0 | 0.0 | red (failure) | CI Test, run 39100907020, attempt 1: chore: lab06 video Demchyna (#66). Cause: job not started, organization Actions billing (log annotation). |
| 10 | `334d3af` | #47 | 2026-10-14 11:34 | 2026-10-18 21:07 | 0.0 | 0.0 | red (failure) | CI Test, run 39166411902, attempt 1: Merge pull request #47 from ukma-fin-ssdm-2026/docs/roles-and-task-wo…. Cause: job not started, organization Actions billing (log annotation). |
| 11 | `3c64a44` | #20 | 2026-09-28 00:52 | 2026-10-18 21:10 | 0.0 | 0.0 | red (failure) | CI Test, run 39166682214, attempt 1: Merge pull request #20 from ukma-fin-ssdm-2026/Roman19122006-patch-4. Cause: job not started, organization Actions billing (log annotation). |
| 12 | `55b1521` | #67 | 2026-10-18 05:24 | 2026-10-18 21:11 | 0.0 | 0.0 | red (failure) | CI Test, run 39166713544, attempt 1: Merge pull request #67 from ukma-fin-ssdm-2026/feat/tdd-hotel-name-ca…. Cause: job not started, organization Actions billing (log annotation). |
| 13 | `76870d7` | #21 | 2026-09-28 02:22 | 2026-10-18 22:42 | 0.0 | 0.0 | red (failure) | CI Test, run 39173391242, attempt 1: Merge pull request #21 from ukma-fin-ssdm-2026/docs/lab03-adrs-matrix. Cause: job not started, organization Actions billing (log annotation). |
| 14 | `b6168c1` | #76 | 2026-10-19 09:42 | 2026-10-19 13:47 | 0.0 | 0.0 | red (failure) | CI Test, run 39263202270, attempt 1: Merge pull request #76 from ukma-fin-ssdm-2026/chore/videos-hovoryshc…. Cause: job not started, organization Actions billing (log annotation). |
| 15 | `becd457` | #77 | 2026-10-19 17:15 | 2026-10-19 17:38 | 0.1 | 1.5 | green | CI Test, run 39295576763, attempt 1: Run CI jobs on the course's self-hosted runners. Fix: CI moved to the course self-hosted runners. |

#### AI usage note

The collection script and this page were drafted with an LLM (Claude Code). Verification:
the queue and execution times of runs 41310419010 and 42404735703 and the first commit of
PR #19 were checked by hand with `gh run view` and `gh pr view`; the values match to the
rounding (1 s).
