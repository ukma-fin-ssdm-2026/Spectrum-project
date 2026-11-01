# Domain Model — Hotel Booking System

Go back to the [Main README](../../README.md).

## 1. Ubiquitous Language

| Term                 | Meaning                                                                                  |
|----------------------|------------------------------------------------------------------------------------------|
| **Client**           | A user who browses hotels/rooms and makes bookings                                       |
| **Hotel Worker**     | A user employed by a hotel, granted specific permissions to manage rooms and/or bookings |
| **Administrator**    | A user who grants/revokes permissions to Hotel Workers                                   |
| **Hotel**            | A property containing one or more rooms                                                  |
| **Room**             | A bookable unit belonging to exactly one hotel                                           |
| **Booking**          | A guest's request to reserve a room for a date range                                     |
| **Approval**         | The act of a Worker confirming a Pending booking                                         |
| **Cancellation**     | The act of terminating a booking, before or after approval                               |
| **Activation**       | The act of making room eligible for booking, after room creation or deactivation         |
| **Deactivation**     | The act of making room non-eligible for booking, after room deactivation                 |
| **Permission Grant** | A scoped set of rights given to a Worker for a specific Hotel                            |
| **Room Scope**       | The subset of rooms within a hotel a Worker is allowed to act on                         |
| **Rating**           | An evaluation left by guest after end of his booking                                     |

 
---

## 2. Bounded Contexts

| Bounded Context               | Responsibility                                      | Key Aggregates                                      |
|-------------------------------|-----------------------------------------------------|-----------------------------------------------------|
| **Identity & Access Context** | Users, roles, authentication, permission grants     | `User`, `PermissionGrant`                           |
| **Hotel Management Context**  | Hotels and their rooms                              | `Hotel` (aggregate root containing `Room` entities) |
| **Booking Context**           | Booking lifecycle: creation, approval, cancellation | `Booking`                                           |
| **Rating Context**            | Reviews left by guest, rating aggregation for hotel | `Review`                                            |

 
---

## 3. Aggregates, Entities, and Value Objects

### 3.1 Identity & Access Context

**Aggregate Root:** `User`

- Entity: `User`
    - `UserId` (identity)
    - `Name` — **personal data**
    - `Email` — **personal data**
    - `PasswordHash` — **sensitive** (credential)
    - `Role` (value object, enum: `CLIENT` | `WORKER` | `ADMIN`) \
    - *Invariants:*
        - no two `User`'s should have the same email
- Entity: `PermissionGrant` — **sensitive** (controls access)
    - `PermissionGrantId`
    - `WorkerId` (reference to `User`)
    - `HotelId` (reference to `Hotel`)
    - `RoomScope` (value object — either `AllRooms` or an explicit set of `RoomId`s)
    - `BookingAccessLevel: array` (enum: `APPROVE` | `CANCEL`)
    - `RoomManagementLevel: array` (enum: `ADD` | `EDIT` | `REMOVE` | `ACTIVATE` |
      `DEACTIVATE`)
    - *Invariants:*
        - Both `BookingAccessLevel` and `RoomManagementLevel` can not be empty
          simultaneously

---

### 3.2 Hotel Management Context

**Aggregate Root: `Hotel`**

- Entity: `Hotel`
    - `HotelId`(identity)
    - `Name`
    - `Address` (value object)
    - `Rooms: array<Room>`
- Entity: `Room`
    - `RoomId`(identity)
    - `RoomNumber`
    - `RoomType` (value object, enum: `SINGLE` | `DOUBLE` | `SUITE`)
    - `PricePerNight` (value object — `Money`)
    - `Status` (value object, enum: `ACTIVE` | `INACTIVE`)
    - *Invariants:*
        - No two `Room`s within the same `Hotel` share a `RoomNumber`.
        - A `Room` cannot be deleted if it has any `PENDING` or `APPROVED` bookings
          referencing it

---

### 3.3 Booking Context

