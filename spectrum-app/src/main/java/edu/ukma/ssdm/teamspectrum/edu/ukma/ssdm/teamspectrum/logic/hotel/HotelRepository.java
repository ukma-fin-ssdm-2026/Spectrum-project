/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.edu.ukma.ssdm.teamspectrum.logic.hotel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HotelRepository {
  List<HotelRecord> search(HotelSearchCriteria criteria, int offset, int limit);

  long count(HotelSearchCriteria criteria);

  Optional<HotelRecord> findById(UUID id);

  void add(HotelRecord hotel);
}
