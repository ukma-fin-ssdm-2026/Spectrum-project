/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.edu.ukma.ssdm.teamspectrum.logic.hotel;

import java.util.Optional;
import java.util.UUID;

public interface HotelService {
  HotelSearchPage search(HotelSearchCriteria criteria);

  HotelRecord create(String name, HotelAddress address);

  Optional<HotelRecord> findById(UUID id);
}
