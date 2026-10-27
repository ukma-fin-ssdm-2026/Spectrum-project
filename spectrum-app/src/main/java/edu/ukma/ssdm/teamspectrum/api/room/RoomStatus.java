/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.room;

import edu.ukma.ssdm.teamspectrum.api.room.RoomStatus.Values;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    description = "Room availability status",
    enumeration = {Values.Active, Values.Inactive})
public enum RoomStatus {
  ACTIVE(Values.Active),
  INACTIVE(Values.Inactive);

  private final String value;

  RoomStatus(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }

  public static class Values {
    public static final String Active = "ACTIVE";
    public static final String Inactive = "INACTIVE";
  }
}
