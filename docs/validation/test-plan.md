## Test plan

Go back to the [Main README](../../README.md).

### Volume

- **Tested:** Auth Service (register, login, password storage) and the Hotels API
  (`GET /v1/api/hotels`, `GET /v1/api/hotels/{id}`, `POST /v1/api/hotels`), as it is the only
  functionality currently implemented.
- **Not tested yet:** rooms, bookings, notifications and `PermissionGrant` management. Their
  rows are in [Planned](#planned-code-not-implemented-yet) and become executable when the code
  is merged.
- **Not tested:** the frontend and third-party code (Quarkus, jOOQ, Flyway).

### Exit criteria

- all rows in [Executable now](#executable-now) pass in CI: the `ci.yml` job runs
  `./mvnw -B spotless:check verify spotbugs:check` on every push and pull request to `main`
- line coverage stays at **70%** or more: the JaCoCo `check` rule in `pom.xml` fails the build
  below this value
- no open defect on a Must requirement or a security NFR (SEC-002, SEC-003)
- integration tests are rerun at least 3 times before a release to catch unstable tests

### Risks

- Auth Service - as a main gateway to system should work impeccable, because any
  instability would prevent any usage of our system (TP-1 – TP-4, TP-13, TP-14)
- Unauthorized changes - `POST /v1/api/hotels` is open to anyone now, and later
  `PermissionGrant` management may result in devastating security breach with user data
  corrupted (TP-18, TP-5 – TP-9)
- Search returning wrong results - search, filters and sorting are the first thing a client
  uses, and they now run against a real database (TP-10, TP-11, TP-15, TP-16, TP-19)

### How tests run in CI

Integration rows are `@QuarkusTest` classes named `*Test`. Maven Surefire runs them in the
`test` phase of `mvnw verify`, against the PostgreSQL service container of the `ci.yml` job.
`skipITs=true` in `pom.xml` skips only Failsafe `*IT` classes, and the project has none, so
it does not switch off the integration tests.

### Executable now

| №     | Component/function                | Test level  | Type (positive/negative) | Expected result / acceptance criteria                                                                                                          | Owner                  | Requirement             |
|-------|-----------------------------------|-------------|--------------------------|------------------------------------------------------------------------------------------------------------------------------------------------|------------------------|-------------------------|
| TP-1  | Auth Service/register             | Integration | Positive                 | `POST /v1/auth/register` with a valid email and a 12–72 character password returns `201` and sets an `HttpOnly` auth cookie                     | @Demon4ort             | FR-019                  |
| TP-2  | Auth Service/register             | Integration | Negative                 | Malformed email, blank or too short password returns `400` with field errors; no user is saved                                                 | @Demon4ort             | FR-020                  |
| TP-13 | Auth Service/register             | Integration | Negative                 | Registering an email that already exists returns `409`; the existing user is not changed                                                       | @Demon4ort             | FR-020                  |
| TP-3  | Auth Service/login                | Integration | Positive                 | `POST /v1/auth/login` with valid credentials returns `200` and sets an auth cookie with a JWT that expires in 15 min                            | @Demon4ort             | FR-021                  |
| TP-4  | Auth Service/login                | Integration | Negative                 | Unknown email and wrong password both return `401` with the same body, so the response does not reveal which emails exist                     | @Demon4ort             | FR-022                  |
| TP-14 | Auth Service/password storage     | Unit        | Positive                 | The stored `passwordHash` is not equal to the password and verifies with bcrypt; the password does not appear in log output                   | @Demon4ort             | SEC-002                 |
| TP-10 | Hotels Service/hotel search       | Integration | Positive                 | Search by city or district returns `200` and only hotels in that location, case-insensitive                                                   | @Demon4ort             | FR-008                  |
| TP-11 | Hotels Service/hotel filters      | Integration | Positive                 | Filters by price range, rating range and amenities are combined with AND: only hotels matching every filter and having all amenities return | @Demon4ort             | FR-009, FR-010, FR-011  |
| TP-15 | Hotels Service/search parameters  | Unit        | Negative                 | Past check-in date, negative price, rating outside 0–5, or too many amenities is rejected by validation, and the endpoint returns `400`        | @Demon4ort             | FR-008, FR-009, FR-010  |
| TP-16 | Hotels Service/sorting            | Integration | Positive                 | Sorting by `PricePerNight` or `Rating`, `Asc` or `Desc`, orders by the requested field, checked on hotels whose price order and rating order differ | @VeronikaGovorischeva  | FR-009, FR-010 (US-004) |
| TP-17 | Hotels Service/hotel page         | Integration | Positive, Negative       | `GET /v1/api/hotels/{id}` returns `200` with name, address, price, rating and amenities for an existing hotel and `404` for an unknown id   | @VeronikaGovorischeva  | FR-012 (US-003)         |
| TP-18 | Hotels Service/create hotel       | Integration | Negative                 | `POST /v1/api/hotels` without an auth cookie returns `401`, and without the worker or admin role returns `403`; no hotel is saved            | @VeronikaGovorischeva  | SEC-003                 |
| TP-19 | Hotels Service/search performance | System      | Positive (NFR)           | 95th percentile response time of `GET /v1/api/hotels?location=Kyiv` is **≤ 2 s** with **20 concurrent users** for 1 min (k6), 0% errors     | @Demon4ort             | PERF-001                |

**Defects found by the plan.** All three are fixed in PR #87, where the tests were committed
red before the fix:

- TP-18: `POST /v1/api/hotels` had no authentication check. A request without a token reached
  the service.
- TP-18: no JWT issued by the application could be verified, so every protected endpoint would
  have returned `401`. SmallRye JWT reads `smallrye.jwt.verify.secretkey` only as a JWK, and in
  dev and test mode Quarkus replaced it with a generated RSA key.
- TP-16: `HotelRepositoryImpl.sortField` (PR #84) sorted `Rating` by price and `PricePerNight`
  by rating. The old sorting test did not catch it, because in its data the cheaper hotel also
  had the lower rating (see the [review log](review-log.md)).

**TP-19** needs a running application with test data, so it runs manually before a release,
not in every CI run. The result (p95 time and error rate) is recorded in the pull request.

### Planned (code not implemented yet)

| №     | Component/function                      | Test level  | Type (positive/negative) | Expected result / acceptance criteria                                                                        | Owner      | Requirement     |
|-------|-----------------------------------------|-------------|--------------------------|--------------------------------------------------------------------------------------------------------------|------------|-----------------|
| TP-5  | Auth Service/PermissionGrant management | Unit        | Positive                 | Worker with a valid `PermissionGrant` for the hotel can create a room                                        | @Demon4ort | FR-001, SEC-003 |
| TP-6  | Auth Service/PermissionGrant management | Unit        | Negative                 | Worker with no grant, or a grant for another hotel, is denied                                               | @Demon4ort | SEC-003         |
| TP-7  | Auth Service/PermissionGrant management | Integration | Positive                 | Room creation endpoint returns `201` for a worker with a valid `PermissionGrant`                             | @Demon4ort | FR-001, SEC-003 |
| TP-9  | Auth Service/PermissionGrant management | Integration | Negative                 | Every room-management endpoint returns `403` to a worker without the required grant (100% of endpoints)     | @Demon4ort | SEC-003         |
| TP-8  | Auth Service/PermissionGrant management | System      | Positive                 | Worker can create a room in hotel after receiving valid `PermissionGrant` by Administrator                   | @Demon4ort | FR-001, SEC-003 |
| TP-12 | Hotels and Rooms Service/room view      | System      | Positive                 | After selecting a hotel, the client sees only rooms free for the selected dates                              | @Demon4ort | FR-012          |
| TP-20 | Hotels and Rooms Service/room view      | Integration | Negative                 | Room list for an unknown hotel returns `404`; check-out before check-in returns `400`                        | @Demon4ort | FR-012          |

### AI usage note

The changes after the peer review (the split into executable and planned rows, TP-13 – TP-20,
status codes, the CI section and the known defects) were drafted with an LLM (Claude) from the
review comments, the code and `ci.yml`. Verification: every test name and status code was
checked against the code; reviewed in the pull request before merging.

### Бонуси (Bonuses)

#### Tests as a specification for an AI agent

- **Function:** authorization and conflict handling of `POST /v1/api/hotels`, and the JWT
  verification it needs (TP-17, TP-18), in PR #87.
- **Order of commits:** the test commit `test: hotel controller lookup, create authorization and
  name conflict (TP-17, TP-18)` comes before the generated code commit `feat: require worker or
  admin role to create a hotel and return 409 for duplicate names`. When the tests were
  committed, 4 of 6 were red.
- **AI checklist:** the generated code was checked with the same 5-point checklist as PR #84
  (imports used, error handling, logic against TP-18, no dead code); the key decisions are
  explained in the PR description, and reviewer questions are answered in PR #87.
- **AI usage:** the implementation was generated with an LLM (Claude Code) until the tests
  passed, in 3 iterations: (1) `@RolesAllowed` and the `409` mapper: valid tokens still got
  `401`; (2) `smallrye.jwt.sign.key` to stop Quarkus from generating a dev key pair: the issuer
  was fixed, but the key was still unresolvable; (3) a `JWTAuthContextInfo` producer with the
  HS256 key: all tests green. Fixed manually: the root cause was confirmed in the SmallRye
  debug log; the test helper was changed to issue tokens with `TokenFactoryImpl`, so it also
  checks login tokens.

#### Executable NFR test (security)

- **NFR row:** TP-18, SEC-003 (only authorized staff may change data).
- **Threshold:** 100% of protected endpoints return `401` without a token and `403` without the
  required role.
- **Measured:** 1 of 1 protected endpoint (`POST /v1/api/hotels`): `401` without a token, `403`
  for `CLIENT`, `201` for `WORKER`, and the service is not called on `401` and `403`
  (`HotelControllerTest`).
- **Run command:** `./mvnw test -Dtest=HotelControllerTest` in `spectrum-app` (runs in CI as part
  of `./mvnw -B spotless:check verify spotbugs:check`).
