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

 
---

## 2. Bounded Contexts

| Bounded Context               | Responsibility                                      | Key Aggregates                                      |
|-------------------------------|-----------------------------------------------------|-----------------------------------------------------|
| **Identity & Access Context** | Users, roles, authentication, permission grants     | `User`, `PermissionGrant`                           |
| **Hotel Management Context**  | Hotels and their rooms                              | `Hotel` (aggregate root containing `Room` entities) |
| **Booking Context**           | Booking lifecycle: creation, approval, cancellation | `Booking`                                           |

 
---

## 3. Aggregates, Entities, and Value Objects

### 3.1 Identity & Access Context

**Aggregate Root:** `User`

- Entity: `User`
    - `UserId` (identity)
    - `Name`
    - `Email`
    - `PasswordHash`
    - `Role` (value object, enum: `CLIENT` | `WORKER` | `ADMIN`) \
    - *Invariants:*
        - no two `User`'s should have the same email
- Entity: `PermissionGrant`
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

- Entity: `Booking`
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

---

### AI usage note:

Initial skeleton was generated with LLM.