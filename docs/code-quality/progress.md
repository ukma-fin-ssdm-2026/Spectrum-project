# Implementation progress

Go back to the [Main README](../../README.md).

State of the code in [`spectrum-app/`](../../spectrum-app) on `main`. The application is a
Quarkus (Java 21) backend that follows the [High-Level Design](../architecture/high-level-design.md):
for now all services run inside one application, with data kept in in-memory stubs instead of
a database.

## 1. Code structure

| Layer   | Package                                    | Responsibility                                                        |
|---------|--------------------------------------------|-----------------------------------------------------------------------|
| API     | `edu.ukma.ssdm.teamspectrum.api.auth`      | Register and login endpoints, request validation, auth cookie         |
| API     | `edu.ukma.ssdm.teamspectrum.api.hotel`     | Hotel endpoints, request/response models, mapping to the logic layer  |
| API     | `edu.ukma.ssdm.teamspectrum.api.common`    | Pagination, sorting, validation error responses                       |
| Logic   | `edu.ukma.ssdm.teamspectrum.logic.auth`    | Users, password hashing, JWT creation                                 |
| Logic   | `edu.ukma.ssdm.teamspectrum.logic.hotel`   | Hotel creation rules, search with filters, sorting and pages          |

The API layer only validates input and maps data; business rules live in the logic layer.
Repositories are interfaces (`UserRepository`, `HotelRepository`) with in-memory stub
implementations, so a real database can replace them without changing the services.

## 2. What works

| Component       | Endpoints                                         | What is implemented                                                                 |
|-----------------|---------------------------------------------------|-------------------------------------------------------------------------------------|
| Authentication  | `POST /v1/auth/register`, `POST /v1/auth/login`   | Email and password validation, bcrypt password hashing, JWT (HS256, 15 minutes) in an `HttpOnly`, `Secure`, `SameSite=Lax` cookie; `409` for an existing email, `401` for wrong credentials |
| Hotels          | `POST /v1/api/hotels`, `GET /v1/api/hotels`, `GET /v1/api/hotels/{hotelId}` | Hotel creation with a unique name (case and surrounding spaces ignored); search by location, price, rating and amenities; sorting; pagination; `404` for an unknown hotel |
| Validation      | all endpoints                                     | Bean Validation on bodies and query parameters, `400` with a list of invalid fields |
| Documentation   | Swagger UI, GitHub Pages                          | OpenAPI specification generated from the code ([API design](../api/api-design.md)) |

## 3. Quality checks

| Check                 | Tool                                   | Where it runs                       |
|-----------------------|----------------------------------------|-------------------------------------|
| Formatting            | Spotless (Google Java Format)          | pre-commit and pull requests        |
| Static analysis       | SpotBugs with Find Security Bugs       | pre-commit and pull requests ([results](static-analysis.md)) |
| Secret scanning       | gitleaks                               | pre-commit                          |
| Unit tests, coverage  | JUnit 5, Mockito, REST Assured, JaCoCo | `mvnw verify` in the "CI Test" workflow |

There are 68 tests in 13 test classes. The committed
[coverage report](../testing/coverage/index.html) shows 71% line and 20% branch coverage;
see the [testing strategy](../testing/testing-strategy.md) for what is not covered and why.

## 4. In progress and next

| Status      | Item                                                                 | Requirements          |
|-------------|----------------------------------------------------------------------|-----------------------|
| In progress | Room endpoints (models `Room`, `CreateRoom` exist)                    | FR-001 – FR-005, FR-012 |
| In progress | Authorization on hotel and room management (only workers may change data) | SEC-003           |
| Next        | Bookings with the overlap check                                      | FR-013, REL-001       |
| Next        | Booking and cancellation notifications                              | FR-015, FR-016        |
| Next        | PostgreSQL instead of the in-memory stubs                            | ADR-002               |
| Later       | Worker permission management, hotel ratings                          | FR-018, FR-014        |

### AI usage note:

This document was drafted with LLM from the source code, the test and coverage reports and
the requirements. Verification: reviewed in the pull request before merging.
