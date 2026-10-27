/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.room;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Set;
import java.util.UUID;
import org.javamoney.moneta.Money;

@SuppressFBWarnings(
    value = {"EI_EXPOSE_REP"},
    justification = "Moneta Money is immutable and thread-safe.")
public record RoomRecord(
    UUID id,
    UUID hotelId,
    String roomNumber,
    RoomType roomType,
    Money pricePerNight,
    RoomStatus roomStatus,
    Set<String> amenities) {
  public RoomRecord {
    amenities = amenities == null ? Set.of() : Set.copyOf(amenities);
  }
}
