# Threat Model (STRIDE)

Go back to the [Main README](../../README.md).

The analysis is done over the [Deployment Diagram](uml/deployment_diagram.puml).
Requirement IDs refer to [Requirements](../requirements/requirements.md).

## 1. Assets

| Asset                                    | Where stored          | Sensitivity                  |
|------------------------------------------|-----------------------|------------------------------|
| Client personal data (name, email)       | Postgres              | Personal data                |
| Password hashes                          | Postgres              | Sensitive                    |
| Bookings (who stays where and when)      | Postgres              | Personal data                |
| Worker permission grants                 | Postgres              | Sensitive (controls access)  |
| Session tokens                           | Client App, Server    | Sensitive                    |
| Cached search results                    | Valkey                | Public (no personal data)    |
| Room pictures                            | S3 File Storage       | Public                       |
| Email provider and S3 API keys           | Server configuration  | Secret                       |

## 2. Trust boundaries

| ID   | Boundary                               | Connection                        |
|------|----------------------------------------|-----------------------------------|
| TB-1 | Internet ↔ DMZ                         | Client App → Nginx (HTTPS)        |
| TB-2 | DMZ ↔ Application zone                 | Nginx → Server App (HTTP)         |
| TB-3 | Application zone ↔ Data zone           | Server App → Postgres, Valkey     |
| TB-4 | System ↔ External providers            | Server App → Email, Nginx → S3    |

## 3. Identified risks

Rating: High / Medium / Low, based on likelihood and impact.

| ID   | Component / boundary      | STRIDE                 | Scenario                                                                                                          | Rating | Countermeasure / decision                                                                                                    |
|------|---------------------------|------------------------|-------------------------------------------------------------------------------------------------------------------|--------|------------------------------------------------------------------------------------------------------------------------------|
| T-01 | TB-1, login endpoint      | Spoofing               | An attacker guesses or reuses leaked passwords (credential stuffing) to log in as a client or worker.             | High   | Rate limiting of login requests in Nginx; passwords stored as salted hashes (SEC-002).                                        |
| T-02 | Client App, session token | Spoofing               | A session token is stolen (e.g. via XSS) and used to act as the victim.                                           | High   | Token in an `HttpOnly`, `Secure`, `SameSite` cookie with a short lifetime; output escaping on the client.                     |
| T-03 | TB-1                      | Tampering              | A request or response is changed in transit on a public network (man-in-the-middle).                              | Medium | HTTPS only on the gateway, HTTP redirected to HTTPS, HSTS enabled.                                                            |
| T-04 | Server App, booking API   | Tampering              | A client changes the price or room ID in the booking request body.                                                | Medium | The server takes the price and availability only from the database, never from the request.                                   |
| T-05 | Server App, booking API   | Repudiation            | A worker cancels a booking and later denies it, or a client denies making a booking.                              | Medium | Every booking status change is logged with user ID and timestamp; the log is append-only.                                     |
| T-06 | TB-3, Postgres            | Information disclosure | SQL injection through search or filter parameters exposes clients' personal data.                                 | High   | Parameterized queries / ORM only; the database user of the server has only the rights it needs.                              |
| T-07 | Server App, bookings API  | Information disclosure | A user changes a booking ID in the URL and sees another client's booking.                                         | High   | Every request checks that the booking belongs to the client or to a worker of that hotel (SEC-001).                           |
| T-08 | TB-1, search endpoint     | Denial of service      | Many heavy search requests overload the server and database, so search stops meeting PERF-001.                   | Medium | Rate limiting per IP in Nginx, results cached in Valkey, pagination with a maximum page size.                                 |
| T-09 | Server App, room API      | Elevation of privilege | A worker with a grant for hotel A changes IDs in requests to edit or delete rooms of hotel B.                     | High   | Every room and booking operation is checked against the worker's `PermissionGrant`; deny by default (SEC-003).               |
| T-10 | TB-2                      | Information disclosure | Traffic between Nginx and the Server App is plain HTTP and could be read inside the private network.             | Low    | **Accepted risk:** both run in the same private network with no public access. Revisit if they move to separate hosts.        |

## 4. Summary

- STRIDE coverage: Spoofing (T-01, T-02), Tampering (T-03, T-04), Repudiation (T-05),
  Information disclosure (T-06, T-07, T-10), Denial of service (T-08), Elevation of
  privilege (T-09).
- High risks (T-01, T-02, T-06, T-07, T-09) will be checked in practice in Lab 5
  (security analyzer) and Lab 8 (Security Rating).
- T-10 is accepted consciously; all other risks have a planned countermeasure.

### AI usage note:

The threat model was drafted with an LLM (Claude) from the deployment diagram, the data
model and the requirements. Verification: reviewed by the team in the pull request before
merging.
