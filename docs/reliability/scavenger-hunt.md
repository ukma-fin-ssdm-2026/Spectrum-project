## Scavenger hunt

### UserRepositoryImpl

No timeout set for any query in `UserRepositoryImpl`

| Field        | Explanation                                                                                                          |
|--------------|----------------------------------------------------------------------------------------------------------------------|
| **Fault**    | [UserRepositoryImpl](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/auth/UserRepositoryImpl.java) |
| **Error**    | Query potentially blocks indefinitely                                                                                |
| **Failure**  | 408 or 504 error code                                                                                                |
| **Severity** | High                                                                                                                 |

### HotelRepositoryImpl

No timeout set for any query in `HotelRepositoryImpl`

| Field        | Explanation                                                                                                             |
|--------------|-------------------------------------------------------------------------------------------------------------------------|
| **Fault**    | [HotelRepositoryImpl](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/hotel/HotelRepositoryImpl.java) |
| **Error**    | Query potentially blocks indefinitely                                                                                   |
| **Failure**  | 408 or 504 error code                                                                                                   |
| **Severity** | High                                                                                                                    |

### RoomRepositoryImpl

No timeout set for any query in `RoomRepositoryImpl`

| Field        | Explanation                                                                                                          |
|--------------|----------------------------------------------------------------------------------------------------------------------|
| **Fault**    | [RoomRepositoryImpl](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/room/RoomRepositoryImpl.java) |
| **Error**    | Query potentially blocks indefinitely                                                                                |
| **Failure**  | 408 or 504 error code                                                                                                |
| **Severity** | High                                                                                                                 |

### GetRoomsParams

No validation for enums in `GetRoomsParams`

| Field        | Explanation                                                                                                |
|--------------|------------------------------------------------------------------------------------------------------------|
| **Fault**    | [GetRoomsParams](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/room/GetRoomsParams.java) |
| **Error**    | Enum types are not checked                                                                                 |
| **Failure**  | wrong error code for invalid value(400), undefined behaviour of system                                     |
| **Severity** | High                                                                                                       |
