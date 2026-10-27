/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.room;

import edu.ukma.ssdm.teamspectrum.api.common.SortOrder;
import edu.ukma.ssdm.teamspectrum.api.common.page.PaginationParams;
import edu.ukma.ssdm.teamspectrum.logic.room.RoomSearchCriteria;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Set;

@ApplicationScoped
public class RoomSearchCriteriaFactory {
  private final RoomFactory roomFactory;

  @Inject
  public RoomSearchCriteriaFactory(RoomFactory roomFactory) {
    this.roomFactory = roomFactory;
  }

  public RoomSearchCriteria create(GetRoomsParams params, PaginationParams pagination) {
    return new RoomSearchCriteria(
        roomFactory.toLogic(params.roomType()),
        roomFactory.toLogic(params.roomStatus()),
        params.checkIn(),
        params.checkOut(),
        params.minPrice(),
        params.maxPrice(),
        Set.copyOf(params.amenities()),
        mkSortBy(params.sortBy()),
        mkSortOrder(params.sortOrder()),
        pagination.page(),
        pagination.limit());
  }

  public edu.ukma.ssdm.teamspectrum.logic.room.RoomSortBy mkSortBy(RoomSortBy sortBy) {
    return sortBy == null
        ? edu.ukma.ssdm.teamspectrum.logic.room.RoomSortBy.PricePerNight
        : edu.ukma.ssdm.teamspectrum.logic.room.RoomSortBy.valueOf(sortBy.name());
  }

  public edu.ukma.ssdm.teamspectrum.logic.common.SortOrder mkSortOrder(SortOrder sortOrder) {
    return sortOrder == null
        ? edu.ukma.ssdm.teamspectrum.logic.common.SortOrder.Asc
        : edu.ukma.ssdm.teamspectrum.logic.common.SortOrder.valueOf(sortOrder.name());
  }
}
