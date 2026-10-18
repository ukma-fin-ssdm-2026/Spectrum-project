## Testing strategy

Go back to the [Main README](../../README.md).

### Current state

At the moment, project is only tested with unit tests. As there are neither database nor
cache, it is hard to justify adding anything else ___currently___.

### Coverage summary

Project has 63% test coverage. Critical places that must be tested as soon
as possible are:

- [HotelController](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/hotel/HotelController.java) -
  to be able to make changes to corresponding service and check correctness with
  regression
- [HotelSortBy](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/hotel/HotelSortBy.java) -
  to avoid introducing bugs as this realization isn't typesafe
- [SortOrder](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/common/SortOrder.java) -
  to avoid introducing bugs as this realization isn't typesafe

Rest don't need to be covered at the moment:

- classes which still aren't in use (
  e.g. [Room](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/hotel/Room.java) )
- classes added for
  libraries ( [HotelPageResponse](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/hotel/HotelPageResponse.java)
  added for correct openapi generation)
- unused methods ( e.g. `hashCode`
  in [ValidationError](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/common/error/ValidationError.java) )
- stubs which will be replaced with real implementation (
  e.g. [UserRepositoryStub](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/auth/UserRepositoryStub.java) )

### Plans

- **Unit:** All code in service layer should be covered, as well as request validation of
  API layer.
- **Integration:** HTTP-level tests for every endpoint, checking status codes, response
  bodies, and error handling. Database integration tests when persistence is introduced.

- **System and acceptance:** For each user story, acceptance scenarios
  based on its criteria and to be linked to the test cases in the
  [requirements traceability matrix](../requirements/rtm.md).

- **Quality attributes:** When the relevant endpoints and infrastructure are implemented,
  the k6 search load test (20 virtual users for 5 minutes, p95 at or below 2 s) should be
  run,
  a 100-request concurrent booking test, and automated invalid-request and access-control
  checks. Described in [API quality attributes](../api/quality-attributes.md).
  Property-based tests for sorting, filtering, and pagination;

### CI/CD

All Unit and Integration should be run in CI/CD for all changes. \
System and acceptance should be rerun in case of changes to corresponding module. \
*NB*: Integrations can be turned off for each changes in case of runs being too long (
e.g. >= 15 minutes)

### Bonuses

#### Coverage gate in CI

- **What:** the JaCoCo `check` goal fails the build when line coverage of the whole
  application (`BUNDLE`) is below **70%** (`spectrum-app/pom.xml`, `jacoco-maven-plugin`,
  rule `LINE` / `COVEREDRATIO` / `0.70`).
- **Where it runs:** the "CI Test" workflow (`.github/workflows/github-actions-test.yml`) runs
  `./mvnw -B verify` on every push and on every pull request to `main`, so a pull request
  that lowers coverage below the threshold fails.
- **Why 70%:** the committed report shows 71% line coverage. The threshold is set just below
  the current value, so coverage cannot drop unnoticed, but it does not force tests for
  code that is better covered at other levels: the controllers and the in-memory
  repository stubs are planned for integration tests, and records and enums have no logic
  to test. The threshold is raised as integration tests are added.

#### AI usage

**Plans** section was drafted with LLM. The **Bonuses** section was drafted with LLM from
`pom.xml` and the workflow configuration; reviewed in the pull request before merging.