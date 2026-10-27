/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.room;

import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.Amenities.AMENITIES;
import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.Hotels.HOTELS;
import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.RoomAmenities.ROOM_AMENITIES;
import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.Rooms.ROOMS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ukma.ssdm.teamspectrum.logic.common.SortOrder;
import edu.ukma.ssdm.teamspectrum.logic.common.repository.RepositoryTestProfile;
import edu.ukma.ssdm.teamspectrum.logic.hotel.HotelAddress;
import edu.ukma.ssdm.teamspectrum.logic.hotel.HotelRecord;
import edu.ukma.ssdm.teamspectrum.logic.hotel.HotelRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.javamoney.moneta.Money;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@TestProfile(RepositoryTestProfile.class)
class RoomRepositoryTest {
  private static final String ROOM_NUMBER_101 = "101";
  private static final String ROOM_NUMBER_102 = "102";
  private static final String ROOM_NUMBER_201 = "201";
  private static final String ROOM_NUMBER_301 = "301";
  private static final String AMENITY_POOL = "pool";
  private static final Set<String> WIFI_AMENITY = Set.of("wifi");
  private static final Set<String> WIFI_AND_POOL_AMENITIES = Set.of("wifi", AMENITY_POOL);
  private static final Set<String> POOL_AND_SAUNA_AMENITIES = Set.of(AMENITY_POOL, "sauna");
  private static final Set<String> NO_AMENITIES = Set.of();
  private static final BigDecimal PRICE_100 = BigDecimal.valueOf(100);
  private static final BigDecimal PRICE_125 = BigDecimal.valueOf(125);
  private static final BigDecimal PRICE_125_50 = new BigDecimal("125.50");
  private static final BigDecimal PRICE_150 = BigDecimal.valueOf(150);
  private static final BigDecimal PRICE_200 = BigDecimal.valueOf(200);
  private static final int FIRST_PAGE = 0;
  private static final int FIRST_ROOM_PER_PAGE = 1;
  private static final int PAGE_SIZE = 10;

  @Inject RoomRepository repository;
  @Inject HotelRepository hotelRepository;
  @Inject DSLContext dsl;

  @BeforeEach
  @AfterEach
  void cleanUp() {
    dsl.deleteFrom(ROOM_AMENITIES).execute();
    dsl.deleteFrom(ROOMS).execute();
    dsl.deleteFrom(HOTELS).execute();
    dsl.deleteFrom(AMENITIES).execute();
  }

  @Test
  void createReturnsInsertedRoom() {
    var hotel = hotel();
    var request =
        room(ROOM_NUMBER_101, RoomType.DOUBLE, PRICE_125_50, RoomStatus.ACTIVE, WIFI_AMENITY);
    hotelRepository.add(hotel);

    var result = repository.create(hotel.id(), request);

    assertEquals(
        new RoomMutationResult.Success(expectedRoom(hotel.id(), request, WIFI_AMENITY)), result);
  }

  @Test
  void createReturnsRoomNumberConflictWhenNumberAlreadyExistsForHotel() {
    var hotel = hotel();
    hotelRepository.add(hotel);
    repository.create(
        hotel.id(),
        room(ROOM_NUMBER_101, RoomType.SINGLE, PRICE_100, RoomStatus.ACTIVE, NO_AMENITIES));

    var result =
        repository.create(
            hotel.id(),
            room(ROOM_NUMBER_101, RoomType.DOUBLE, PRICE_150, RoomStatus.INACTIVE, NO_AMENITIES));

    assertEquals(new RoomMutationResult.RoomNumberConflict(), result);
  }

  @Test
  void createReturnsNotFoundWhenHotelDoesNotExist() {
    var request =
        room(ROOM_NUMBER_101, RoomType.DOUBLE, PRICE_125_50, RoomStatus.ACTIVE, NO_AMENITIES);

    assertEquals(new RoomMutationResult.NotFound(), repository.create(UUID.randomUUID(), request));
  }

