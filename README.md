# Spectrum-project

[![CI Test](https://github.com/ukma-fin-ssdm-2026/Spectrum-project/actions/workflows/github-actions-test.yml/badge.svg?branch=main)](https://github.com/ukma-fin-ssdm-2026/Spectrum-project/actions/workflows/github-actions-test.yml)


### Team charter document:
[Team charter](TeamCharter.md)

## Requirements artefacts:
- [Requirements](docs/requirements/requirements.md)
- [User stories](docs/requirements/user-stories.md)

## Architecture:
- [High-Level Design](docs/architecture/high-level-design.md)
- [Data model](docs/architecture/data-model.md)
- [Threat model (STRIDE)](docs/architecture/threat-model.md)

### Diagrams:
- [Deployment diagram](docs/architecture/uml/deployment_diagram.puml)
- [Component diagram](docs/architecture/uml/component_diagram.puml)
- [Sequence diagram](docs/architecture/uml/sequence_diagram.puml)


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
