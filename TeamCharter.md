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
- Code review rules: all commits require pull request, successful ci run and at least 1
  approve(ideally)
- Task working process: open → assign → work → pr → close, described in
  [Task workflow](docs/process/task-workflow.md)
- Requirements review: reviewing pull requests or issue in repo
- NFR checks: benchmarks and their results with explanations and instructions how to
  reproduce
- Loom videos: member will show what commits, comments on review or issues he made
- Requirements artifacts: all requirements documents are stored in `docs/requirements/`
- README maintenance: a pull request that adds, renames or moves an artifact also updates
  the
  links in `README.md`
- Contribution evidence: each member commits the link to their Loom video as
  `LabNN/video_<name>.txt` before the deadline; the team checks at the weekly meeting that
  every
  member's link is present and matches their commits and reviews

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

- Every developer should install pre-commit and use it
- Therefore, any problem raised by spotbugs or gitleaks should be fixed or suppressed with
  sufficient justification
- Any pull request must not be merged without successful workflows runs
- Pull request should have at least one review ideally
- If developer finds a problem in already merged code - he should create an issue
  describing the problem and assign it to the author of problematic part
- Any breaking changes to API after creation of frontend part must create new version of
  API

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
- [ ] Роман (GitHub: @Roman19122006)
