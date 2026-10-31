/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.hotel;

import java.util.Optional;
import java.util.UUID;

public interface HotelService {
  HotelSearchPage search(HotelSearchCriteria criteria);

  HotelMutationResult save(HotelRecord hotel);

  Optional<HotelRecord> findById(UUID id);
}
