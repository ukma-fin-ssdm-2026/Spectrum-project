/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record Hotel(
    @Schema(examples = {idExample}) UUID hotelId,
    @Schema(examples = {nameExample}) String name,
    Address address,
    @Schema(examples = {pictureUrlExample}) String pictureUrl,
    @Schema(examples = {pricePerNightExample}) BigDecimal pricePerNight,
    @DecimalMin("0.0") @DecimalMax("5.0") @Schema(examples = {ratingExample}) double rating,
    @Schema(examples = {amenitiesExample}) Set<String> amenities) {
  public static final String idExample = "123e4567-e89b-12d3-a456-426614174000";
  static final String nameExample = "Grand Budapest";
  static final String pictureUrlExample = "https://example.com/hotels/grand-budapest.jpg";
  static final String pricePerNightExample = "145.00";
  static final String ratingExample = "4.8";
  static final String amenitiesExample = "Wi-Fi";

  public Hotel {
    amenities = Set.copyOf(amenities);
  }
}
