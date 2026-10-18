/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import edu.ukma.ssdm.teamspectrum.api.common.page.PaginationParams;
import edu.ukma.ssdm.teamspectrum.logic.common.SortOrder;
import edu.ukma.ssdm.teamspectrum.logic.hotel.HotelSearchCriteria;
import edu.ukma.ssdm.teamspectrum.logic.hotel.HotelSortBy;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Set;

@ApplicationScoped
public class HotelSearchCriteriaFactory {

  public SortOrder mkSortOrder(edu.ukma.ssdm.teamspectrum.api.common.SortOrder sortOrder) {
    return sortOrder == null ? SortOrder.Desc : SortOrder.valueOf(sortOrder.name());
  }

  public HotelSortBy mkSortBy(edu.ukma.ssdm.teamspectrum.api.hotel.HotelSortBy sortBy) {
    return sortBy == null ? HotelSortBy.Rating : HotelSortBy.valueOf(sortBy.name());
  }

  public HotelSearchCriteria create(GetHotelsParams params, PaginationParams pagination) {
    return new HotelSearchCriteria(
        params.location(),
        params.checkIn(),
        params.checkOut(),
        params.minPrice(),
        params.maxPrice(),
        params.minRating(),
        params.maxRating(),
        Set.copyOf(params.amenities()),
        pagination.page(),
        pagination.limit(),
        mkSortBy(params.sortBy()),
        mkSortOrder(params.sortOrder()));
  }
}