  @Test
  void updateReturnsUpdatedRoomAndReplacesAmenities() {
    var hotel = hotel();
    hotelRepository.add(hotel);
    var original =
        room(ROOM_NUMBER_101, RoomType.SINGLE, PRICE_100, RoomStatus.ACTIVE, WIFI_AMENITY);
    repository.create(hotel.id(), original);
    var update =
        new UpdateRoomCommand(
            ROOM_NUMBER_102,
            RoomType.SUITE,
            Money.of(new BigDecimal("250.75"), "EUR"),
            RoomStatus.INACTIVE,
            POOL_AND_SAUNA_AMENITIES);

    var result = repository.update(hotel.id(), original.roomId(), update);

    var expected =
        new RoomRecord(
            original.roomId(),
            hotel.id(),
            ROOM_NUMBER_102,
            RoomType.SUITE,
            update.pricePerNight(),
            RoomStatus.INACTIVE,
            POOL_AND_SAUNA_AMENITIES);
    assertEquals(new RoomMutationResult.Success(expected), result);
  }

  @Test
  void updateReturnsNotFoundWhenRoomDoesNotExist() {
    var hotel = hotel();
    hotelRepository.add(hotel);

    var result =
        repository.update(
            hotel.id(),
            UUID.randomUUID(),
            new UpdateRoomCommand(ROOM_NUMBER_101, null, null, null, null));

    assertEquals(new RoomMutationResult.NotFound(), result);
  }

  @Test
  void updateReturnsNotFoundWhenHotelDoesNotExist() {
    var result =
        repository.update(
            UUID.randomUUID(),
            UUID.randomUUID(),
            new UpdateRoomCommand(ROOM_NUMBER_101, null, null, null, null));

    assertEquals(new RoomMutationResult.NotFound(), result);
  }

  @Test
  void updateReturnsRoomNumberConflictWhenAnotherRoomUsesRequestedNumber() {
    var hotel = hotel();
    hotelRepository.add(hotel);
    var first = room(ROOM_NUMBER_101, RoomType.SINGLE, PRICE_100, RoomStatus.ACTIVE, NO_AMENITIES);
    var second = room(ROOM_NUMBER_102, RoomType.DOUBLE, PRICE_150, RoomStatus.ACTIVE, NO_AMENITIES);
    repository.create(hotel.id(), first);
    repository.create(hotel.id(), second);

    var result =
        repository.update(
            hotel.id(),
            second.roomId(),
            new UpdateRoomCommand(ROOM_NUMBER_101, null, null, null, null));

    assertEquals(new RoomMutationResult.RoomNumberConflict(), result);
  }

  @Test
  void findPageReturnsMatchingRoomsAndTotalCount() {
    var hotel = hotel();
    hotelRepository.add(hotel);
    var first =
        room(
            ROOM_NUMBER_101,
            RoomType.DOUBLE,
            PRICE_125,
            RoomStatus.ACTIVE,
            WIFI_AND_POOL_AMENITIES);
    var second =
        room(
            ROOM_NUMBER_102,
            RoomType.DOUBLE,
            PRICE_150,
            RoomStatus.ACTIVE,
            WIFI_AND_POOL_AMENITIES);
    var excluded =
        room("103", RoomType.SINGLE, PRICE_125, RoomStatus.ACTIVE, WIFI_AND_POOL_AMENITIES);
    repository.create(hotel.id(), first);
    repository.create(hotel.id(), second);
    repository.create(hotel.id(), excluded);
    var criteria =
        new RoomSearchCriteria(
            RoomType.DOUBLE,
            RoomStatus.ACTIVE,
            null,
            null,
            PRICE_100,
            PRICE_200,
            WIFI_AND_POOL_AMENITIES,
            RoomSortBy.RoomNumber,
            SortOrder.Asc,
            FIRST_PAGE,
            FIRST_ROOM_PER_PAGE);

    var result = repository.findPage(hotel.id(), criteria, FIRST_PAGE, FIRST_ROOM_PER_PAGE);

    assertEquals(
        new RoomPage(List.of(expectedRoom(hotel.id(), first, WIFI_AND_POOL_AMENITIES)), 2), result);
  }

