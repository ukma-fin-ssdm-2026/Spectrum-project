# Task workflow

Go back to the [Main README](../../README.md).

How the team plans, tracks and closes work. All tasks live in
[GitHub Issues](https://github.com/ukma-fin-ssdm-2026/Spectrum-project/issues); the rules for
branches and reviews are summarised in [Team charter](../../TeamCharter.md), section 4.

## 1. Lifecycle of a task

**open → assign → work → pull request → review → merge → close**

1. **Open.** Anyone can open an issue. The title says what has to be done
   ("Hotel management", "Static analysis"); the description lists what counts as done.
2. **Label.** Every issue gets a lab label and, where it fits, a type label (see section 2).
3. **Assign.** The person who takes the task assigns it to themselves. An issue without an
   assignee is free to take. One task has one assignee.
4. **Work.** The assignee creates a branch from `main` (see section 3) and commits there.
5. **Pull request.** The assignee opens a pull request to `main`. The description contains
   `Closes #<issue number>`, so the issue is closed automatically on merge.
6. **Review.** At least one other member reviews the pull request. All required checks
   must pass (section 4). Requested changes are fixed in the same branch.
7. **Merge and close.** After approval the assignee merges the pull request; the linked
   issue closes. The branch can then be deleted.

## 2. Labels

| Label                              | Used for                                                    |
|------------------------------------|-------------------------------------------------------------|
| `Lab04`, `Lab05`, …                | The lab the task belongs to; a new label is added per lab    |
| `documentation`                    | Changes to documents in `docs/`, README or the charter       |
| `enhancement`                      | New functionality                                            |
| `bug`                              | Something does not work as described                         |
| `question`                         | Something the team has to decide or clarify                  |
| `feedback`                         | Instructor feedback on a lab; closed when all remarks are addressed |
| `dependencies`, `github_actions`, `java` | Dependency updates opened by Dependabot                |

## 3. Branches and commits

- Branching strategy: GitHub Flow. `main` is always in a working state; every change goes
  through a short-lived branch and a pull request.
- Branch names describe the change, for example `feat/hotel-search`, `docs/lab03-threat-model`,
  `fix/deploy-workflow`.
- Commit messages follow Conventional Commits: `feat:`, `fix:`, `docs:`, `test:`, `chore:`.

## 4. Pull requests and protection of `main`

`main` is protected. A pull request can be merged only when:

- it has at least 1 approving review;
- the required checks pass: `build`, `spotless` (formatting), `spotbugs` (static analysis)
  and `generate-and-commit` (OpenAPI specification);
- the branch is up to date with `main`.

## 5. Instructor feedback

The instructor posts feedback for each lab as an issue with the `feedback` label. The team
splits the remarks among members at the weekly meeting, links the fixing pull requests to
the feedback issue and closes it when every remark is addressed.

### AI usage note:

This document was drafted with LLM from the team charter, the repository settings and the
existing issues. Verification: reviewed in the pull request before merging.
