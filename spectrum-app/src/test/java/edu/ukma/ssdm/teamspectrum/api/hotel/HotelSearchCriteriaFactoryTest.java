/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.ukma.ssdm.teamspectrum.api.common.page.PaginationParams;
import edu.ukma.ssdm.teamspectrum.logic.common.SortOrder;
import edu.ukma.ssdm.teamspectrum.logic.hotel.HotelSearchCriteria;
import edu.ukma.ssdm.teamspectrum.logic.hotel.HotelSortBy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class HotelSearchCriteriaFactoryTest {
  private final HotelSearchCriteriaFactory factory = new HotelSearchCriteriaFactory();

  @Test
  void mapsGetHotelsParamsNullSortFieldsToDefaultValues() {
    GetHotelsParams params = new GetHotelsParams(null, null, null, null, null, null, null, null);
    PaginationParams pagination = new PaginationParams(2, 20);

    HotelSearchCriteria criteria = factory.create(params, pagination);

    assertEquals(
        new HotelSearchCriteria(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            2,
            20,
            HotelSortBy.RATING,
            SortOrder.DESC),
        criteria);
  }

  @Test
  void mapsGetHotelsSortOrder() {
    assertEquals(
        SortOrder.ASC, factory.mkSortOrder(edu.ukma.ssdm.teamspectrum.api.common.SortOrder.Asc));
    assertEquals(
        SortOrder.DESC, factory.mkSortOrder(edu.ukma.ssdm.teamspectrum.api.common.SortOrder.Desc));
  }

  @Test
  void mapsGetHotelsSortBy() {
    assertEquals(
        HotelSortBy.RATING,
        factory.mkSortBy(edu.ukma.ssdm.teamspectrum.api.hotel.HotelSortBy.Rating));
    assertEquals(
        HotelSortBy.PRICE_PER_NIGHT,
        factory.mkSortBy(edu.ukma.ssdm.teamspectrum.api.hotel.HotelSortBy.PricePerNight));
  }

  @Test
  void mapsGetHotelsParamsToHotelSearchCriteria() {
    GetHotelsParams params =
        new GetHotelsParams(
            "Kyiv",
            LocalDate.parse("2026-11-01"),
            LocalDate.parse("2026-11-05"),
            new BigDecimal("50.00"),
            new BigDecimal("200.00"),
            3.5,
            5.0,
            List.of("wifi", "parking"));
    PaginationParams pagination = new PaginationParams(2, 20);

    HotelSearchCriteria criteria = factory.create(params, pagination);

    assertEquals(
        new HotelSearchCriteria(
            "Kyiv",
            LocalDate.parse("2026-11-01"),
            LocalDate.parse("2026-11-05"),
            new BigDecimal("50.00"),
            new BigDecimal("200.00"),
            3.5,
            5.0,
            Set.of("wifi", "parking"),
            2,
            20,
            HotelSortBy.RATING,
            SortOrder.DESC),
        criteria);
  }
}
