/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.edu.ukma.ssdm.teamspectrum.logic.hotel;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record HotelRecord(
    UUID id,
    String name,
    HotelAddress address,
    String pictureUrl,
    BigDecimal pricePerNight,
    double rating,
    Set<String> amenities) {
  public HotelRecord {
    amenities = Set.copyOf(amenities);
  }
}
