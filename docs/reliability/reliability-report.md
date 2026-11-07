# Reliability report

Go back to the [Main README](../../README.md).

## Issues and criticality

| Issue                                           | Category | Criticality and reason                               | Current status                                                                         |
|-------------------------------------------------|----------|------------------------------------------------------|----------------------------------------------------------------------------------------|
| User queries without timeouts                   | B, E     | __High:__ blocked requests can exhaust threads       | Fixed: five-second query timeouts                                                      |
| Hotel queries without timeouts                  | B, E     | __High:__ blocked requests can exhaust threads       | Fixed: five-second query timeouts                                                      |
| Room queries without timeouts                   | B, E     | __High:__ blocked requests can exhaust threads       | Fixed: five-second query timeouts                                                      |
| Hotel name conflict during concurrent insertion | A        | __Medium:__ expected conflict becomes HTTP 500       | Partially fixed: conflict won't fail, but will report successful creation despite noop |
| Room query enum conversion/error response       | C, A     | __Medium:__ invalid values receive misleading errors | Fixed: custom conversion covers `RoomSortBy` and `SortOrder`                           |
| Registration ignores unsuccessful insertion     | D        | __High:__ token issued for an unpersisted user ID    | Open                                                                                   |
| Room pagination offset overflow                 | C, A     | __Medium:__ client input triggers HTTP 500           | Open                                                                                   |
| Hotel pagination offset overflow                | C, A     | __Medium:__ client input triggers HTTP 500           | Open                                                                                   |

## Before/after and applied patterns

### Bounded database execution

All three repositories apply the same 5 seconds statement timeout and retry with backoff
on timeout exceptions.

#### Before:

```java
  public Optional<User> findByEmail(String email) {
    return dsl.selectFrom(USERS)
            .where(USERS.EMAIL.eq(email))
            .fetchOptional(UserRepositoryImpl::toUser);
}
```

After:

```java
private static final int QUERY_TIMEOUT_SECONDS = 5;

public Optional<User> findByEmail(String email) {
    return dsl.selectFrom(USERS)
            .where(USERS.EMAIL.eq(email))
            .queryTimeout(QUERY_TIMEOUT_SECONDS)
            .fetchOptional(UserRepositoryImpl::toUser);
}
```

### Validation at the API boundary

An [explicit parameter converter](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/common/param/EnumParamConverterProvider.java)
matches public
enum values and throws `BadRequestException` for unsupported values.

#### Before:

Existing enum declaration; no custom converter

```java

@QueryParam("sortBy")
RoomSortBy sortBy,
```

#### After:

Converter fragment

```java
    public E fromString(String value) {
    if (value == null) {
        return null;
    }

    return Arrays.stream(values)
            .filter(candidate -> candidate.toString().equals(value))
            .findFirst()
            .orElseThrow(
                    () ->
                            new BadRequestException(
                                    "Invalid value '%s' for %s. Allowed values: %s"
                                            .formatted(value, enumType.getSimpleName(), allowedValues())));
}

```

The provider registers only `RoomSortBy` and `SortOrder`.
Existing [RoomControllerTest](../../spectrum-app/src/test/java/edu/ukma/ssdm/teamspectrum/api/room/RoomControllerTest.java)
tests cover their valid values and HTTP 400 for `UNKNOWN`;

## Failure and recovery modes

| Resolved issue                    | Behavior                                                | What the user sees                        | Recovery                 |
|-----------------------------------|---------------------------------------------------------|-------------------------------------------|--------------------------|
| User query timeout                | Fail-fast when the statement exceeds five seconds       | Failed request; HTTP 500                  | Retry after recovery;    |
| Hotel query timeout               | Fail-fast; transactional inserts roll back on failure   | Failed request, HTTP 500                  | Retry after recovery;    |
| Room query timeout                | Fail-fast; transactional mutations roll back on failure | Failed request, HTTP 500                  | Retry after recovery;    |
| Supported sorting enum conversion | Fail-fast for unsupported values                        | HTTP 400 for invalid `sortBy`/`sortOrder` | Retry with correct value |

## Remaining open issues

- **Registration:** check `add(user)` before logging success or issuing a token;
  translate `false` into the existing email-conflict response.
- **Hotel creation:** handle the database uniqueness conflict atomically and return
  HTTP 409, including when concurrent requests pass the preliminary lookup.
- **Pagination in both services:** validate that `page * limit` fits the supported
  offset range and return HTTP 400 instead of an uncaught arithmetic exception.
- **Other enum parameters:** `RoomType` and `RoomStatus` still use framework
  conversion.

## Bonuses:

### Backoff with jitter:

Added [configurable](../../spectrum-app/src/main/resources/application.properties)
back-off with jitter for
[timeout exceptions](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/common/db/DatabaseTimeoutPredicate.java)
thrown by db calls.

```java

@Retry
@ExponentialBackoff
@RetryWhen(exception = DatabaseTimeoutPredicate.class)
public Optional<User> findByEmail(String email) {
    return dsl.selectFrom(USERS)
            .where(USERS.EMAIL.eq(email))
            .queryTimeout(config.queryTimeoutSeconds())
            .fetchOptional(UserRepositoryImpl::toUser);
}

```

### Healthcheck

#### Liveness check

Same for when db `up` or `down`:

```json
{
  "status": "UP",
  "checks": [
    {
      "name": "Database connections health check",
      "status": "UP",
      "data": {
        "<default>": "UP"
      }
    }
  ]
}
```

#### Readiness check

`GET` http://localhost:8080/q/health/ready

##### DB is up

```json
{
  "status": "UP",
  "checks": [
    {
      "name": "Database connections health check",
      "status": "UP",
      "data": {
        "<default>": "UP"
      }
    }
  ]
}
```

##### DB is down:

```json
{
  "status": "DOWN",
  "checks": [
    {
      "name": "Database connections health check",
      "status": "DOWN",
      "data": {
        "<default>": "validation check failed for the default DataSource"
      }
    }
  ]
}
```

### Reliability tests

All tests were tagged with next categories:

- `unit`
- `integration`
- `reliability`

To be able to run them separately.

e.g. for `reliability` tests:
```shell
./mvnw -B test -Dgroups=reliability
```


See [ci workflow](../../.github/workflows/ci.yml)
`Run reliability tests` step for reliability tests run.

#### AI usage:

Initial draft was made with LLM.