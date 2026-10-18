# Architecture Traceability Matrix (Lab 3)

Go back to the [Main README](../../README.md).

This matrix maps the requirements from Lab 2 ([Requirements](../requirements/requirements.md))
to the architectural components defined in the [High-Level Design](high-level-design.md).
The requirements register (story → NFR → test) stays in [rtm.md](../requirements/rtm.md).

## 1. Functional requirements

| Requirement ID | Requirement Description | Architectural Component | Design Decision | Verification Method |
| :--- | :--- | :--- | :--- | :--- |
| **FR-018** | Hotel administrator permissions and roles management | `Auth Service`, `Database` (PostgreSQL) | Dedicated service for centralized authentication and role-based access control (RBAC). | Integration Test (verifying access to protected endpoints via token). |
| **FR-001 - FR-005**, **FR-017** | Room CRUD operations, activation/deactivation, inventory browsing | `HotelRoom Service`, `Database` (PostgreSQL), `S3 File Storage` | Dedicated microservice for hotel and room content management. Storing room images delegated to external S3 cloud storage. | API Contract Test, Integration Test (verifying DB-Service connectivity). |
| **FR-008 - FR-012**, **US-001, US-002** | Client hotel search, filter application, and results sorting | `HotelRoom Service`, `Cache` (Valkey), `API Gateway` (Nginx) | Caching layer (Valkey) for frequent and filtered client queries to reduce primary database load. | Load Test, Integration Test (verifying cache invalidation). |
| **FR-006, FR-007, FR-013** | Availability check, booking creation, and cancellation | `Booking Service`, `Database` (PostgreSQL) | Isolating booking logic into a separate service with transactional writes to a relational DB to prevent double booking. | E2E Test, Unit Test (verifying date overlap logic). |
| **FR-015, FR-016** | Client notifications on booking creation or cancellation | `Notification Service`, `Email Provider` | Asynchronous notification service trigger from `Booking Service`. Delegating actual email delivery to an external provider. | Mock Test (mocking external Email Provider response). |
| *No requirement yet* | Automated reminders for upcoming bookings | `Scheduler`, `Notification Service`, `Database` | Background process (Scheduler) periodically querying the database and triggering reminder dispatches. | Integration Test (cron jobs execution testing). |
| **US-001 - US-010** | Client and worker interaction with the system | `Web Client`, `API Gateway` (Nginx) | Client application interacts with all internal services exclusively through a single entry point (Reverse Proxy) via HTTPS/REST. | E2E Test (UI automation). |

## 2. Non-functional requirements

| Requirement ID | Requirement Description | Architectural Component | Design Decision | Verification Method |
| :--- | :--- | :--- | :--- | :--- |
| **PERF-001** | Hotel search returns results in ≤ 2 s with 20 concurrent users | `API Gateway` (Nginx), `Hotel and Room Service`, `Cache` (Valkey), `Database` | Cache for repeated search queries; paginated results (at most 100 items per page) | Load test (scenario QAS-1) |
| **REL-001** | No two active bookings of one room overlap | `Booking Service`, `Database` (PostgreSQL) | Booking inserted in one transaction; database constraint on overlapping date ranges ([ADR-002](decisions/ADR-002.md)) | Concurrency integration test (scenario QAS-2) |
| **SEC-001** | A client's personal data is visible only to that client and the hotel's workers | `Auth Service`, `Booking Service` | Every request carries a JWT; ownership of the booking is checked on each request | Automated access-control tests |
| **SEC-002** | Passwords are stored only as salted hashes | `Auth Service`, `Database` | Hashing in the Auth Service before saving; plain text never reaches the database | Code review; check of the stored password column |
| **SEC-003** | A worker manages only rooms and bookings covered by their `PermissionGrant` | `Auth Service`, `Hotel and Room Service`, `Booking Service` | Deny by default; every room and booking operation checks the grant | Authorization tests for every protected endpoint (scenario QAS-3) |

## 3. Reverse check: every component has a requirement

| Component (from the [Component Diagram](uml/component_diagram.puml)) | Requirements |
| :--- | :--- |
| `Client App` | US-001 – US-012 (user interface for all stories) |
| `API Gateway` (Nginx) | All FRs (single entry point), PERF-001, SEC-001 |
| `Auth Service` | FR-018, SEC-001, SEC-002, SEC-003 |
| `Hotel and Room Service` | FR-001 – FR-005, FR-008 – FR-012, FR-017, PERF-001, SEC-003 |
| `Booking Service` | FR-006, FR-007, FR-013, REL-001, SEC-001, SEC-003 |
| `Notification Service` | FR-015, FR-016 |
| `Scheduler` | **None** – see finding below |
| `Database` | All FRs that store data, REL-001, SEC-002 |
| `Storage Provider` (S3) | FR-001, FR-002 (room pictures, US-006) |
| `Email Provider` | FR-015, FR-016 |

**Result.** Every requirement maps to at least one component. One component has no
requirement: the `Scheduler` sends reminders for upcoming bookings, but no FR or user story
asks for reminders. The team either adds a requirement for reminders or removes the
`Scheduler` from the design.

## 4. Quality attribute scenarios

### QAS-1: Performance of hotel search (PERF-001)

| Part | Value |
| :--- | :--- |
| Source | 20 clients at the same time |
| Stimulus | Each searches hotels by location and dates, with filters |
| Artifact | `API Gateway`, `Hotel and Room Service`, `Cache`, `Database` |
| Environment | Normal operation, data for 1,000 hotels |
| Response | Search results are returned, one page per request |
| Response measure | 95th percentile response time ≤ 2 s; less than 1% errors |
| Verification | Load test with k6: 20 virtual users for 5 minutes |

### QAS-2: Reliability of booking under concurrency (REL-001)

| Part | Value |
| :--- | :--- |
| Source | 100 clients at the same time |
| Stimulus | All try to book the same room for the same dates |
| Artifact | `Booking Service`, `Database` |
| Environment | Normal operation |
| Response | Exactly one booking is created; all other requests are rejected |
| Response measure | 1 response `201 Created`, 99 responses `409 Conflict`; 0 overlapping bookings in the database |
| Verification | Concurrency integration test with 100 parallel requests |

### QAS-3: Security of room management (SEC-003)

| Part | Value |
| :--- | :--- |
| Source | An authenticated hotel worker |
| Stimulus | Tries to edit a room of a hotel their `PermissionGrant` does not cover |
| Artifact | `Auth Service`, `Hotel and Room Service` |
| Environment | Normal operation |
| Response | The request is rejected and the room is not changed |
| Response measure | 100% of such requests get `403 Forbidden`; 0 changes in the database |
| Verification | Authorization tests for every room- and booking-management endpoint |

### AI usage note:

Sections 2–4 were drafted with LLM from the requirements, the high-level design and the
component diagram. Verification: reviewed in the pull request before merging.
