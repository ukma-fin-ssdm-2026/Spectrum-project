/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record GetHotelsParams(
    @QueryParam("page") @DefaultValue("0") @Min(0) @Schema(examples = {pageExample}) int page,
    @QueryParam("limit") @DefaultValue("10") @Min(1) @Max(100) @Schema(examples = {limitExample})
        int limit,
    @QueryParam("location")
        @Size(min = 2, max = locationMaxSize)
        @Schema(examples = {locationExample})
        String location,
    @QueryParam("checkIn") @FutureOrPresent @Schema(examples = {checkInExample}) LocalDate checkIn,
    @QueryParam("checkOut") @FutureOrPresent @Schema(examples = {checkOutExample})
        LocalDate checkOut,
    @QueryParam("minPrice") @DecimalMin("0.0") @Schema(examples = {minPriceExample})
        BigDecimal minPrice,
    @QueryParam("maxPrice") @DecimalMin("0.0") @Schema(examples = {maxPriceExample})
        BigDecimal maxPrice,
    @QueryParam("minRating")
        @DecimalMin("0.0")
        @DecimalMax("5.0")
        @Schema(examples = {minRatingExample})
        Double minRating,
    @QueryParam("maxRating")
        @DecimalMin("0.0")
        @DecimalMax("5.0")
        @Schema(examples = {maxRatingExample})
        Double maxRating,
    @QueryParam("amenity") @Size(max = maxAmenities) @Schema(examples = {amenitiesExample})
        List<@Size(min = 2, max = amenityMaxSize) String> amenities) {
  static final String pageExample = "0";
  static final String limitExample = "10";
  static final int locationMaxSize = 100;
  static final String locationExample = Address.cityExample;
  static final String checkInExample = "2027-06-15";
  static final String checkOutExample = "2027-06-20";
  static final String minPriceExample = "100.00";
  static final String maxPriceExample = "300.00";
  static final String minRatingExample = "3.5";
  static final String maxRatingExample = "5.0";
  static final int maxAmenities = 20;
  static final int amenityMaxSize = 40;
  static final String amenitiesExample = "Wi-Fi";

  public GetHotelsParams {
    amenities = amenities == null ? List.of() : List.copyOf(amenities);
  }
}
