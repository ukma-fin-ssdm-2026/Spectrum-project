/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.room;

import edu.ukma.ssdm.teamspectrum.api.hotel.Hotel;
import java.util.Set;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "Room", description = "A room belonging to a hotel")
public record Room(
    @Schema(examples = {roomIdExample}) UUID roomId,
    @Schema(examples = {Hotel.idExample}) UUID hotelId,
    @Schema(examples = {CreateRoom.roomNumberExample}) String roomNumber,
    @Schema(
            enumeration = {RoomType.Values.Single, RoomType.Values.Double, RoomType.Values.Suite},
            examples = {RoomType.Values.Double})
        RoomType roomType,
    Money pricePerNight,
    @Schema(
            enumeration = {RoomStatus.Values.Active, RoomStatus.Values.Inactive},
            examples = {RoomStatus.Values.Active})
        RoomStatus roomStatus,
    @Schema(examples = {amenitiesExample}) Set<String> amenities) {
  static final String roomIdExample = "321e7564-e89b-12d3-a456-426614174000";
  static final String amenitiesExample = "Wi-Fi";

  public Room {
    amenities = amenities == null ? Set.of() : Set.copyOf(amenities);
  }

  @Override
  public Set<String> amenities() {
    return Set.copyOf(amenities);
  }
}
