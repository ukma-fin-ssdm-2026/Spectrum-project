/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.room;

import edu.ukma.ssdm.teamspectrum.api.common.SortOrder;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.QueryParam;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

@Schema(name = "GetRoomsParams", description = "Optional filters for listing a hotel's rooms")
public record GetRoomsParams(
    @QueryParam("roomType")
        @Parameter(
            name = "roomType",
            in = ParameterIn.QUERY,
            example = RoomType.Values.Double,
            schema =
                @Schema(
                    enumeration = {
                      RoomType.Values.Single,
                      RoomType.Values.Double,
                      RoomType.Values.Suite
                    }))
        RoomType roomType,
    @QueryParam("roomStatus")
        @Parameter(
            name = "roomStatus",
            in = ParameterIn.QUERY,
            example = RoomStatus.Values.Active,
            schema = @Schema(enumeration = {RoomStatus.Values.Active, RoomStatus.Values.Inactive}))
        RoomStatus roomStatus,
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
    @QueryParam("amenity")
        @Size(max = CreateRoom.maxAmenities)
        @Parameter(example = CreateRoom.amenitiesExample)
        @Schema(examples = {CreateRoom.amenitiesExample})
        Set<@Size(min = CreateRoom.amenityMinSize, max = CreateRoom.amenityMaxSize) String>
            amenities,
    @QueryParam("sortBy")
        @Parameter(
            name = "sortBy",
            in = ParameterIn.QUERY,
            example = RoomSortBy.Values.RoomNumber,
            schema =
                @Schema(
                    enumeration = {RoomSortBy.Values.RoomNumber, RoomSortBy.Values.PricePerNight}))
        RoomSortBy sortBy,
    @QueryParam("sortOrder")
        @Parameter(
            name = "sortOrder",
            in = ParameterIn.QUERY,
            example = SortOrder.Values.Asc,
            schema = @Schema(enumeration = {SortOrder.Values.Asc, SortOrder.Values.Desc}))
        SortOrder sortOrder) {
  static final String minPriceExample = "100.00";
  static final String maxPriceExample = "300.00";
  static final String checkInExample = "2027-06-15";
  static final String checkOutExample = "2027-06-20";

  public GetRoomsParams {
    amenities = amenities == null ? Set.of() : Set.copyOf(amenities);
  }
}
