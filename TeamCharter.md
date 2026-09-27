# Team charter

Go back to the [Main README](README.md).

## 1. Main info

- Team name: Spectrum team
- Members:
    - Данило (GitHub: @Demon4ort, ukma email: d.demchyna@ukma.edu.ua)
    - Вероніка (GitHub: @VeronikaGovorischeva, ukma email: vhovoryshcheva@ukma.edu.ua)
    - Павло (GitHub: @FalsePromisee, ukma email: p.halkovskyi@ukma.edu.ua)
    - Роман (GitHub: @Roman19122006, ukma email: r.butko@ukma.edu.ua)
- Requirements documentation: [Requirements](docs/requirements/requirements.md)

## 2. Roles and responsibilities

Lab 1 (team setup):

- Repo Maintainer: @VeronikaGovorischeva
- CI Maintainer: @Demon4ort
- Documentation Lead (README, team charter): @Roman19122006
- Issue Tracker Lead: @FalsePromisee
- Project Brief Lead: @Roman19122006

Lab 2 (requirements):

- Requirements Lead: @Demon4ort
- FR Lead: @Demon4ort
- Quality Lead: @Demon4ort
- Traceability Lead: @VeronikaGovorischeva
- Documentation Lead (requirements artifacts): @VeronikaGovorischeva

## 3. Communication plan

- Main channel: telegram chat
- Meeting schedule: every week at Thursday/Friday evening
- Answer expectation: not later than 24 hours

## 4. Cooperation and working process

- Branching strategy: GitHub Flow
- Commit practice: conventional commits
- Code review rules: all commits require a pull request, successful CI runs and at least 1
  approval from a member who did not write the change (see section 8)
- Task working process: open → assign → work → pr → close, described in
  [Task workflow](docs/process/task-workflow.md)
- Requirements review: reviewing pull requests or issue in repo
- NFR checks: benchmarks and their results with explanations and instructions how to
  reproduce
- Loom videos: member will show what commits, comments on review or issues he made
- Requirements artifacts: all requirements documents are stored in `docs/requirements/`
- README maintenance: a pull request that adds, renames or moves an artifact also updates
  the links in `README.md`
- Contribution evidence: each member commits the link to their Loom video as
  `LabNN/video_<name>.txt` before the deadline; the team checks at the weekly meeting that
  every member's link is present and matches their commits and reviews

## 5. Conflict resolution

- Conflict resolution plan: discussion, then escalation if unresolved

## 6. Availability and workload

- Members availability:
    - Danylo: 6 hours per week

## 7. Etics and professional behavior

- Core principles:
    - Academic decency
    - Academic Integrity
    - Intellectual Honesty
    - Respect for colleagues

## 8. Code quality and review policy

### 8.1 Coding standard

- Java code follows the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html),
  applied by Spotless with `google-java-format` (`GOOGLE` style). Every Java file starts with
  the header `/* (C)<year> */`.
- Format before committing: `./mvnw spotless:apply`; check: `./mvnw spotless:check`.
- Names: classes in `PascalCase`, methods and variables in `camelCase`, constants in
  `UPPER_SNAKE_CASE`; test methods describe the behaviour they check
  (`saveThrowsAndDoesNotAddHotelWhenNameAlreadyExists`).
- Layers: HTTP handling and validation stay in the `api` package, business rules in the
  `logic` package.

### 8.2 Mandatory tools

| Tool                             | Checks                              | Runs in                     |
|----------------------------------|-------------------------------------|-----------------------------|
| Spotless (google-java-format)    | Formatting                          | pre-commit, pull requests   |
| SpotBugs + Find Security Bugs    | Bugs and security issues (`Medium`) | pre-commit, pull requests   |
| gitleaks                         | Secrets in commits                  | pre-commit                  |
| JUnit 5 + JaCoCo                 | Tests, line coverage ≥ 70%          | `mvnw verify`, "CI Test"    |

- Every developer installs pre-commit and uses it.
- Any problem raised by SpotBugs or gitleaks is fixed, or suppressed with a written
  justification next to the suppression.

### 8.3 Code review steps

1. The author opens a pull request to `main`, linked to its issue, and checks the
   Definition of Done (8.4) first.
2. At least **one approval is mandatory**, from a member who did not write the change.
3. The reviewer checks style and standards, readability and structure, security (input
   validation, authentication, logging of sensitive data), the boundary between business
   logic and framework, and documentation.
4. The author answers every comment, by a fix or an explanation, before merging.
5. The pull request is merged only after the approval and successful required checks.

### 8.4 Definition of Done

A change is done when:

- `./mvnw spotless:check` and `./mvnw spotbugs:check` pass;
- new or changed logic has unit tests, and `./mvnw verify` passes, including the coverage
  minimum;
- documentation is updated: README links, `docs/` documents, and the OpenAPI specification
  for API changes;
- an "AI usage" note is added where AI was used;
- the pull request is approved by another member.

### 8.5 Responsibilities

- Quality Lead (@Demon4ort): maintains the tool configuration and this policy, and
  coordinates code reviews.
- CI Maintainer (@Demon4ort): keeps the workflows running.
- Every member reviews pull requests of others.

### 8.6 Other rules

- If a developer finds a problem in already merged code, they create an issue describing
  the problem and assign it to the author of that part.
- Any breaking change to the API after the frontend exists must be released as a new API
  version.

*AI usage: section 8 was restructured with LLM from the team's existing rules, the
project configuration and the Lab 5 requirements; reviewed in the pull request before
merging.*

## 9. AI usage

- Instruments: Any kind of available llm(OpenAI, Anthropic, Deepseek, Google) or
  harnesses(codex, claude code, hermess, openclaw, omp)
- Disclosure rules: Any kind of llm usage for content creation should be disclosed in
  short note inside the artifact the content is placed
- Verification rules: generated code should be treated the same as the one written by a
  person - all same responsibilities and rules apply

## 10. Signatures

- [x] Данило (GitHub: @Demon4ort)
- [x] Вероніка (GitHub: @VeronikaGovorischeva)
- [ ] Павло (GitHub: @FalsePromisee)
- [x] Роман (GitHub: @Roman19122006)