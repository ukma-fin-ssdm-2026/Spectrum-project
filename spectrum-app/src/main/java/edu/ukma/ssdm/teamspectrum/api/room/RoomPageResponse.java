/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.room;

import edu.ukma.ssdm.teamspectrum.api.common.page.PageResponse;
import edu.ukma.ssdm.teamspectrum.api.common.page.PaginationParams;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "RoomPageResponse", description = "A page of rooms")
public class RoomPageResponse extends PageResponse<Room> {
  public RoomPageResponse(List<Room> items, long totalItems, PaginationParams params) {
    super(items, totalItems, params);
  }
}
