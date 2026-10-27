/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.room;

import edu.ukma.ssdm.teamspectrum.api.room.RoomSortBy.Values;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    description = "Room field used to sort search results",
    enumeration = {Values.RoomNumber, Values.PricePerNight})
public enum RoomSortBy {
  RoomNumber(Values.RoomNumber),
  PricePerNight(Values.PricePerNight);

  private final String sortByName;

  RoomSortBy(String sortByName) {
    this.sortByName = sortByName;
  }

  @Override
  public String toString() {
    return sortByName;
  }

  public static class Values {
    public static final String RoomNumber = "ROOM_NUMBER";
    public static final String PricePerNight = "PRICE_PER_NIGHT";
  }
}
