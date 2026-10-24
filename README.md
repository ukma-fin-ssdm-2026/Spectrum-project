# Spectrum-project

[![CI Test](https://github.com/ukma-fin-ssdm-2026/Spectrum-project/actions/workflows/github-actions-test.yml/badge.svg?branch=main)](https://github.com/ukma-fin-ssdm-2026/Spectrum-project/actions/workflows/github-actions-test.yml)

## Team

**Spectrum team**

| Member | GitHub | Role(s) |
|---|---|---|
| Данило | [@Demon4ort](https://github.com/Demon4ort) | CI Maintainer, Requirements Lead, FR Lead, Quality Lead |
| Вероніка | [@VeronikaGovorischeva](https://github.com/VeronikaGovorischeva) | Repo Maintainer, Traceability Lead, Documentation Lead (requirements artifacts) |
| Павло | [@FalsePromisee](https://github.com/FalsePromisee) | Issue Tracker Lead |
| Роман | [@Roman19122006](https://github.com/Roman19122006) | Documentation Lead (README, team charter), Project Brief Lead |

Roles are described in the [Team charter](TeamCharter.md); the task process is described in
[Task workflow](docs/process/task-workflow.md).

[Project Description](./Project-Description.md)

### Team charter document:

[Team charter](TeamCharter.md)

## Requirements artefacts:

- [Requirements](docs/requirements/requirements.md)
- [User stories](docs/requirements/user-stories.md)
- [Requirements traceability matrix](docs/requirements/rtm.md)

## Architecture:

- [High-Level Design](docs/architecture/high-level-design.md)
- [Data model](docs/architecture/data-model.md)
- [Threat model (STRIDE)](docs/architecture/threat-model.md)
- [Traceability matrix](docs/architecture/traceability-matrix.md)
- Architecture decisions: [ADR-001](docs/architecture/decisions/ADR-001.md) (architectural
  style), [ADR-002](docs/architecture/decisions/ADR-002.md) (data storage)

### Diagrams:

- [Deployment diagram](docs/architecture/uml/deployment_diagram.puml)
- [Component diagram](docs/architecture/uml/component_diagram.puml)
- [Sequence diagram](docs/architecture/uml/sequence_diagram.puml)
- [Data model diagram](docs/architecture/uml/data_model.puml)

### API:

- [API design](docs/api/api-design.md)
- [API quality attributes](docs/api/quality-attributes.md)
- [OpenAPI specification](docs/api/openapi.yaml) (generated from the code)
- [API documentation on GitHub Pages](https://ukma-fin-ssdm-2026.github.io/Spectrum-project/)
  (Swagger UI)
- API source code: [`spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api`](spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api)
- Swagger UI when running locally: `http://localhost:8080/api-docs`

### Testing:

- [Test coverage report](docs/testing/coverage/index.html)
- [Debugging log](docs/testing/debugging-log.md)

### Code quality:

- [Implementation progress](docs/code-quality/progress.md)
- [Static analysis](docs/code-quality/static-analysis.md)
- [Code review report](docs/code-quality/review-report.md)

### How to run locally:

#### Prerequisites:

- **JDK 21+** (check with `java -version`)

**Linux / macOS**

```shell
cd spectrum-app
./mvnw quarkus:dev
```

> If you get `Permission denied`, make the wrapper executable first: `chmod +x mvnw`

**Windows (Command Prompt)**

```bat
cd spectrum-app
mvnw quarkus:dev
```

**Windows (PowerShell)**

```powershell
cd spectrum-app
.\mvnw quarkus:dev
```

### Code Formatting

The project uses [Spotless](https://github.com/diffplug/spotless/tree/main/plugin-maven)
to keep the code consistently formatted. Run it before committing.

| Goal                               | Linux / macOS           | Windows (CMD)             | Windows (PowerShell)    |
|------------------------------------|-------------------------|---------------------------|-------------------------|
| Format the code (fix in place)     | `./mvnw spotless:apply` | `mvnw.cmd spotless:apply` | `.\mvnw spotless:apply` |
| Check formatting only (no changes) | `./mvnw spotless:check` | `mvnw.cmd spotless:check` | `.\mvnw spotless:check` |

### Static analysis

The project uses [SpotBugs](https://spotbugs.github.io/)
with [Find Security Bugs](https://find-sec-bugs.github.io/).

| Linux / macOS           | Windows (CMD)             | Windows (PowerShell)    |
|-------------------------|---------------------------|-------------------------|
| `./mvnw spotbugs:check` | `mvnw.cmd spotbugs:check` | `.\mvnw spotbugs:check` |

### Pre-commit

Pre-commit runs Spotless, SpotBugs, and Gitleaks before each commit. Install Python 3, then install the `pre-commit` package and Git hook once after cloning the repository.

**Linux / macOS**

```shell
python3 -m pip install --user pre-commit
python3 -m pre_commit install
```

**Windows (PowerShell)**

```powershell
py -m pip install --user pre-commit
py -m pre_commit install
```

Run all hooks against every tracked file (for example, after installing or updating the hooks):

**Linux / macOS**

```shell
python3 -m pre_commit run --all-files
```

**Windows (PowerShell)**

```powershell
py -m pre_commit run --all-files
```

After `pre-commit install` has been run in a clone, the hooks run automatically on each `git commit`.