/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.room;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Set;
import java.util.UUID;
import org.javamoney.moneta.Money;

@SuppressFBWarnings(
    value = {"EI_EXPOSE_REP"},
    justification = "Moneta Money is immutable and thread-safe.")
public record CreateRoomCommand(
    UUID roomId,
    String roomNumber,
    RoomType roomType,
    Money pricePerNight,
    RoomStatus roomStatus,
    Set<String> amenities) {
  public CreateRoomCommand {
    amenities = amenities == null ? Set.of() : Set.copyOf(amenities);
  }
}
