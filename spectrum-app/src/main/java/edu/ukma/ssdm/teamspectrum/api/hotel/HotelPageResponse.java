/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import edu.ukma.ssdm.teamspectrum.api.common.page.PageResponse;
import edu.ukma.ssdm.teamspectrum.api.common.page.PaginationParams;
import java.util.List;

public class HotelPageResponse extends PageResponse<Hotel> {
  public HotelPageResponse(List<Hotel> items, long totalItems, PaginationParams params) {
    super(items, totalItems, params);
  }
}
