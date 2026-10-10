/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record CreateRoom(
    @NotBlank
        @Size(min = roomNumberMinSize, max = roomNumberMaxSize)
        @Schema(examples = {roomNumberExample})
        String roomNumber,
    @NotNull @Schema(examples = {RoomType.example}) RoomType roomType,
    @NotNull @Valid Money pricePerNight,
    @NotNull @Schema(examples = {RoomStatus.example}) RoomStatus roomStatus) {
  static final int roomNumberMinSize = 3;
  static final int roomNumberMaxSize = 40;
  static final String roomNumberExample = "204";
}
