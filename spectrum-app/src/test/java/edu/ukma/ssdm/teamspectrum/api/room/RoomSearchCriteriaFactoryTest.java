/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.room;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.ukma.ssdm.teamspectrum.api.common.page.PaginationParams;
import edu.ukma.ssdm.teamspectrum.logic.common.SortOrder;
import edu.ukma.ssdm.teamspectrum.logic.room.RoomSearchCriteria;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Tag("unit")
class RoomSearchCriteriaFactoryTest {
  private final RoomSearchCriteriaFactory factory =
      new RoomSearchCriteriaFactory(new RoomFactory());

  @Test
  void mapsGetRoomsParamsNullSortFieldsToDefaultValues() {
    GetRoomsParams params =
        new GetRoomsParams(null, null, null, null, null, null, null, null, null);
    PaginationParams pagination = new PaginationParams(2, 20);

    RoomSearchCriteria criteria = factory.create(params, pagination);

    assertEquals(
        new RoomSearchCriteria(
            null,
            null,
            null,
            null,
            null,
            null,
            Set.of(),
            edu.ukma.ssdm.teamspectrum.logic.room.RoomSortBy.PRICE_PER_NIGHT,
            SortOrder.ASC,
            2,
            20),
        criteria);
  }

  @ParameterizedTest
  @MethodSource("sortOrders")
  void mapsGetRoomsSortOrder(
      edu.ukma.ssdm.teamspectrum.api.common.SortOrder sortOrder, SortOrder expected) {
    assertEquals(expected, factory.mkSortOrder(sortOrder));
  }

  static Stream<Arguments> sortOrders() {
    return Stream.of(
        Arguments.of(edu.ukma.ssdm.teamspectrum.api.common.SortOrder.Asc, SortOrder.ASC),
        Arguments.of(edu.ukma.ssdm.teamspectrum.api.common.SortOrder.Desc, SortOrder.DESC));
  }

  @ParameterizedTest
  @MethodSource("sortFields")
  void mapsGetRoomsSortBy(
      RoomSortBy sortBy, edu.ukma.ssdm.teamspectrum.logic.room.RoomSortBy expected) {
    assertEquals(expected, factory.mkSortBy(sortBy));
  }

  static Stream<Arguments> sortFields() {
    return Stream.of(
        Arguments.of(
            RoomSortBy.RoomNumber, edu.ukma.ssdm.teamspectrum.logic.room.RoomSortBy.ROOM_NUMBER),
        Arguments.of(
            RoomSortBy.PricePerNight,
            edu.ukma.ssdm.teamspectrum.logic.room.RoomSortBy.PRICE_PER_NIGHT));
  }

  @Test
  void mapsGetRoomsParamsToRoomSearchCriteria() {
    GetRoomsParams params =
        new GetRoomsParams(
            RoomType.DOUBLE,
            RoomStatus.INACTIVE,
            LocalDate.parse("2027-06-15"),
            LocalDate.parse("2027-06-20"),
            new BigDecimal("50.00"),
            new BigDecimal("200.00"),
            Set.of("wifi", "parking"),
            RoomSortBy.RoomNumber,
            edu.ukma.ssdm.teamspectrum.api.common.SortOrder.Desc);
    PaginationParams pagination = new PaginationParams(3, 25);

    RoomSearchCriteria criteria = factory.create(params, pagination);

    assertEquals(
        new RoomSearchCriteria(
            edu.ukma.ssdm.teamspectrum.logic.room.RoomType.DOUBLE,
            edu.ukma.ssdm.teamspectrum.logic.room.RoomStatus.INACTIVE,
            LocalDate.parse("2027-06-15"),
            LocalDate.parse("2027-06-20"),
            new BigDecimal("50.00"),
            new BigDecimal("200.00"),
            Set.of("wifi", "parking"),
            edu.ukma.ssdm.teamspectrum.logic.room.RoomSortBy.ROOM_NUMBER,
            SortOrder.DESC,
            3,
            25),
        criteria);
  }
}
