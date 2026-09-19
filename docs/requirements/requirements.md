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

- FR-001: The system shall allow a hotel worker to create a room when all required fields
  contain valid data
- FR-002: The system shall allow a hotel worker to edit an existing room's details when
  submitted data is valid
- FR-003: The system shall allow a hotel worker to edit a room when the room is vacant and
  no booking exists
- FR-004: The system shall allow a hotel worker to delete a room when the room is vacant
  and no booking exists
- FR-005: The system shall allow a hotel worker to activate or deactivate a room
- FR-006: The system shall allow a hotel worker to view a room's availability calendar
- FR-007: The system shall allow a hotel worker to cancel a booking
- FR-008: The system shall allow a client to search for hotels by location and dates
- FR-009: The system shall allow a client to filter search results by price
- FR-010: The system shall allow a client to filter search results by rating
- FR-011: The system shall allow a client to filter search results by amenities
- FR-012: The system shall allow a client to view a hotel's rooms
- FR-013: The system shall allow a client to book a room
- FR-013: The system shall allow a client to rate a hotel after a stay
- FR-014: The system shall notify a client about a booking
- FR-014: The system shall notify a client about a booking cancellation
- FR-015: The system shall allow a hotel worker to view a hotel's rooms
- FR-016: The system shall allow a hotel administrator to manage workers' permissions

## Assumptions and limitations