**Aggregate Root: `Booking`**

- Entity: `Booking` — **personal data** (who stays where and when)
    - `BookingId`(identity)
    - `RoomId`
    - `ClientId` (reference to `User`)
    - `DateRange` (value object: `CheckIn`, `CheckOut`, with validation
      `CheckOut > CheckIn`)
    - `Status` (value object, enum: `PENDING` | `APPROVED` | `CANCELLED` | `COMPLETED`)
    - `CreatedAt`
    - *Invariants:*
        - A `Booking` can only transition `PENDING → APPROVED`, `PENDING → CANCELLED`, or
          `APPROVED → CANCELLED`.
        - No two `APPROVED` bookings for the same `RoomId` may have overlapping
          `DateRange`s.
        - Only a `Worker` with a valid `PermissionGrant` may perform the corresponding
          transition.
        - `Booking` can only reference `User` with `Role` = `CLIENT`

### 3.4 Rating Context

**Aggregate Root: `Review`**

- Entity: `Review` — **public data** (hotel evaluation by guest)
    - `ReviewId`(identity)
    - `BookingId`
    - `ClientId` (reference to `User`)
    - `DateRange` (value object: `CheckIn`, `CheckOut`, with validation
      `CheckOut > CheckIn`)
    - `CreatedAt`
    - *Invariants:*
        - Can only reference `APPROVED` `Booking`.
        - `CreatedAt` > `CheckOut`
        - Can only reference `User` with `Role` = `CLIENT`

---

## 4. Personal and sensitive data

Marked fields are the starting point of the [Threat model](threat-model.md).

| Data                                          | Classification | Why                                                |
|-----------------------------------------------|----------------|----------------------------------------------------|
| `User.Name`, `User.Email`                     | Personal       | Identify a person                                  |
| `User.PasswordHash`                           | Sensitive      | Credential; stored only as a salted hash (SEC-002) |
| `PermissionGrant` (all fields)                | Sensitive      | Controls what a worker may do (SEC-003)            |
| `Booking` (`ClientId`, `RoomId`, `DateRange`) | Personal       | Shows who stays where and when (SEC-001)           |
| `Hotel`, `Room`, `Review`                     | Public         | Shown to every visitor in search results           |

---

## 5. Diagram

See [Data model diagram](uml/data_model.puml)

---

## 6. Key feature coverage

Key features are taken from the project description.

| # | Key feature                                   | Model elements                       | Covered                                                                                                                                             |
|---|-----------------------------------------------|--------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| 1 | Hotel and room search with filters            | `Hotel`, `Room`, `Booking.DateRange` | Partly: location, price, and room amenities are represented and searchable; dates are accepted but not applied because bookings are not implemented |
| 2 | Room details with availability for dates      | `Room`, `Booking`                    | Partly: room details are available; date-based availability needs the unimplemented booking data                                                    |
| 3 | Room booking and cancellation                 | `Booking`, `User`                    | Planned: no booking storage or API is implemented yet                                                                                               |
| 4 | Room inventory management by hotel workers    | `Hotel`, `Room`, `PermissionGrant`   | Partly: hotel/room creation, viewing, and room updates exist; deletion and scoped permission grants are not implemented                             |
| 5 | Booking and cancellation notifications        | `Booking`, `User.Email`              | Planned: booking and notification flows are not implemented                                                                                         |
| 6 | Hotel ratings after a stay                    | `Review`, `Hotel`                    | Planned: depends on `Booking`, `Rating` for hotel will be aggregated from `Review`                                                                  |
| 7 | Worker permission management by administrator | `User.Role`, `PermissionGrant`       | Partly: roles exist, but administrator-managed permission grants are not implemented                                                                |

---

### AI usage note:

Initial skeleton was generated with LLM. Sections 4–6 and the sensitive data marks were
drafted with LLM from the threat model and the project description. Verification: reviewed
in the pull request before merging.
