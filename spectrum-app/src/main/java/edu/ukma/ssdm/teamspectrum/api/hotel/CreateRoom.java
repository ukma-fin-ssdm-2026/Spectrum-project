/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRoom(
    @NotBlank @Size(min = roomNumberMinSize, max = roomNumberMaxSize) String roomNumber,
    @NotNull RoomType roomType,
    @NotNull @Valid Money pricePerNight,
    @NotNull RoomStatus roomStatus) {
  static final int roomNumberMinSize = 3;
  static final int roomNumberMaxSize = 40;
}
