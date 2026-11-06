## Scavenger hunt

### UserRepositoryImpl

#### No timeout set for any query in `UserRepositoryImpl`

__Category:__ `E` \
__Code:__

```java
  public Optional<User> findByEmail(String email) {
    return dsl.selectFrom(USERS)
            .where(USERS.EMAIL.eq(email))
            .queryTimeout(QUERY_TIMEOUT_SECONDS)
            .fetchOptional(UserRepositoryImpl::toUser);
}
```

__Why is it dangerous:__ Depending on current and future implementations of db access can
cause calls to block indefinitely \
__Potential impact:__ thread starvation and degraded availability

| Field        | Explanation                                                                                                          |
|--------------|----------------------------------------------------------------------------------------------------------------------|
| **Fault**    | [UserRepositoryImpl](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/auth/UserRepositoryImpl.java) |
| **Error**    | Query potentially blocks indefinitely                                                                                |
| **Failure**  | `408` or `504` error code                                                                                            |
| **Severity** | High                                                                                                                 |

### HotelRepositoryImpl

#### No timeout set for any query in `HotelRepositoryImpl`

__Category:__ `B`, `E` (historical concern; timeout already present) \
__Code:__

```java
  public Optional<HotelRecord> findById(UUID id) {
    return dsl.selectFrom(HOTELS)
            .where(HOTELS.ID.eq(id))
            .queryTimeout(QUERY_TIMEOUT_SECONDS)
            .fetchOptional(record -> toHotel(record, fetchAmenities(id)));
}
```

__Why is it dangerous:__ Depending on current and future implementations of db access can
cause calls to block indefinitely \
__Potential impact:__ thread starvation and degraded availability

| Field        | Explanation                                                                                                             |
|--------------|-------------------------------------------------------------------------------------------------------------------------|
| **Fault**    | [HotelRepositoryImpl](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/hotel/HotelRepositoryImpl.java) |
| **Error**    | Query potentially blocks indefinitely                                                                                   |
| **Failure**  | `408` or `504` error code                                                                                               |
| **Severity** | High                                                                                                                    |

#### `add` method doesn't handle name conflict

__Category:__ `A` \
__Code:__

```java
  public void add(HotelRecord hotel) {
    dsl.transaction(
            configuration -> {
                DSLContext tx = DSL.using(configuration);
                tx.insertInto(HOTELS)
                        .set(HOTELS.ID, hotel.id())
                        .set(HOTELS.NAME, hotel.name())
                        .set(HOTELS.CITY, hotel.address().city())
                        .set(HOTELS.DISTRICT, hotel.address().district())
                        .set(HOTELS.STREET, hotel.address().street())
                        .set(HOTELS.BUILDING_NUMBER, hotel.address().buildingNumber())
                        .set(HOTELS.PICTURE_URL, hotel.pictureUrl())
                        .set(HOTELS.PRICE_PER_NIGHT, hotel.pricePerNight())
                        .set(HOTELS.RATING, hotel.rating())
                        .queryTimeout(QUERY_TIMEOUT_SECONDS)
                        .execute();
            });
}

```

__Why is it dangerous:__ The service checks for a duplicate before calling
`HotelRepositoryImpl.add`, but another request can insert the same name between those
operations. The insert has no conflict handling, and the resulting uniqueness exception is
not translated into `NameConflict`. \
__Potential impact:__ Concurrent hotel creation can return HTTP `500` instead of the
documented HTTP 409 conflict response.

| Field        | Explanation                                                                                                             |
|--------------|-------------------------------------------------------------------------------------------------------------------------|
| **Fault**    | [HotelRepositoryImpl](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/hotel/HotelRepositoryImpl.java) |
| **Error**    | Confict error is raised due to existence of hotel with such name                                                        |
| **Failure**  | `500` error code                                                                                                        |
| **Severity** | Medium                                                                                                                  |

### RoomRepositoryImpl

#### No timeout set for any query in `RoomRepositoryImpl`

__Category:__ `E`  \
__Code:__

```java
  private Set<String> fetchAmenities(DSLContext context, UUID roomId) {
    return context
            .select(AMENITIES.NAME)
            .from(ROOM_AMENITIES)
            .join(AMENITIES)
            .on(ROOM_AMENITIES.AMENITY_ID.eq(AMENITIES.ID))
            .where(ROOM_AMENITIES.ROOM_ID.eq(roomId))
            .queryTimeout(QUERY_TIMEOUT_SECONDS)
            .fetchSet(AMENITIES.NAME);
}
```

__Why is it dangerous:__ Depending on current and future implementations of db access can
cause calls to block indefinitely \
__Potential impact:__ thread starvation and degraded availability

| Field        | Explanation                                                                                                          |
|--------------|----------------------------------------------------------------------------------------------------------------------|
| **Fault**    | [RoomRepositoryImpl](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/room/RoomRepositoryImpl.java) |
| **Error**    | Query potentially blocks indefinitely                                                                                |
| **Failure**  | `408` or `504` error code                                                                                            |
| **Severity** | High                                                                                                                 |

