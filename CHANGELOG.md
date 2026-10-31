## [0.1.1] - <2026-10.31>
### Refactoring:
- Moved hotel amenity retrieval out of `toHotels` and passed the data into the mapper (`Separating Data Retrieval from Transformation`, `Add Parameter`). (see [HotelRepositoryImp](spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/hotel/HotelRepositoryImpl.java))
- Replaced `HotelAlreadyExistsException` handling with a sealed result and handled the response in the controller (`Inline Class`, `Exception to Sealed Result`). (see [HotelController](spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/hotel/HotelController.java))
- From `RoomRepositoryImpl.update` extracted transaction, room number check, and amenity handling helpers (`Extract Method`, `Decompose Conditional`, `Replace Nested Conditional`). (see [RoomRepositoryImpl](spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/room/RoomRepositoryImpl.java))
- Provide db password through env for build
