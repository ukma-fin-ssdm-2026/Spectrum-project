# API design

Go back to the [Main README](../../README.md).

## 1. Overview

- Local base URL: `http://localhost:8080`
- API style: REST, JSON over HTTP
- Framework: Quarkus (Java 21); resource code is in
  [`spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api`](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api)
- Specification: [`openapi.yaml`](openapi.yaml), generated from the code (Code-First)
- Interactive documentation: Swagger UI at `http://localhost:8080/api-docs` locally and on
  [GitHub Pages](https://ukma-fin-ssdm-2026.github.io/Spectrum-project/)
- Storage: PostgreSQL, configured through `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`; Flyway
  applies the migrations at startup
- Authentication: JWTs are issued by the auth endpoints and returned in an `access_token` cookie.
  Hotel creation requires `WORKER` or `ADMIN`; room creation and updates require `WORKER`.
- Resource paths: hotel and room resources use `/v1/api`; registration and login use `/v1/auth`

## 2. Resource model

Resources follow the [data model](../architecture/data-model.md). The API currently exposes these
fields:

### Hotel

- `hotelId` (UUID), `name` (3–40 non-blank characters), and `address`
- `address` contains `city`, `district`, and `street` (each 3–30 non-blank characters), plus
  `buildingNumber` (integer from 1 to 100)
- Response-only fields: `pictureUrl`, `rating` (0–5), and `amenities`
- Hotel amenities are aggregated from the amenities on its rooms. Hotel creation accepts only
  `name` and `address`. `pricePerNight` is planned to be aggregated from rooms too.

### Room

- Nested under a hotel: `/v1/api/hotels/{hotelId}/rooms`
- `roomId` and `hotelId` (UUIDs), `roomNumber` (3–40 non-blank characters), `roomType`
  (`SINGLE`, `DOUBLE`, or `SUITE`), `pricePerNight`, `roomStatus` (`ACTIVE` or `INACTIVE`), and
  `amenities`
- `pricePerNight` is an object containing a positive `amount` (at most 1,000,000,000) and a
  supported three-character ISO 4217 `currency` code
- Room numbers are unique within a hotel. Room creation and updates can include up to 20 unique
  amenities, each 2–40 characters long.

## 3. Endpoints

| Method | Path | Purpose | Success | Other documented statuses |
|--------|------|---------|---------|----------------------------|
| POST | `/v1/auth/register` | Register a client account | 201 | 400, 409 |
| POST | `/v1/auth/login` | Log in and issue an access token | 200 | 400, 401 |
| POST | `/v1/api/hotels` | Create a hotel (WORKER or ADMIN) | 201 | 400, 401, 403, 409 |
| GET | `/v1/api/hotels` | Search and list hotels | 200 | 400 |
| GET | `/v1/api/hotels/{hotelId}` | Get one hotel | 200 | 400, 404 |
| POST | `/v1/api/hotels/{hotelId}/rooms` | Create a room (WORKER) | 201 | 400, 401, 403, 404, 409 |
| GET | `/v1/api/hotels/{hotelId}/rooms` | Search and list rooms for a hotel | 200 | 400 |
| GET | `/v1/api/hotels/{hotelId}/rooms/{roomId}` | Get one room | 200 | 400, 404 |
| PUT | `/v1/api/hotels/{hotelId}/rooms/{roomId}` | Update supplied room fields (WORKER) | 200 | 400, 401, 403, 404, 409 |

`404` responses from resource lookups and mutation failures currently have empty bodies. A room
list request for a hotel with no matching rooms returns an empty page.

## 4. Request and response behavior

### Authentication

Registration accepts `email` (valid email, at most 254 characters) and `password` (12–72
characters). New accounts receive the `CLIENT` role. Login accepts a valid email and non-blank
password. Successful registration and login return `expiresIn` in the JSON body and set an
`access_token` cookie with `HttpOnly`, `Secure`, and `SameSite=Lax` attributes, scoped to `/v1/api`.
The cookie carries the JWT used to authorize protected hotel and room writes.

### Hotel and room search

Both list endpoints accept `page` (default `0`, minimum `0`) and `limit` (default `10`, range
`1`–`100`). Responses contain `items`, `totalItems`, `totalPages`, `currentPage`, and `limit`.

Hotel search accepts `location` (2–100 characters; matches city or district, case-insensitively),
`minPrice`, `maxPrice`, `minRating`, `maxRating`, repeated `amenity` parameters (up to 20, each
2–40 characters), `sortBy` (`RATING` or `PRICE_PER_NIGHT`, default `RATING`), and `sortOrder`
(`ASC` or `DESC`, default `DESC`). Price values must be non-negative and ratings must be from 0
to 5.

Room search accepts `roomType`, `roomStatus`, `minPrice`, `maxPrice`, repeated `amenity`
parameters, `sortBy` (`ROOM_NUMBER` or `PRICE_PER_NIGHT`, default `PRICE_PER_NIGHT`), and
`sortOrder` (`ASC` or `DESC`, default `ASC`). Price values must be non-negative. Both search
endpoints also accept `checkIn` and `checkOut` dates that must be today or later; date parameters
are currently validated but are not used to filter results(will be when booking functionality will be ready)

The room update body is a partial update: omitted or `null` fields are left unchanged. Supplying
`amenities` replaces the room's amenity set; omitting it preserves the current set. An empty object
is accepted and leaves the room unchanged.

### Status codes and validation

- `POST` creation returns `201 Created` with the created resource; room update returns `200 OK`
  with the updated room. Reads return `200 OK`.
- `400 Bad Request` is used for request validation failures, including invalid path UUIDs and
  query parameters.
- `401 Unauthorized` indicates a missing or invalid authentication token on protected writes;
  `403 Forbidden` indicates the authenticated role lacks permission.
- `404 Not Found` indicates a missing hotel or room on lookups and room mutations.
- `409 Conflict` indicates a duplicate hotel name or a duplicate room number within its hotel.

Validation failures use a JSON object with a `fields` array. Each item contains `path` and
`message`, for example:

```json
{
  "fields": [
    { "path": "name", "message": "size must be between 3 and 40" }
  ]
}
```

Validation errors have this body shape; other error responses such as conflicts, unauthorized,
forbidden, and not found are currently empty. Error responses do not include a machine-readable
error code or request ID.

## 5. Design decisions and current gaps

### Code-first specification

The OpenAPI document is generated from the Quarkus resource and model annotations, keeping request
and response schemas close to the implementation. The application writes the generated schema to
`docs/api` during its configured build/startup workflow.

### Pagination

Lists use page-based offset pagination with defaults `page=0` and `limit=10`, and a maximum limit
of 100. The response wraps items with `totalItems`, `totalPages`, `currentPage`, and `limit`.

### Current search limitations

The hotel and room search request models validate `checkIn` and `checkOut`, but the repository
search logic does not yet apply availability/date filtering.

### Versioning

Hotel and room routes are under `/v1/api`, while auth routes are under `/v1/auth`. A breaking
change should use a new major path version and keep the previous version available during a
migration. Backward-compatible additions do not require a version change.

### AI usage note

This document was drafted with LLM from the API source code and the generated
specification. Verification: reviewed in the pull request before merging.
