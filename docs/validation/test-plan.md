## Test plan

Go back to the [Main README](../../README.md).

### Volume

- Auth Service
- Hotels and Rooms Service

As it is the only functionality currently implemented

### Exit criteria

- all tests pass(unit and integration in CI/CD, system and acceptance for corresponding module)
- integration tests should be rerun a few times to avoid instability(>= 3 times)
- coverage should stay at 60% at least

### Risks

- Auth Service - as a main gateway to system should work impeccable, because any
  instability would prevent any usage of our system
- PermissionGrant management - as it may result in devastating security breach with user data corrupted

| №     | Component/function                      | Test level  | Type (positive/negative) | 	Expected result / acceptance criteria                                                   | Owner      | Requirement |
|-------|-----------------------------------------|-------------|--------------------------|------------------------------------------------------------------------------------------|------------|-------------|
| TP-1  | Auth Service/register                   | Integration | Positive                 | User registers with valid credentials                                                    | @Demon4ort | FR-019      |
| TP-2  | Auth Service/register                   | Integration | Negative                 | User registers with invalid credentials                                                  | @Demon4ort | FR-020      |
| TP-3  | Auth Service/login                      | System      | Positive                 | User logs in with valid credentials                                                      | @Demon4ort | FR-021      |
| TP-4  | Auth Service/login                      | System      | Negative                 | User unable to log in with invalid credentials                                           | @Demon4ort | FR-022      |
| TP-5  | Auth Service/PermissionGrant management | Unit        | Positive                 | Worker can create a room in hotel having valid PermissionGrant                           | @Demon4ort | 6           |
| TP-6  | Auth Service/PermissionGrant management | Unit        | Negative                 | Worker can't create a room in hotel having invalid PermissionGrant                       | @Demon4ort | SEC-003     |
| TP-7  | Auth Service/PermissionGrant management | Integration | Positive                 | Worker can create a room in hotel having valid PermissionGrant                           | @Demon4ort | 6           |
| TP-8  | Auth Service/PermissionGrant management | System      | Positive                 | Worker can create a room in hotel after receiving valid PermissionGrant by Administrator | @Demon4ort | 6           |
| TP-9  | Auth Service/PermissionGrant management | Integration | Negative                 | Worker can't create a room in hotel having invalid PermissionGrant                       | @Demon4ort | SEC-003     |
| TP-10 | Hotels and Rooms Service/hotel view     | Integration | Positive                 | User can view hotels for selected date and location                                      | @Demon4ort | 1           |
| TP-11 | Hotels and Rooms Service/hotel view     | Integration | Positive                 | User can filter hotels for selected date and location                                    | @Demon4ort | 2           |
| TP-12 | Hotels and Rooms Service/room view      | System      | Positive                 | User can view rooms after selecting hotel                                                | @Demon4ort | 5           |
