/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.room;

import java.util.List;

public record RoomPage(List<RoomRecord> items, long totalItems) {
  public RoomPage {
    items = List.copyOf(items);
  }
}
