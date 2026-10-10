/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import edu.ukma.ssdm.teamspectrum.logic.hotel.HotelSearchCriteria;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Set;

@ApplicationScoped
public class HotelSearchCriteriaFactory {
  public HotelSearchCriteria create(GetHotelsParams params) {
    return new HotelSearchCriteria(
        params.location(),
        params.checkIn(),
        params.checkOut(),
        params.minPrice(),
        params.maxPrice(),
        params.minRating(),
        params.maxRating(),
        Set.copyOf(params.amenities()),
        params.page(),
        params.limit());
  }
}
