/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.hotel;

import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.Amenities.AMENITIES;
import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.HotelAmenities.HOTEL_AMENITIES;
import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.Hotels.HOTELS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ukma.ssdm.teamspectrum.logic.common.SortOrder;
import edu.ukma.ssdm.teamspectrum.logic.common.repository.RepositoryTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@TestProfile(RepositoryTestProfile.class)
class HotelRepositoryImplTest {
  @Inject HotelRepository repository;
  @Inject DSLContext dsl;

  @BeforeEach
  @AfterEach
  void cleanUp() {
    dsl.deleteFrom(HOTEL_AMENITIES).execute();
    dsl.deleteFrom(HOTELS).execute();
    dsl.deleteFrom(AMENITIES).execute();
  }

  @Test
  void findByIdReturnsEmptyWhenHotelDoesNotExist() {
    assertEquals(Optional.empty(), repository.findById(UUID.randomUUID()));
  }

  @Test
  void addAndFindById() {
    HotelRecord hotel = hotel("Spectrum Hotel", "Kyiv", "Podil", 120, 4.7, Set.of("wifi", "pool"));

    repository.add(hotel);

    assertEquals(Optional.of(hotel), repository.findById(hotel.id()));
  }

  @Test
  void findByName() {
    HotelRecord hotel = hotel("Spectrum Hotel", "Kyiv", "Podil", 120, 4.7, Set.of("wifi"));
    repository.add(hotel);

    assertEquals(Optional.of(hotel), repository.findByName(" SpEctrum HotEl "));
  }

  @Test
  void findByNameReturnsEmptyForNullOrUnknownName() {
    repository.add(hotel("Spectrum Hotel", "Kyiv", "Podil", 120, 4.7, Set.of()));

    assertTrue(repository.findByName(null).isEmpty());
    assertTrue(repository.findByName("Unknown Hotel").isEmpty());
  }

  @Test
  void searchFiltersByLocationPriceRatingAndAllRequestedAmenities() {
    HotelRecord matching = hotel("Matching", "Kyiv", "Podil", 120, 4.7, Set.of("wifi", "pool"));
    repository.add(matching);
    repository.add(hotel("Wrong location", "Lviv", "Center", 120, 4.7, Set.of("wifi", "pool")));
    repository.add(hotel("Too cheap", "Kyiv", "Podil", 80, 4.7, Set.of("wifi", "pool")));
    repository.add(hotel("Low rated", "Kyiv", "Podil", 120, 3.2, Set.of("wifi", "pool")));
    repository.add(hotel("Missing amenity", "Kyiv", "Podil", 120, 4.7, Set.of("wifi")));
    HotelSearchCriteria criteria =
        criteria(
            " KYIV ",
            new BigDecimal("100"),
            new BigDecimal("150"),
            4.0,
            5.0,
            Set.of("wifi", "pool"));

    assertEquals(List.of(matching), repository.search(criteria, 0, 10));
    assertEquals(1, repository.count(criteria));
  }

  @Test
  void searchAppliesOffsetAndLimit() {
    HotelRecord first = hotel("First", "Kyiv", "Podil", 100, 3.0, Set.of());
    HotelRecord second = hotel("Second", "Kyiv", "Podil", 110, 4.0, Set.of());
    HotelRecord third = hotel("Third", "Kyiv", "Podil", 120, 5.0, Set.of());
    repository.add(first);
    repository.add(second);
    repository.add(third);
    HotelSearchCriteria criteria =
        new HotelSearchCriteria(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            Set.of(),
            0,
            10,
            HotelSortBy.PricePerNight,
            SortOrder.Asc);

    assertEquals(List.of(second), repository.search(criteria, 1, 1));
  }

  @ParameterizedTest
  @MethodSource("sortingCases")
  void searchSortsByRequestedFieldAndOrder(HotelSortBy sortBy, SortOrder order) {
    // TP-16: price order and rating order differ, so sorting by the wrong field fails the test
    HotelRecord low = hotel("Low price, high rating", "Kyiv", "Podil", 100, 5.0, Set.of());
    HotelRecord high = hotel("High price, low rating", "Kyiv", "Podil", 200, 3.0, Set.of());

    repository.add(low);
    repository.add(high);

    Comparator<HotelRecord> comparator =
        switch (sortBy) {
          case Rating -> Comparator.comparingDouble(HotelRecord::rating);
          case PricePerNight -> Comparator.comparing(HotelRecord::pricePerNight);
        };

    comparator =
        switch (order) {
          case Desc -> comparator.reversed();
          case Asc -> comparator;
        };

    List<String> expected = Stream.of(low, high).sorted(comparator).map(HotelRecord::name).toList();

    var actual =
        repository.search(sortedCriteria(sortBy, order), 0, 10).stream()
            .map(HotelRecord::name)
            .toList();

    assertEquals(expected, actual);
  }

  private static Stream<org.junit.jupiter.params.provider.Arguments> sortingCases() {
    return Stream.of(
        org.junit.jupiter.params.provider.Arguments.of(HotelSortBy.PricePerNight, SortOrder.Asc),
        org.junit.jupiter.params.provider.Arguments.of(HotelSortBy.PricePerNight, SortOrder.Desc),
        org.junit.jupiter.params.provider.Arguments.of(HotelSortBy.Rating, SortOrder.Asc),
        org.junit.jupiter.params.provider.Arguments.of(HotelSortBy.Rating, SortOrder.Desc));
  }

  @Test
  void countReturnsZeroWhenNoHotelsMatch() {
    assertEquals(0, repository.count(criteria("Kyiv", null, null, null, null, Set.of())));
  }

  private static HotelSearchCriteria criteria(
      String location,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      Double minRating,
      Double maxRating,
      Set<String> amenities) {
    return new HotelSearchCriteria(
        location, null, null, minPrice, maxPrice, minRating, maxRating, amenities, 0, 10);
  }

  private static HotelSearchCriteria sortedCriteria(HotelSortBy sortBy, SortOrder order) {
    return new HotelSearchCriteria(
        null, null, null, null, null, null, null, Set.of(), 0, 10, sortBy, order);
  }

  private static HotelRecord hotel(
      String name, String city, String district, int price, double rating, Set<String> amenities) {
    return new HotelRecord(
        UUID.randomUUID(),
        name,
        new HotelAddress(city, district, "Main Street", 1),
        "https://example.com/hotel.jpg",
        BigDecimal.valueOf(price),
        rating,
        amenities);
  }
}
