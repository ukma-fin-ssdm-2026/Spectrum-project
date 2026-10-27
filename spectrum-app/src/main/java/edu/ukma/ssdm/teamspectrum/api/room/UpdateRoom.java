/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.room;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "UpdateRoom", description = "Optional room fields to update")
public record UpdateRoom(
    @Size(min = CreateRoom.roomNumberMinSize, max = CreateRoom.roomNumberMaxSize)
        @Pattern(regexp = "(?s).*\\S.*", message = "must contain a non-whitespace character")
        @Schema(examples = {CreateRoom.roomNumberExample})
        String roomNumber,
    @Schema(
            enumeration = {RoomType.Values.Single, RoomType.Values.Double, RoomType.Values.Suite},
            examples = {RoomType.Values.Double})
        RoomType roomType,
    @Valid Money pricePerNight,
    @Schema(
            enumeration = {RoomStatus.Values.Active, RoomStatus.Values.Inactive},
            examples = {RoomStatus.Values.Active})
        RoomStatus roomStatus,
    @Size(max = CreateRoom.maxAmenities) @Schema(examples = {CreateRoom.amenitiesExample})
        Set<
                @NotBlank @Size(min = CreateRoom.amenityMinSize, max = CreateRoom.amenityMaxSize)
                String>
            amenities) {
  public UpdateRoom {
    amenities = amenities == null ? null : Set.copyOf(amenities);
  }
}
