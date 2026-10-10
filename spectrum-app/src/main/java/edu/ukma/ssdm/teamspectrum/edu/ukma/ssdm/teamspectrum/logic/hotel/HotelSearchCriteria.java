/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.edu.ukma.ssdm.teamspectrum.logic.hotel;

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
    int limit) {
  public HotelSearchCriteria {
    amenities = amenities == null ? Set.of() : Set.copyOf(amenities);
  }
}