### GetRoomsParams

#### No validation for enums in `GetRoomsParams`

__Category:__ `C`, `A` \
__Code:__

```java
public record GetRoomsParams(
        @QueryParam("roomType")
        @Parameter(
                name = "roomType",
                in = ParameterIn.QUERY,
                example = RoomType.Values.Double,
                schema =
                @Schema(
                        enumeration = {
                                RoomType.Values.Single,
                                RoomType.Values.Double,
                                RoomType.Values.Suite
                        }))
        RoomType roomType) {
}
```

__Why is it dangerous:__ Invalid values for enums are not handled and cause
`IllegalArgumentException` \
__Potential impact:__ Wrong handling of invalid value which will confuse user

| Field        | Explanation                                                                                                |
|--------------|------------------------------------------------------------------------------------------------------------|
| **Fault**    | [GetRoomsParams](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/room/GetRoomsParams.java) |
| **Error**    | Enum types are not checked                                                                                 |
| **Failure**  | `404` error code for validation error                                                                      |
| **Severity** | Medium                                                                                                     |

### AuthServiceImpl

#### `userRepository.add(user)` result is ignored

__Category:__ `D` \
__Code:__

```java
  public IssuedToken register(RegisterRequest request) {
    logger.debug("Register request: {}", request.email());
    String email = userFactory.normalizeEmail(request.email());
    User user = userFactory.create(email, request.password());
    if (userRepository.findByEmail(user.email()).isPresent()) {
        throw new EmailAlreadyRegisteredException();
    } else {
        userRepository.add(user);
        logger.debug("Registered user: {}", user.email());
        return tokenFactory.create(user);
    }
}
```

__Why is it dangerous:__ `add` returns `false` when an email conflict prevents insertion,
but this result is ignored. Concurrent registrations can both pass the initial lookup, and
the losing request still logs success and receives a token. \
__Potential impact:__ HTTP 201 and a signed token can be returned for a user ID that was
never persisted, causing inconsistent authentication and later failures when that ID is
used.

| Field        | Explanation                                                                                                    |
|--------------|----------------------------------------------------------------------------------------------------------------|
| **Fault**    | [AuthServiceImpl](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/auth/AuthServiceImpl.java) |
| **Error**    | Token is granted even when `add` returns false                                                                 |
| **Failure**  | User without account receives working token                                                                    |
| **Severity** | High                                                                                                           |

### RoomServiceImpl

#### Offset calculation in `findPage` method may overflow int

__Category:__ `C`, `A` \
__Code:__

```java
  public RoomPage findPage(UUID hotelId, RoomSearchCriteria criteria) {
    int offset = Math.multiplyExact(criteria.page(), criteria.limit());
    return repository.findPage(hotelId, criteria, offset, criteria.limit());
}
```

__Why is it dangerous:__ Pagination validation permits any non-negative `int` page and
limits up to 100. For example, `page=2147483647&limit=100` passes those constraints but
causes `Math.multiplyExact` to throw an unhandled `ArithmeticException`. \
__Potential impact:__ Client-controlled pagination produces HTTP `500` instead of an
explicit validation error.

| Field        | Explanation                                                                                                    |
|--------------|----------------------------------------------------------------------------------------------------------------|
| **Fault**    | [RoomServiceImpl](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/room/RoomServiceImpl.java) |
| **Error**    | Offset calculation throws `ArithmeticException` due to int overflow                                            |
| **Failure**  | `500` error code                                                                                               |
| **Severity** | Medium                                                                                                         |

### HotelServiceImpl

#### Offset calculation in `search` method may overflow int

__Category:__ `C`, `A` \
__Code:__

```java
  public HotelSearchPage search(HotelSearchCriteria criteria) {
    int offset = Math.multiplyExact(criteria.page(), criteria.limit());
    return new HotelSearchPage(
            repository.search(criteria, offset, criteria.limit()), repository.count(criteria));
}
```

__Why is it dangerous:__ Pagination validation permits any non-negative `int` page and
limits up to 100. For example, `page=2147483647&limit=100` passes those constraints but
causes `Math.multiplyExact` to throw an unhandled `ArithmeticException`. \
__Potential impact:__ Client-controlled pagination produces HTTP `500` instead of an
explicit validation error.

| Field        | Explanation                                                                                                       |
|--------------|-------------------------------------------------------------------------------------------------------------------|
| **Fault**    | [HotelServiceImpl](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/hotel/HotelServiceImpl.java) |
| **Error**    | Offset calculation throws `ArithmeticException` due to int overflow                                               |
| **Failure**  | `500` error code                                                                                                  |
| **Severity** | Medium                                                                                                            |


#### AI usage:

LLM was used to draft troubleshooting measures