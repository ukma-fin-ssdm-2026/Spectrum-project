## SonarCloud report

Go back to the [Main README](../../README.md).

#### Report for `7585214a`

Currently, there are ___2,219___ lines of code in [src](../../spectrum-app/src)

### Before:

See [screenshot from analysis panel](measures_before_7585214a.png)

| Measure                       | Value |
|-------------------------------|-------|
| ___*Code Smells___            | 0     |
| ___*Maintainability Rating___ | A     |
| Technical Debt                | 0 h   |
| Duplicated Lines              | 0%    |
| ___*Reliability Rating___     | A     |
| Congnitive complexity         | 73    |
| ___*Security Rating___        | C     |

### Refactoring:

As linter haven't found any problems with current implementation, next measures will be
chosen:

- __Code smells__ - (no code smells, no duplications found):
    1. Amenities for hotels are fetched inside mapping function `toHotels`, hiding IO
       operation and making code more obscure(
       see [HotelRepositoryImpl at line 195](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/hotel/HotelRepositoryImpl.java)).
       Also `HotelRepositoryImpl` has second-highest cognitive complexity at project.
        - ___Fix:___ `Separating data retrival from transformation`, or if choosing from
          catalogue - `Add Parameter`
        - __Unchanged:__ Returned hotel data.
        - __Changes in structure:__ `toHotels` now takes amenities map as an argument.
          `search` now fetch amenities before transforming fetched list of `HotelsRecord`
          to `HotelRecord`.
        - __Test:__ `searchFiltersByLocationPriceRatingAndAllRequestedAmenities` test case
          at [HotelRepositoryImplTest at line 89](../../spectrum-app/src/test/java/edu/ukma/ssdm/teamspectrum/logic/hotel/HotelRepositoryImplTest.java)

    2. [HotelAlreadyExistsException](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/hotel/HotelAlreadyExistsException.java)
       are thrown (
       see [HotelServiceImpl at line 33](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/hotel/HotelServiceImpl.java))
       instead of returning result type as sealed hierarchy (
       see [RoomMutationResult](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/room/RoomMutationResult.java))
       and then mapped to the endpoint error code outside the controller (
       see [HotelAlreadyExistsExceptionMapper](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/hotel/HotelAlreadyExistsExceptionMapper.java)).
       This obscures logic under the guise of DRY.
        - ___Fix:___ `Inline class`(also `Exception -> sealed result`)
        - __Unchanged:__ Response of [
          `HotelController.create` at line 67](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/hotel/HotelController.java)
          still return `201` or `409` depending on result
          from [HotelService](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/hotel/HotelService.java)
        - __Changes in structure:__ `HotelAlreadyExistsExceptionMapper` was removed, and [
          `HotelController.create` at line 67](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/hotel/HotelController.java)
          now switches
          on [HotelMutationResult](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/hotel/HotelMutationResult.java)
          directly.
        - __Test:__ `createReturnsCreatedForWorker` and
          `createReturnsConflictWhenHotelNameExists` test cases
          of [HotelControllerTest](../../spectrum-app/src/test/java/edu/ukma/ssdm/teamspectrum/api/hotel/HotelControllerTest.java).
    3. Method `update`
       at [RoomRepositoryImpl](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/room/RoomRepositoryImpl.java)
       as a function with the highest cognitive complexity(4 branches, multiple db IO
       inside transaction).
        - ___Fix:___
            - `Extract Method`: amenity updates, check for room number and room mapping
              are moved into a helpers.
            - `Decompose Conditional`: the room number conflict condition and query were
              extracted into `roomNumberInUse`.
            - `Replace Nested Conditional`: not-found and conflict cases return early.
        - __Unchanged:__ actual logic stays the same if we look at function as a black
          box: transaction and row locks, not-found and conflict results, room field
          updates, and amenity behavior remain the same
        - __Changes in structure:__ update delegates its transaction work to
          `updateInTransaction`; duplicate room number checking is in `roomNumberInUse`;
          amenity handling and `RoomRecord` creation are in `updateAmenities`
        - __Test:__ `updateReturnsUpdatedRoomAndReplacesAmenities`,
          `updateReturnsNotFoundWhenRoomDoesNotExist`,
          `updateReturnsNotFoundWhenHotelDoesNotExist`, and
          `updateReturnsRoomNumberConflictWhenAnotherRoomUsesRequestedNumber`
          at [RoomRepositoryTest](../../spectrum-app/src/test/java/edu/ukma/ssdm/teamspectrum/logic/room/RoomRepositoryTest.java)

