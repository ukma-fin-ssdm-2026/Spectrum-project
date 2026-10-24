# Requirements Traceability Matrix (RTM)

Go back to the [Main README](../../README.md).

Links each user story to its functional requirements, non-functional requirements and the
test cases that will verify it. Sources: [User stories](user-stories.md),
[Requirements](requirements.md).

| Story  | Type | Description                             | Related FR             | Related NFR | Test cases                                                                                               |
|--------|------|-----------------------------------------|------------------------|-------------|----------------------------------------------------------------------------------------------------------|
| US-001 | FR   | Client sees available hotels for dates  | FR-008                 | PERF-001    | Test-01: search returns only hotels with free rooms; Test-02: search ≤ 2 s, 20 users; TP-10              |
| US-002 | FR   | Client filters the hotel list           | FR-009, FR-010, FR-011 | PERF-001    | Test-03: combined filters use AND logic; Test-02: filtered search ≤ 2 s; TP-11                           |
| US-003 | FR   | Client opens a hotel's page             | FR-012                 | —           | Test-04: hotel page shows name, photos, price, rating, address                                           |
| US-004 | FR   | Filters persist during the session      | FR-009, FR-010, FR-011 | —           | Test-05: filters kept after refresh, reset after new session                                             |
| US-005 | FR   | Client sees available rooms in a hotel  | FR-012                 | —           | Test-06: room list shows only rooms free for the selected dates; TP-12                                   |
| US-006 | FR   | Worker creates a room                   | FR-001                 | SEC-003     | Test-07: invalid fields rejected; Test-08: worker without grant gets `403`; TP-5, TP-6, TP-7, TP-8, TP-9 |
| US-007 | FR   | Worker views the hotel's rooms          | FR-017                 | —           | Test-09: search and filters return matching rooms only                                                   |
| US-008 | FR   | Worker views and manages room details   | FR-005, FR-006, FR-007 | SEC-003     | Test-10: deactivated room hidden from clients; Test-08: `403` without grant                              |
| US-009 | FR   | Worker edits a room                     | FR-002, FR-003         | SEC-003     | Test-11: occupied room cannot be edited; Test-08: `403` without grant                                    |
| US-010 | FR   | Worker deletes a room                   | FR-004                 | SEC-003     | Test-12: booked or occupied room cannot be deleted; Test-08: `403` without grant                         |
| US-011 | FR   | Client books a room                     | FR-013, FR-015         | REL-001     | Test-13: 100 concurrent bookings of same room and dates → 1 booking; Test-14: email sent                 |
| US-012 | FR   | Client is notified about a cancellation | FR-007, FR-016         | SEC-001     | Test-15: cancellation email sent; Test-16: client sees only own bookings                                 |
| US-013 | FR   | Visitor registers an account            | FR-019, FR-020         | SEC-002     | TP-1, TP-2                                                                                               |
| US-014 | FR   | Registered user logs in                 | FR-021, FR-022         | SEC-002     | TP-3, TP-4                                                                                               |

## Gaps

- Stories without an NFR: US-003, US-004, US-005, US-007.
- NFRs without a story: none. SEC-002 (password hashing) is related to US-013 and US-014;
  planned test: Test-17: stored passwords are hashes, not plain text.
- FRs without a story: FR-014 (rate a hotel), FR-018 (manage workers' permissions).

### AI usage note:

The matrix and test case names were drafted with an LLM (Claude) from the user stories and
requirements. Verification: reviewed by the team in the pull request before merging.
