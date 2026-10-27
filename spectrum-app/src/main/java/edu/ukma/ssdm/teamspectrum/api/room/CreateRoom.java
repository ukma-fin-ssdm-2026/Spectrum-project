/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.room;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "CreateRoom", description = "Room details required to create a room")
public record CreateRoom(
    @NotBlank
        @Size(min = roomNumberMinSize, max = roomNumberMaxSize)
        @Schema(examples = {roomNumberExample})
        String roomNumber,
    @NotNull
        @Schema(
            enumeration = {RoomType.Values.Single, RoomType.Values.Double, RoomType.Values.Suite},
            examples = {RoomType.Values.Double})
        RoomType roomType,
    @NotNull @Valid Money pricePerNight,
    @NotNull
        @Schema(
            enumeration = {RoomStatus.Values.Active, RoomStatus.Values.Inactive},
            examples = {RoomStatus.Values.Active})
        RoomStatus roomStatus,
    @Size(max = maxAmenities) @Schema(examples = {amenitiesExample})
        Set<@Size(min = amenityMinSize, max = amenityMaxSize) String> amenities) {
  static final int roomNumberMinSize = 3;
  static final int roomNumberMaxSize = 40;
  static final String roomNumberExample = "204";
  static final int maxAmenities = 20;
  static final int amenityMinSize = 2;
  static final int amenityMaxSize = 40;
  static final String amenitiesExample = "Wi-Fi";

  public CreateRoom {
    amenities = amenities == null ? Set.of() : Set.copyOf(amenities);
  }
}
