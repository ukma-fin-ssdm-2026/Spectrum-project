# System requirements

Go back to the [Main README](../../README.md).

## Purpose and scope

A web-based hotel room booking system that lets clients find suitable hotels and rooms and
book them, as well as lets hotel workers manage hotel inventory.

Clients can search for hotels by location and dates and browse the results. They can
narrow results filters. Clients can open a hotel's page to view the rooms available there
for the selected dates, including the number of rooms left of each type.

Hotel workers can manage rooms for their hotel. They can see each room's availability
calendar and current occupancy. Hotel workers can activate or deactivate a room to control
whether clients see it and cancel bookings. Rooms that are occupied or booked are
protected from editing and deletion.

Out of scope: payments, in-app communication between hotel workers and clients

## Glossary

- **Client**: a user who searches for hotels and book rooms
- **Hotel**: a place with rooms where clients can stay
- **Room**: a place in a hotel where clients can stay
- **Worker**: a user who manages hotel rooms
- **Booking**: a reservation of a room in a hotel for a period of time
- **Administrator**: a user who manages hotel workers

## Functional Requirements(FR)

Priorities follow MoSCoW: **Must** – needed for the first working version, **Should** –
important but the system works without it, **Could** – implemented last and cut first if
time runs out (see R-004).

- FR-001 (Must): The system shall allow a hotel worker to create a room when all required fields
  contain valid data
- FR-002 (Must): The system shall allow a hotel worker to edit an existing room's details when
  submitted data is valid
- FR-003 (Must): The system shall allow a hotel worker to edit a room when the room is vacant and
  no booking exists
- FR-004 (Must): The system shall allow a hotel worker to delete a room when the room is vacant
  and no booking exists
- FR-005 (Should): The system shall allow a hotel worker to activate or deactivate a room
- FR-006 (Should): The system shall allow a hotel worker to view a room's availability calendar
- FR-007 (Must): The system shall allow a hotel worker to cancel a booking
- FR-008 (Must): The system shall allow a client to search for hotels by location and dates
- FR-009 (Should): The system shall allow a client to filter search results by price
- FR-010 (Should): The system shall allow a client to filter search results by rating
- FR-011 (Should): The system shall allow a client to filter search results by amenities
- FR-012 (Must): The system shall allow a client to view a hotel's rooms
- FR-013 (Must): The system shall allow a client to book a room
- FR-014 (Could): The system shall allow a client to rate a hotel after a stay
- FR-015 (Should): The system shall notify a client about a booking
- FR-016 (Should): The system shall notify a client about a booking cancellation
- FR-017 (Must): The system shall allow a hotel worker to view a hotel's rooms
- FR-018 (Could): The system shall allow a hotel administrator to manage workers' permissions
- FR-019 (Must): The system shall allow a visitor to register an account when submitted credentials are valid
- FR-020 (Must): The system shall reject registration when submitted credentials are invalid or incomplete
- FR-021 (Must): The system shall authenticate a registered user when they submit valid login credentials
- FR-022 (Must): The system shall reject login attempts with invalid credentials
- FR-023 (Should): The system shall allow a client to sort search results by price
- FR-024 (Should): The system shall allow a client to sort search results by rating
- FR-025 (Must): The system shall allow a client to view hotel page
- FR-026 (Should): The system shall allow a hotel worker to approve/reject booking
- FR-027 (Should): The system shall notify a client about approval/rejection of his booking

## Non-Functional Requirements(NFR)

Categories follow ISO/IEC 25010. Every requirement is measurable and names how it is
verified.

### Reliability

- REL-001: The system shall never hold two active bookings with overlapping dates for the
  same room. This is enforced by a database constraint, so 0 double bookings occur even
  when 100 concurrent requests target the same room and dates. \
  *Verification:* concurrency integration test.

### Performance efficiency

- PERF-001: Hotel search by location and dates shall return results in ≤ 2 s with 20
  concurrent users. \
  *Verification:* load test on the search endpoint.

### Security

- SEC-001: A client's personal data (name, email, bookings) shall be visible only to that
  client and to workers of the hotel the booking belongs to. Any other user receives
  `403`. \
  *Verification:* automated access-control tests.
- SEC-002: User passwords shall be stored only as salted hashes (e.g. bcrypt), never as
  plain text. \
  *Verification:* code review and a check that the password column contains no plain
  text.
- SEC-003: A hotel worker shall be able to create, edit, delete, activate or deactivate
  only rooms, and cancel only bookings, that their `PermissionGrant` covers (hotel, room
  scope, access level). 100% of room- and booking-management endpoints return `403` to a
  worker without the required permission. \
  *Verification:* automated authorization tests for every protected endpoint.

### Maintainability

- MAINT-001: Every pull request shall pass the project's automated tests and configured
  static analysis, with no new critical or high-severity findings. \
  *Verification:* corresponding workflows successful runs must be required for pull-request to main branch

### Usability

- USB-001: At least 4 out of 5 first-time users shall be able to search for a room and
  complete a booking without assistance within 5 minutes. \
  *Verification:* observe five representative users completing the task and record
  completion and time.

## Assumptions and limitations

- A-001: Payments are made outside the system (e.g. at the hotel).
- A-002: Hotels and hotel workers are added by an administrator; hotels do not register
  themselves.
- A-003: Every user has a valid email address, used for login and notifications.
- C-001: The team has 4 members and one semester to deliver the system.
- C-002: Email delivery depends on an external email provider, and room pictures are
  stored in an external S3-compatible storage.
- C-003: The system is a web application only; there is no mobile app.

## Risks and open questions

- R-001: Two clients may book the same room for the same dates at the same moment. \
  *Mitigation:* database constraint (REL-001).
- R-002: The email provider may be unavailable, so clients miss booking notifications. \
  *Mitigation:* retry sending later; the booking itself does not depend on the email.
- R-003: Worker permissions (per hotel, per room) can be configured incorrectly and give
  too much access. \
  *Mitigation:* deny access by default and cover it with tests (SEC-001).
- R-004: The scope may not fit into one semester. \
  *Mitigation:* rating (FR-014) and permission management (FR-018) are implemented last
  and can be cut.
- OQ-001: Does a booking need worker approval (`PENDING → APPROVED` in the data model),
  or is it confirmed immediately? No FR covers approval yet.
- OQ-002: Can a client cancel their own booking, and until when?
- OQ-003: Who may rate a hotel — only clients with a completed stay there?

### AI usage note:

The NFR wording, assumptions, risks and open questions were drafted with an LLM (Claude)
from the NFRs agreed by the team and the existing requirements and architecture
documents. FR priorities were proposed by the LLM based on the key features and R-004. SEC-003 was proposed by the LLM to cover the worker stories (US-006 – US-010)
and agreed by the team. Verification: reviewed by the team in the pull request before merging.

