## Review log

Go back to the [Main README](../../README.md).

Records the peer review of our [test plan](test-plan.md), the changes made after it, and the
AI-generated code review of one team pull request.

### Peer review of the test plan

- **Reviewer team:** Monkey Business (reviewer: @MykolaVashchenko)
- **Reviewed:** PR #78 `docs: initial test plan`, review posted on 24.10.2026 as a
  [comment in the PR](https://github.com/ukma-fin-ssdm-2026/Spectrum-project/pull/78#issuecomment-6386967804)

#### Checklist result

| № | Criterion                                     | Answer | Comments                                                                              |
|---|-----------------------------------------------|--------|---------------------------------------------------------------------------------------|
| 1 | Key requirements and critical paths covered   | No     | Nit: `POST /hotels` (no auth), duplicate email `409` and SEC-002 hashing are missing   |
| 2 | Acceptance criteria clearly stated            | No     | Blocking: no NFR case with a threshold. Nit: expected results are generic, no status codes |
| 3 | Negative scenarios tested                     | No     | Nit: TP-10 – TP-12 have only positive cases                                           |
| 4 | Traceability to Lab 2 requirements            | Yes    | Nit: TP-11 is mapped to FR-009/010/011 but describes date and location (FR-008)       |
| 5 | Plan is realistic and runnable in CI          | No     | Blocking: TP-5 – TP-9 and TP-12 test code that does not exist yet. Blocking: `skipITs=true` in `pom.xml` |

#### Strengths

1. Every row has a valid requirement ID, and TP-1 – TP-12 are linked back in `rtm.md`.
2. Risks and exit criteria are concrete and tied to the rows.
3. `testing-strategy.md` clearly says what is not worth testing and why.

#### Areas for improvement

1. Split tests into "executable now" and "planned", and start with `HotelController`
   integration tests (0% covered now).
2. Add NFR cases with thresholds and align the exit criteria with CI (the plan said 60%
   coverage, `pom.xml` enforces 70%).

### Changes made to the plan after review

| Review comment                                   | Type     | Change in [test-plan.md](test-plan.md) and [rtm.md](../requirements/rtm.md)                                                                                                   |
|--------------------------------------------------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| No NFR case with a threshold                     | Blocking | Added TP-19 (PERF-001: p95 ≤ 2 s with 20 concurrent users, k6) and TP-14 (SEC-002: stored hash is not the password and verifies with bcrypt; the password is not logged)     |
| TP-5 – TP-9, TP-12 test code that does not exist | Blocking | The table is split into **Executable now** and **Planned (code not implemented yet)**; TP-5 – TP-9 and TP-12 moved to Planned. Volume now says what is not tested yet       |
| `skipITs=true`, integration tests not run in CI  | Blocking | Checked the build: integration tests are `@QuarkusTest` classes named `*Test`, run by Surefire in `mvnw verify` against the PostgreSQL service of `ci.yml`. `skipITs` only skips Failsafe `*IT` classes, and there are none. Explained in the new section **How tests run in CI** |
| `POST /hotels`, `409`, SEC-002 missing           | Nit      | Added TP-18 (`POST /v1/api/hotels` without auth → `401`/`403`, marked as a known defect), TP-13 (duplicate email → `409`) and TP-14 (SEC-002)                              |
| Expected results generic, no status codes        | Nit      | Every executable row now states the endpoint and the status code or the exact condition                                                                                    |
| TP-10 – TP-12 only positive                      | Nit      | Added negative rows TP-15 (invalid search parameters → `400`), TP-17 (unknown hotel → `404`) and TP-20 (planned, unknown hotel or wrong dates for rooms)                  |
| TP-11 describes date and location                | Nit      | TP-11 now describes the price, rating and amenities filters it is mapped to; date and location stay in TP-10 (FR-008)                                                      |
| Start with `HotelController` integration tests   | Suggestion | Added TP-16, TP-17 and TP-18 for `HotelController` (owner @VeronikaGovorischeva)                                                                                         |
| Exit criteria 60% vs 70% in `pom.xml`            | Suggestion | Exit criteria now use 70% and name the CI command and the JaCoCo rule that enforce it                                                                                     |

The changes also updated `rtm.md`: new TP IDs are added to US-001 – US-005, US-013 and US-014,
and the Gaps section records that `POST /v1/api/hotels` has no story or FR yet.

### AI-generated code review

- **Pull request:** PR #84 `feat: db support` (author @Demon4ort, closes #83): jOOQ
  repositories `UserRepositoryImpl` and `HotelRepositoryImpl`, Flyway migrations, PostgreSQL
  compose file, merged CI workflow and repository tests.
- **Reviewer:** @VeronikaGovorischeva. The PR was approved without comments before the
  checklist review; the findings below were made afterwards.

| № | Criterion                                                    | Answer  | Comments |
|---|--------------------------------------------------------------|---------|----------|
| 1 | All imported packages are real and actually used             | Yes     | The jOOQ, Flyway and PostgreSQL dependencies exist in `pom.xml`, and the code compiles against the classes jOOQ generates from the migrations. Every import in `JooqProducer`, `UserRepositoryImpl` and `HotelRepositoryImpl` is used. |
| 2 | Error handling covers realistic failure scenarios            | Partly  | Good: `UserRepositoryImpl.add` uses `ON CONFLICT (email) DO NOTHING` and returns `false`, so a duplicate email becomes `409`, not a database error. Missing: `HotelRepositoryImpl.add` does not handle the `UNIQUE` violation on `hotels.name`. Two parallel requests with the same name pass the `findByName` check, and the second one ends in an unhandled `DataAccessException` (`500`). |
| 3 | Logic matches the requirements and test plan                 | **No**  | **Blocking:** `HotelRepositoryImpl.sortField(HotelSortBy)` swaps the fields: `case Rating -> HOTELS.PRICE_PER_NIGHT` and `case PricePerNight -> HOTELS.RATING`. Sorting by rating actually sorts by price (US-004, TP-16). The test `searchSortsByRequestedFieldAndOrder` passes anyway, because in its data the cheaper hotel (100, 3.0) also has the lower rating (200, 5.0). That is why the plan's TP-16 requires hotels whose price order and rating order differ. |
| 4 | No dead code                                                 | Yes, Nit | No uncalled methods. Nit: `fetchAmenities(Set<UUID>)` and `fetchAmenities(UUID)` are `public` but are used only inside the class and are not part of `HotelRepository`, so they can be `private`. The `case null` branches in the `sortField` switches are reachable only if `HotelSearchCriteriaFactory` stops setting defaults. |
| 5 | The PR author explained key decisions in answer to reviewer questions | Pending | The PR was approved without questions. The questions below were [asked in the PR](https://github.com/ukma-fin-ssdm-2026/Spectrum-project/pull/84#issuecomment-6393918386) on 25.10.2026; the author's answers are recorded here when he replies. |

#### Questions to the author (PR #84)

1. Which parts of the PR were generated with AI, and how were they checked?
2. Why are `Rating` and `PricePerNight` mapped to the opposite columns in `sortField`? Was
   this intended?
3. Why is the `UNIQUE` violation on `hotels.name` not handled in `HotelRepositoryImpl.add`,
   while `UserRepositoryImpl.add` handles the same case with `ON CONFLICT`?
4. Why were the four workflows merged into one `ci.yml`, and does the formatting check still
   block a pull request on its own?

#### Follow-up

- Done in PR #87: the swapped sort fields are fixed, and the test data now has price and
  rating in different order, so the test fails on the old code (TP-16).
- Done in PR #87: `HotelAlreadyExistsException` is mapped to `409`.
- Open: the `UNIQUE` violation on `hotels.name` in `HotelRepositoryImpl.add` is still not
  handled for two parallel requests (question 3).

### AI usage note

This log was drafted with an LLM (Claude) from the review comment in PR #78, the test plan
and the code of PR #84. Verification: the findings in the AI-generated code review were
checked against the code by @VeronikaGovorischeva; reviewed in the pull request before
merging.