- __Maintainability Rating__
    - Problem _#3_ and _#2_ from `Code smells` point will cause maintainability problems
      in future

- __Reliability Rating__
    - Problem _#1_ and _#2_ from `Code smells` point will cause reliability problems in
      future

- __Security Rating__
    - `Medium` issue found at [build configuration file](../../spectrum-app/pom.xml):
  ```
  (27, 8) "jooq.codegen.password" detected here, make sure this is not a hard-coded credential. [xml:S2068]
  ```
  Same issue was reported for [ci.yml](../../.github/workflows/ci.yml)
  and [sonar.yml](../../.github/workflows/sonar.yml). It isn't shown at remote analysis
  panel, due to configuration causing it to only run
  for [spectrum-app](../../spectrum-app). These additional issues were found with IntelliJ
  IDEA
  plugin "SonarQube for IDE" while connected to cloud project(
  ukma-fin-ssdm-2026_Spectrum-project. \
  ___Fix:___ password is now passed from env. For workflows `DEV_DB_PASSWORD` secret was
  created.

    - Issue in [ci.yml](../../.github/workflows/ci.yml) that allows to inject bash script
      through branch name:
  ```
  SonarQube: The expression github.event.pull_request.head.ref can be set by an external actor to a specially crafted value, enabling script injection. Change this workflow to not use user-controlled data directly in a run block, for example by assigning this expression to an environment variable
  ```
  ___Fix:___ change how branch name are passed.

#### NB:

As per our [Team charter](../../TeamCharter.md#82-mandatory-tools): pre-commit is required
to be used in development. Its [configuration](../../.pre-commit-config.yaml) has our
linter `spotbugs`, therefore no errors/warnings pass to main branch, except for the ones,
which are explicitly supressed with justification(
e.g. [HotelServiceImpl at line 15](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/hotel/HotelServiceImpl.java)).

### Regression testing:

All tests passed successfully. Command (the same one the CI job `checks` runs, from
`spectrum-app`, with PostgreSQL from `compose.yaml` and `DB_PASSWORD` set):

```shell
./mvnw -B spotless:check verify spotbugs:check
```

```terminaloutput
[INFO] Results:
[INFO] 
[INFO] Tests run: 174, Failures: 0, Errors: 0, Skipped: 0
```

### After:

#### Report for `9d88f0d`

See [screenshot from analysis panel](measures_after_9d88f0d.png)

| Measure                       | Value |
|-------------------------------|-------|
| ___*Code Smells___            | 0     |
| ___*Maintainability Rating___ | A     |
| Technical Debt                | 0 h   |
| Duplicated Lines              | 0%    |
| ___*Reliability Rating___     | A     |
| Congnitive complexity         | 68    |
| ___*Security Rating___        | A     |

### Refactoring for `v0.2.1`

Second round, done by @VeronikaGovorischeva in PR #106.

#### Before: report for `80ec284` (analysis of 07.11.2026, version `0.2.0`)

| Measure                      | Project | `EnumParamConverterProvider` | `RoomFactory` |
|------------------------------|---------|------------------------------|---------------|
| ___*Code Smells___           | 0       | 0                            | 0             |
| ___*Cognitive complexity___  | 96      | 5                            | 8             |
| ___*Cyclomatic complexity___ | —       | 11                           | 20            |
| Maintainability Rating       | A       |                              |               |
| Reliability Rating           | A       |                              |               |
| Duplicated Lines             | 0%      |                              |               |
| Security Rating              | A       |                              |               |

SonarQube still reports no code smells, so the refactorings were chosen by reading the
code of the API layer, which the first round did not touch.

#### Refactorings

1. [EnumParamConverterProvider](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/common/param/EnumParamConverterProvider.java)
   `getConverter` chose the converter with an `if`/`else if` chain on the parameter type
   and created a new converter on every call. Every new enum query parameter would add one
   more branch (code smell: _Switch Statements_, a conditional on type).
    - ___Fix:___ `Substitute Algorithm` (`Replace Conditional with Lookup Table`): a
      `Map` from the supported enum class to its converter.
    - __Unchanged:__ `SortOrder` and `RoomSortBy` are parsed from their wire values (`ASC`,
      `ROOM_NUMBER`), any other value gives `400` with the list of allowed values, `null`
      stays `null`, and other types still get no converter.
    - __Changes in structure:__ one converter per enum is created once; `getConverter` is a
      single lookup, and adding an enum means adding one map entry.
    - __Test:__ characterization tests in
      [EnumParamConverterProviderTest](../../spectrum-app/src/test/java/edu/ukma/ssdm/teamspectrum/api/common/param/EnumParamConverterProviderTest.java)
      (committed before the refactoring, see [Bonuses](#bonuses)) and the `sortBy` and
      `sortOrder` cases of `RoomControllerTest`.
2. [RoomFactory](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/room/RoomFactory.java)
   `toLogic(CreateRoom)` repeated the type, price and status conversions inline, although
   the class already has converter methods for them (code smell: _Duplicated Code_).
   `toLogic(UpdateRoom)` started with a five-part `&&` condition and five almost equal
   `FieldError` lines (code smells: _Long Method_, _Duplicated Code_).
    - ___Fix:___ `Replace Inline Code with Function Call` for the conversions;
      `Decompose Conditional` into `hasNoFields`; `Extract Method` `noFieldsError`, which
      builds the errors from one list of field names.
    - __Unchanged:__ the created and updated commands, and the `400` response with one
      error per field when an update has no fields. The converters return `null` for
      `null`, but `CreateRoom` fields are `@NotNull`, so a valid request is mapped exactly
      as before.
    - __Changes in structure:__ each conversion exists once; the field names of the update
      error are in one list.
    - __Test:__ `mapsCreateRoomToLogicCommand`, `mapsUpdateRoomToLogicCommand` and
      `rejectsUpdateRoomWithNoFields` in
      [RoomFactoryTest](../../spectrum-app/src/test/java/edu/ukma/ssdm/teamspectrum/api/room/RoomFactoryTest.java),
      and the create and update cases of `RoomControllerTest`.

#### Regression testing

Same command as above. All tests passed, including the 11 new characterization tests:

```terminaloutput
Tests run: 226, Failures: 0, Errors: 0, Skipped: 0
[INFO] BugInstance size is 0
[INFO] All coverage checks have been met.
[INFO] BUILD SUCCESS
```

#### After

SonarQube Cloud analysis of PR #106 (it measures the changed files; Quality Gate: passed):

| Measure                      | Project       | `EnumParamConverterProvider` | `RoomFactory` |
|------------------------------|---------------|------------------------------|---------------|
| ___*Code Smells___           | 0             | 0                            | 0             |
| ___*Cognitive complexity___  | 93 (96 − 3)   | 2 (was 5)                    | 8 (was 8)     |
| ___*Cyclomatic complexity___ | —             | 9 (was 11)                   | 23 (was 20)   |
| Maintainability Rating       | A             |                              |               |
| Reliability Rating           | A             |                              |               |
| Duplicated Lines             | 0%            |                              |               |
| Security Rating              | A             |                              |               |

The project value 93 is the analysis of `80ec284` minus the change in the two files; the
analysis of `main` after the merge confirms it.

- **Improved:** `EnumParamConverterProvider` cognitive complexity 5 → 2, because the
  `if`/`else if` chain is gone.
- **Not improved by the metric:** `RoomFactory` cognitive complexity stays 8, and its
  cyclomatic complexity grows 20 → 23, because SonarQube counts every method as +1 and two
  methods were extracted. The gain there is removed duplication (each conversion and each
  field name exists once) and a shorter `toLogic(UpdateRoom)`, which these metrics do not
  show.
- **Security Rating:** A before and after; the changes touch no security-relevant code.

#### AI usage:

##### First round (`v0.1.1`)

- **Performed by the agent:** all three refactorings in [Refactoring](#refactoring). The
  agent generated the code changes: the new `toHotels` signature and the amenity query in
  `search`; `HotelMutationResult` and the `switch` in `HotelController.create` in place of
  `HotelAlreadyExistsExceptionMapper`; the `updateInTransaction`, `roomNumberInUse` and
  `updateAmenities` helpers in `RoomRepositoryImpl`. After the initial analysis, the agent
  was told what to change in each feature.
- **Verified by:** review of every change in PR #94; the regression tests after each
  finished change (174 tests, 0 failures); the metrics after the merge (Cognitive
  Complexity 73 → 68, Security Rating C → A).
- The descriptions of the code smells were drafted with an LLM.

##### Second round (`v0.2.1`)

- **Performed by the agent (Claude Code):** both refactorings, the characterization tests
  and the descriptions in [Refactoring for `v0.2.1`](#refactoring-for-v021). The agent
  proposed the candidates from the SonarQube Cloud metrics per file and generated the map
  lookup in `EnumParamConverterProvider` and the `hasNoFields` and `noFieldsError` helpers
  in `RoomFactory`.
- **Verified by:** the characterization tests were run on the old code first (11 passed)
  and committed before the refactoring; after each `[REFACTOR]` commit the related tests
  were run, and after both the full build above (226 tests, SpotBugs 0); the `@NotNull`
  constraints of `CreateRoom` were checked by hand to confirm that the `null`-safe
  converters do not change the result for valid requests; the metrics of the PR analysis
  are in the table above; review in PR #106.

#### Bonuses:

##### Quality Gate:

[Failed](https://sonarcloud.io/project/issues?id=ukma-fin-ssdm-2026_Spectrum-project&pullRequest=104&s=IMPACT_RANK&open=AaG3Lr19tqj7RNvsZzSE)
at [pull-request](https://github.com/ukma-fin-ssdm-2026/Spectrum-project/pull/104).

Problem was with `@Startup` annotation usage
in [AppConfigProducer](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/config/AppConfigProducer.java),
because it produces dependencies. Was fixed by changing annotation for it to listen to
`StartupEvent`.

[Succeeded](https://github.com/ukma-fin-ssdm-2026/Spectrum-project/pull/104#issuecomment-6711283838)
after [fix](https://github.com/ukma-fin-ssdm-2026/Spectrum-project/commit/9dc1754571e1be7ac96563603b9e15365a3d4a10)

##### Characterization tests before refactoring:

- **Module:** [EnumParamConverterProvider](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/common/param/EnumParamConverterProvider.java)
  had no tests of its own.
- **Tests:** [EnumParamConverterProviderTest](../../spectrum-app/src/test/java/edu/ukma/ssdm/teamspectrum/api/common/param/EnumParamConverterProviderTest.java),
  11 cases. They record the current behavior, including the odd parts: the enum constant
  name (`Asc`, `RoomNumber`) is rejected and only the wire value (`ASC`, `ROOM_NUMBER`) is
  accepted; matching is case-sensitive and does not trim spaces; `HotelSortBy` gets no
  converter from this provider; the error message lists the values in declaration order.
- **Order:** the test commit `test: characterization tests for enum query parameter
  converter` comes before the refactoring commit `refactor: look up enum query parameter
  converters in a map [REFACTOR]` in PR #106.
- **Before and after:** 11 of 11 pass on the old code (run before the test commit) and on
  the refactored code (run after the `[REFACTOR]` commit and in CI of PR #106).
