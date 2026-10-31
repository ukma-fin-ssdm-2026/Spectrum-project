## SonarCloud report

Go back to the [Main README](../../README.md).

#### Report for `7585214a`

Currently, there are ___2,219___ lines of code in [src](../../spectrum-app/src)

### Before:

See [screenshot from analysis panel](measures_before_7585214a.png)

| Measure                | Value |
|------------------------|-------|
| Code Smells            | 0     |
| Maintainability Rating | A     |
| Technical Debt         | 0 h   |
| Duplicated Lines       | 0%    |
| Reliability Rating     | A     |
| Security Rating        | C     |

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
  for [spectrum-app](../../spectrum-app). These additional issues were found with IntelliJ IDEA
  plugin "SonarQube for IDE" while connected to cloud project(ukma-fin-ssdm-2026_Spectrum-project. \
  ___Fix:___ password is now passed from env. For workflows `DEV_DB_PASSWORD` secret was created.

  - Issue in [ci.yml](../../.github/workflows/ci.yml) that allows to inject bash script through branch name:
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
All tests passed successfully.
```terminaloutput
[INFO] Results:
[INFO] 
[INFO] Tests run: 174, Failures: 0, Errors: 0, Skipped: 0
```

#### AI usage:

Refactoring was performed with LLM. Descriptions for code smells were drafted with LLM.