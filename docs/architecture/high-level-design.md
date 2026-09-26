# High-Level Design (HLD)

Go back to the [Main README](README.md).

## 1. Introduction

Booking system for hotels

### 2.1 Requirements Reference

**System Requirements:**
See [Requirements Document](../requirements/requirements.md)

---

## 3. System Architecture

System follows a client-server architecture. **Web Client** communicates with backend over
HTTPS. Request will go through **API Gateway**(nginx proxy) to allow server be spit into
microservices in case of inability to optimize in order to meet requirements.

*Guests* interact with the **Hotel and Room Service** to browse hotels and view room
availability, then submit a booking request to the **Booking Service**.

*Hotel Workers* use the **Booking Service** to approve/cancel bookings and manage rooms,
but every action is checked against the **Auth Service**, which stores per-hotel,
per-worker permissions (e.g., "can edit rooms 101–110", "can approve bookings").
*Administrators* manage these permission through the AuthService.

When a booking is created, approved, or canceled, the **Booking Service** emits calls the
**Notification Service**, which sends the corresponding email. A **Scheduler**
periodically queries upcoming bookings and triggers reminder notifications through the
same service.

All services share a central **Database**, with the **Notification Service** depending
only on booking/user data needed to compose emails. Actual email delivery is delegated to
an external **Email Provider**.

### 3.1 Deployment Diagram

See [Deployment Diagram](uml/deployment_diagram.puml)

### 3.2 Component Descriptions

See [Component Diagram](uml/component_diagram.puml)

* **API Gateway:** Entry point for all client requests; handles rate limiting and SSL
  termination.
* **Core Application Service:** Handles the main business logic.
    * **Auth Service:** login, roles and permissions
    * **Hotel and Room Service:** hotel and rooms listing, room crud
    * **Booking Service:** room availability, booking management
    * **Notification Service:** email sending
* **Web Client:** handles ui logic, sends and fetches data from backend
* **Scheduler:** handles triggering of delayed notifications about bookings
* **Database:** persists users, permissions, hotels, rooms, bookings

### 3.3 Sequence diagram

See [Sequence Diagram](uml/sequence_diagram.puml)

---

## 4. Data Design

See [Data model document](data-model.md)

### 4.1 Storage

* **Relational Database:** Stores user profiles, hotel and room info, booking info,
  permissions
* **Cache Layer:** Caches filtered requests of hotels and rooms to reduce database load.

---

### AI usage note:

Initial skeleton was generated with LLM. **System Architecture** block was edited with
LLM.