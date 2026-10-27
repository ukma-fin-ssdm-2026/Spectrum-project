/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.room;

import edu.ukma.ssdm.teamspectrum.api.room.RoomType.Values;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    description = "Room accommodation type",
    enumeration = {Values.Single, Values.Double, Values.Suite})
public enum RoomType {
  SINGLE(Values.Single),
  DOUBLE(Values.Double),
  SUITE(Values.Suite);

  private final String value;

  RoomType(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }

  public static class Values {
    public static final String Single = "SINGLE";
    public static final String Double = "DOUBLE";
    public static final String Suite = "SUITE";
  }
}
