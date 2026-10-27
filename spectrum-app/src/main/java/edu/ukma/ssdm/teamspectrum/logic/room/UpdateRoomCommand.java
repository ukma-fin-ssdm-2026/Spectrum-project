/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.room;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Set;
import org.javamoney.moneta.Money;

@SuppressFBWarnings(
    value = {"EI_EXPOSE_REP"},
    justification = "Moneta Money is immutable and thread-safe.")
public record UpdateRoomCommand(
    String roomNumber,
    RoomType roomType,
    Money pricePerNight,
    RoomStatus roomStatus,
    Set<String> amenities) {
  public UpdateRoomCommand {
    if (roomNumber == null
        && roomType == null
        && pricePerNight == null
        && roomStatus == null
        && amenities == null) {
      throw new IllegalArgumentException("At least one room field must be provided");
    }
    amenities = amenities == null ? null : Set.copyOf(amenities);
  }
}
