/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record GetHotelsParams(
    @QueryParam("page") @DefaultValue("0") @Min(0) int page,
    @QueryParam("limit") @DefaultValue("10") @Min(1) @Max(100) int limit,
    @QueryParam("location") String location,
    @QueryParam("checkIn") LocalDate checkIn,
    @QueryParam("checkOut") LocalDate checkOut,
    @QueryParam("minPrice") BigDecimal minPrice,
    @QueryParam("maxPrice") BigDecimal maxPrice,
    @QueryParam("minRating") Double minRating,
    @QueryParam("maxRating") Double maxRating,
    @QueryParam("amenity") List<String> amenities) {
  public GetHotelsParams {
    amenities = amenities == null ? List.of() : List.copyOf(amenities);
  }
}
