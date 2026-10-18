/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import edu.ukma.ssdm.teamspectrum.api.common.SortOrder;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.QueryParam;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

public record GetHotelsParams(
    @QueryParam("location")
        @Size(min = 2, max = locationMaxSize)
        @Parameter(example = locationExample)
        @Schema(examples = {locationExample})
        String location,
    @QueryParam("checkIn")
        @FutureOrPresent
        @Parameter(example = checkInExample)
        @Schema(examples = {checkInExample})
        LocalDate checkIn,
    @QueryParam("checkOut")
        @FutureOrPresent
        @Parameter(example = checkOutExample)
        @Schema(examples = {checkOutExample})
        LocalDate checkOut,
    @QueryParam("minPrice")
        @DecimalMin("0.0")
        @Parameter(example = minPriceExample)
        @Schema(examples = {minPriceExample})
        BigDecimal minPrice,
    @QueryParam("maxPrice")
        @DecimalMin("0.0")
        @Parameter(example = maxPriceExample)
        @Schema(examples = {maxPriceExample})
        BigDecimal maxPrice,
    @QueryParam("minRating")
        @DecimalMin("0.0")
        @DecimalMax("5.0")
        @Parameter(example = minRatingExample)
        @Schema(examples = {minRatingExample})
        Double minRating,
    @QueryParam("maxRating")
        @DecimalMin("0.0")
        @DecimalMax("5.0")
        @Parameter(example = maxRatingExample)
        @Schema(examples = {maxRatingExample})
        Double maxRating,
    @QueryParam("amenity")
        @Size(max = maxAmenities)
        @Parameter(example = amenitiesExample)
        @Schema(examples = {amenitiesExample})
        List<@Size(min = 2, max = amenityMaxSize) String> amenities,
    @QueryParam("sortBy") @Schema(defaultValue = HotelSortBy.Values.Rating) HotelSortBy sortBy,
    @QueryParam("sortOrder") @Schema(defaultValue = SortOrder.Values.Desc) SortOrder sortOrder) {
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

  public GetHotelsParams(
      String location,
      LocalDate checkIn,
      LocalDate checkOut,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      Double minRating,
      Double maxRating,
      List<@Size(min = 2, max = amenityMaxSize) String> amenities) {
    this(
        location, checkIn, checkOut, minPrice, maxPrice, minRating, maxRating, amenities, null,
        null);
  }
}
