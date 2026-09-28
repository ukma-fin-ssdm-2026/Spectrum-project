# Architecture Traceability Matrix (Lab 3)

This matrix maps functional requirements (from Lab 2) to architectural components defined during high-level design (HLD).

| Requirement ID | Requirement Description | Architectural Component | Design Decision | Verification Method |
| :--- | :--- | :--- | :--- | :--- |
| **FR-018** | Hotel administrator permissions and roles management | `Auth Service`, `Database` (PostgreSQL) | Dedicated service for centralized authentication and role-based access control (RBAC). | Integration Test (verifying access to protected endpoints via token). |
| **FR-001 - FR-005**, **FR-017** | Room CRUD operations, activation/deactivation, inventory browsing | `HotelRoom Service`, `Database` (PostgreSQL), `S3 File Storage` | Dedicated microservice for hotel and room content management. Storing room images delegated to external S3 cloud storage. | API Contract Test, Integration Test (verifying DB-Service connectivity). |
| **FR-008 - FR-012**, **US-001, US-002** | Client hotel search, filter application, and results sorting | `HotelRoom Service`, `Cache` (Valkey), `API Gateway` (Nginx) | Caching layer (Valkey) for frequent and filtered client queries to reduce primary database load. | Load Test, Integration Test (verifying cache invalidation). |
| **FR-006, FR-007, FR-013** | Availability check, booking creation, and cancellation | `Booking Service`, `Database` (PostgreSQL) | Isolating booking logic into a separate service with transactional writes to a relational DB to prevent double booking. | E2E Test, Unit Test (verifying date overlap logic). |
| **FR-015, FR-016** | Client notifications on booking creation or cancellation | `Notification Service`, `Email Provider` | Asynchronous notification service trigger from `Booking Service`. Delegating actual email delivery to an external provider. | Mock Test (mocking external Email Provider response). |
| *Implicit Req. (US-005)* | Automated reminders for upcoming bookings | `Scheduler`, `Notification Service`, `Database` | Background process (Scheduler) periodically querying the database and triggering reminder dispatches. | Integration Test (cron jobs execution testing). |
| **US-001 - US-010** | Client and worker interaction with the system | `Web Client`, `API Gateway` (Nginx) | Client application interacts with all internal services exclusively through a single entry point (Reverse Proxy) via HTTPS/REST. | E2E Test (UI automation). |
