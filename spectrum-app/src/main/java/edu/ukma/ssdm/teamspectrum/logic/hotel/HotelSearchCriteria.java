/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.hotel;

import edu.ukma.ssdm.teamspectrum.logic.common.SortOrder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record HotelSearchCriteria(
    String location,
    LocalDate checkIn,
    LocalDate checkOut,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Double minRating,
    Double maxRating,
    Set<String> amenities,
    int page,
    int limit,
    HotelSortBy sortBy,
    SortOrder order) {
  public HotelSearchCriteria {
    amenities = amenities == null ? Set.of() : Set.copyOf(amenities);
  }

  public HotelSearchCriteria(
      String location,
      LocalDate checkIn,
      LocalDate checkOut,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      Double minRating,
      Double maxRating,
      Set<String> amenities,
      int page,
      int limit) {
    this(
        location, checkIn, checkOut, minPrice, maxPrice, minRating, maxRating, amenities, page,
        limit, null, null);
  }
}
