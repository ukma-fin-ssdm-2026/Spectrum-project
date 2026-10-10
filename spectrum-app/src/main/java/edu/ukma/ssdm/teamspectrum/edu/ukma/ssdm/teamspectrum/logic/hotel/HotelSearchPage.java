/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.edu.ukma.ssdm.teamspectrum.logic.hotel;

import java.util.List;

public record HotelSearchPage(List<HotelRecord> items, long totalItems) {
  public HotelSearchPage {
    items = List.copyOf(items);
  }
}