  @ParameterizedTest
  @MethodSource("sortingCases")
  void findPageSortsByRequestedFieldAndOrder(
      RoomSortBy sortBy, SortOrder order, List<String> expectedRoomNumbers) {
    var hotel = hotel();
    hotelRepository.add(hotel);
    repository.create(
        hotel.id(),
        room(
            ROOM_NUMBER_201,
            RoomType.DOUBLE,
            BigDecimal.valueOf(300),
            RoomStatus.ACTIVE,
            NO_AMENITIES));
    repository.create(
        hotel.id(),
        room(ROOM_NUMBER_101, RoomType.SINGLE, PRICE_100, RoomStatus.ACTIVE, NO_AMENITIES));
    repository.create(
        hotel.id(),
        room(ROOM_NUMBER_301, RoomType.SUITE, PRICE_200, RoomStatus.ACTIVE, NO_AMENITIES));

    var result = repository.findPage(hotel.id(), criteria(sortBy, order), FIRST_PAGE, PAGE_SIZE);

    assertEquals(expectedRoomNumbers, result.items().stream().map(RoomRecord::roomNumber).toList());
  }

  private static Stream<Arguments> sortingCases() {
    return Stream.of(
        Arguments.of(
            RoomSortBy.RoomNumber,
            SortOrder.Asc,
            List.of(ROOM_NUMBER_101, ROOM_NUMBER_201, ROOM_NUMBER_301)),
        Arguments.of(
            RoomSortBy.RoomNumber,
            SortOrder.Desc,
            List.of(ROOM_NUMBER_301, ROOM_NUMBER_201, ROOM_NUMBER_101)),
        Arguments.of(
            RoomSortBy.PricePerNight,
            SortOrder.Asc,
            List.of(ROOM_NUMBER_101, ROOM_NUMBER_301, ROOM_NUMBER_201)),
        Arguments.of(
            RoomSortBy.PricePerNight,
            SortOrder.Desc,
            List.of(ROOM_NUMBER_201, ROOM_NUMBER_301, ROOM_NUMBER_101)));
  }

  @Test
  void findPageReturnsEmptyPageWhenNoRoomsMatch() {
    var hotel = hotel();
    hotelRepository.add(hotel);

    var result = repository.findPage(hotel.id(), criteria(), FIRST_PAGE, PAGE_SIZE);

    assertEquals(new RoomPage(List.of(), 0), result);
  }

  @Test
  void findByIdReturnsRoomWithAmenities() {
    var hotel = hotel();
    hotelRepository.add(hotel);
    var request =
        room(ROOM_NUMBER_101, RoomType.DOUBLE, PRICE_125_50, RoomStatus.ACTIVE, WIFI_AMENITY);
    repository.create(hotel.id(), request);

    assertEquals(
        Optional.of(expectedRoom(hotel.id(), request, WIFI_AMENITY)),
        repository.findById(hotel.id(), request.roomId()));
  }

  @Test
  void findByIdReturnsEmptyWhenRoomDoesNotExist() {
    assertTrue(repository.findById(UUID.randomUUID(), UUID.randomUUID()).isEmpty());
  }

  private HotelRecord hotel() {
    return new HotelRecord(
        UUID.randomUUID(),
        "Hotel " + UUID.randomUUID(),
        new HotelAddress("Kyiv", "Podil", "Main Street", 1),
        null,
        PRICE_100,
        4.5,
        NO_AMENITIES);
  }

  private static CreateRoomCommand room(
      String number, RoomType type, BigDecimal price, RoomStatus status, Set<String> amenities) {
    return new CreateRoomCommand(
        UUID.randomUUID(), number, type, Money.of(price, "USD"), status, amenities);
  }

  private static RoomRecord expectedRoom(
      UUID hotelId, CreateRoomCommand request, Set<String> amenities) {
    return new RoomRecord(
        request.roomId(),
        hotelId,
        request.roomNumber(),
        request.roomType(),
        request.pricePerNight(),
        request.roomStatus(),
        amenities);
  }

  private static RoomSearchCriteria criteria() {
    return new RoomSearchCriteria(
        null, null, null, null, null, null, NO_AMENITIES, null, null, FIRST_PAGE, PAGE_SIZE);
  }

  private static RoomSearchCriteria criteria(RoomSortBy sortBy, SortOrder order) {
    return new RoomSearchCriteria(
        null, null, null, null, null, null, NO_AMENITIES, sortBy, order, FIRST_PAGE, PAGE_SIZE);
  }
}
