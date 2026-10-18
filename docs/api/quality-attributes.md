# API quality attributes

Go back to the [Main README](../../README.md).

Every target below has three parts: a number, a way to measure it, and a condition under
which the target counts as failed. Targets follow the non-functional requirements in
[Requirements](../requirements/requirements.md).

## Performance

- Target: 95th percentile response time of `GET /v1/hotels` ≤ 2 s with 20 concurrent users
  (PERF-001)
- Failure condition: p95 > 2 s in a 5-minute load test run
- Implementation:
    - Paginated responses, at most 100 items per request
    - Cache for repeated search queries (planned)
- Measurement: load test with k6, 20 virtual users, 5 minutes

## Security

- Target: 100% of invalid requests are answered with `400`; 0 responses with `5xx` or
  `2xx` on the set of invalid requests
- Failure condition: at least one `5xx` or `2xx` response to an invalid request, or an error
  body that contains a stack trace or class name
- Implementation:
    - Bean Validation on every request body and query parameter
    - One error structure without internal details
    - Authentication with JWT and permission checks returning `403` (planned, SEC-001,
      SEC-003)
- Measurement: automated tests that send a fixed set of invalid requests to every endpoint

## Reliability

- Target: 0 double bookings when 100 concurrent requests book the same room for the same
  dates: exactly one request gets `201`, the others get `409` (REL-001)
- Failure condition: more than one `201` response in the concurrency test
- Implementation:
    - Database constraint on overlapping bookings (planned together with the booking
      endpoints)
    - No unhandled exceptions: validation errors are mapped to `400`
- Measurement: concurrency integration test with 100 parallel requests

## Maintainability

- Target: the published specification always matches the code: 0 differences between
  `docs/api/openapi.yaml` on `main` and the specification generated from the code on `main`
- Failure condition: a build on `main` generates a specification that differs from the
  committed `docs/api/openapi.yaml`
- Implementation:
    - Code-First: the specification is generated during the build
    - Workflow that regenerates and commits the specification on every pull request
    - Formatting check (Spotless) on every pull request
- Measurement: the `generate-and-commit` and `spotless` checks required for merging

## Trade-off analysis

| Attribute                         | Trade-off                                    | Impact                                 | Mitigation                                         |
|-----------------------------------|----------------------------------------------|----------------------------------------|----------------------------------------------------|
| Security (validation)             | Extra processing on every request            | Small added latency                    | Keep constraints simple; measure in the load test  |
| Performance (pagination)          | Limited amount of data per request           | Clients need several requests          | `limit` up to 100; cursor pagination if lists grow |
| Performance (cache, planned)      | Cached search results can be stale           | A just-booked room may still be listed | Short TTL; availability re-checked on booking      |
| Reliability (database constraint) | Booking waits for a transactional write      | Slower booking under contention        | Constraint only on the booking table               |
| Security (detailed errors)        | Field-level messages reveal validation rules | Minor information disclosure           | No internal details in messages                    |

### AI usage note:

This document was drafted with LLM from the non-functional requirements and the API source
code. Verification: reviewed in the pull request before merging.
