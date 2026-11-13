# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this
project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.1] - 2026-11-14

### Refactoring
- Replaced the `if`/`else if` chain on the parameter type in `EnumParamConverterProvider.getConverter` with a lookup in a map of supported enums (`Substitute Algorithm`, `Replace Conditional with Lookup Table`). (see [EnumParamConverterProvider](spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/common/param/EnumParamConverterProvider.java))
- `RoomFactory.toLogic(CreateRoom)` now reuses the existing type, price and status converters instead of repeating them inline; the "no fields in update" check and its error were extracted from `toLogic(UpdateRoom)` (`Replace Inline Code with Function Call`, `Decompose Conditional`, `Extract Method`). (see [RoomFactory](spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/room/RoomFactory.java))

## [0.2.0] - 2026-11-07

### Added
- Query and transaction timeouts to db calls
- Retries with backoff for timeout exceptions
- Timeouts and retry are provided through config
- Config is passed as type safe dependency and is initialized at application start

## [0.1.1] - 2026-10-31

### Refactoring
- Moved hotel amenity retrieval out of `toHotels` and passed the data into the mapper (`Separating Data Retrieval from Transformation`, `Add Parameter`). (see [HotelRepositoryImpl](spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/hotel/HotelRepositoryImpl.java))
- Replaced `HotelAlreadyExistsException` handling with a sealed result and handled the response in the controller (`Inline Class`, `Exception to Sealed Result`). (see [HotelController](spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/hotel/HotelController.java))
- From `RoomRepositoryImpl.update` extracted transaction, room number check, and amenity handling helpers (`Extract Method`, `Decompose Conditional`, `Replace Nested Conditional`). (see [RoomRepositoryImpl](spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/room/RoomRepositoryImpl.java))
- Moved the jOOQ code generation database password from `pom.xml` to the `DB_PASSWORD` environment variable (`Externalize Configuration`).

[0.2.1]: https://github.com/ukma-fin-ssdm-2026/Spectrum-project/compare/v0.2.0...v0.2.1
[0.2.0]: https://github.com/ukma-fin-ssdm-2026/Spectrum-project/compare/v0.1.1...v0.2.0
[0.1.1]: https://github.com/ukma-fin-ssdm-2026/Spectrum-project/releases/tag/v0.1.1
