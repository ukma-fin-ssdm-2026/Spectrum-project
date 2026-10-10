/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record Hotel(
    UUID hotelId,
    @Schema(examples = {nameExample}) String name,
    Address address,
    String pictureUrl,
    BigDecimal pricePerNight,
    double rating,
    Set<String> amenities) {
  static final String nameExample = "Grand Budapest";

  public Hotel {
    amenities = Set.copyOf(amenities);
  }
}
