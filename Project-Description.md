# Project Description

Go back to the [Main README](./README.md).

> **AI usage note:** first draft of this document was generated with AI assistance (Claude), based on the team's existing `docs/requirements/requirements.md`. Reviewed and finalized by the Project Brief Lead before commit, per section 8 of `TeamCharter.md`.

## 1. Purpose of the system

The system is a web-based hotel room booking platform that lets clients find suitable hotels and rooms and book them, while allowing hotel workers to manage their hotel's room inventory. It solves the problem of fragmented, manual room management and booking coordination between hotels and clients by giving both sides a single, real-time source of truth for room availability.

## 2. System boundaries

**In scope (this semester):**
- Client-facing search and browsing of hotels/rooms by location, dates, price, rating, and amenities
- Room booking and booking cancellation
- Hotel worker tools to create, edit, delete, activate/deactivate rooms, and view an availability calendar
- Administrator tools to manage hotel workers' permissions
- Booking and cancellation notifications to clients
- Client ratings/reviews of a hotel after a stay

**Out of scope (deliberately excluded this semester):**
- Payment processing (bookings are confirmed without an integrated payment flow)
- In-app communication between hotel workers and clients (e.g., chat/messaging)

## 3. User roles

| Role | Can do |
|---|---|
| **Client** | Search/filter hotels, view room availability, book a room, cancel a booking, rate a hotel after a stay, receive notifications |
| **Hotel worker** | Create/edit/delete rooms (when vacant and unbooked), activate/deactivate rooms, view availability calendar, cancel bookings |
| **Administrator** | Manage hotel workers' permissions |

## 4. Key features (high-level)

1. Hotel and room search with filters (location, dates, price, rating, amenities)
2. Room detail view with live availability for selected dates
3. Room booking and cancellation flow
4. Room inventory management for hotel workers (CRUD + activate/deactivate)
5. Booking/cancellation notifications for clients
6. Post-stay hotel ratings from clients
7. Worker permission management by administrators

## 5. Non-functional requirements

Classified per ISO/IEC 25010 quality characteristics, kept simple to implement with standard tools (a DB constraint, a role check, a basic timing test) rather than requiring extra infrastructure.

1. **Reliability — no double-booking:** a database-level constraint must prevent two confirmed bookings from overlapping for the same room and date range, verified by a test that submits two simultaneous booking requests for the same room/dates and confirms only one succeeds.
2. **Performance Efficiency:** the hotel search page must return results within 2 seconds for a typical load of 20 concurrent users on the team's development environment.
3. **Security:** a client's personal data (contact info, booking history) is visible only to that client and to hotel workers/administrators associated with the relevant hotel, enforced by role-based access checks; passwords are stored hashed, never in plain text.

## 6. Assumptions and constraints

- Team size: 4 students, ~10+ weeks of part-time availability (see `TeamCharter.md` for individual hours).
- Scope is intentionally kept small and achievable: no payment integration, no real-time chat, no advanced infrastructure (e.g., no caching layer, no microservices) — a single straightforward monolithic application is enough to meet the requirements.
- Tech stack (agreed by the team): **React** (frontend), **Java** (backend), **PostgreSQL** (database).
- Existing team-authored source of truth for detailed functional requirements: `docs/requirements/requirements.md` (Lab02 deliverable).
