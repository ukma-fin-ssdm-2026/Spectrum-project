/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.room;

import edu.ukma.ssdm.teamspectrum.logic.common.SortOrder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record RoomSearchCriteria(
    RoomType roomType,
    RoomStatus roomStatus,
    LocalDate checkIn,
    LocalDate checkOut,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Set<String> amenities,
    RoomSortBy sortBy,
    SortOrder order,
    int page,
    int limit) {
  public RoomSearchCriteria {
    amenities = amenities == null ? Set.of() : Set.copyOf(amenities);
  }
}
