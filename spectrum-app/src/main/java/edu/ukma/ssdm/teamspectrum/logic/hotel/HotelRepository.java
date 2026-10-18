/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.hotel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HotelRepository {
  List<HotelRecord> search(HotelSearchCriteria criteria, int offset, int limit);

  long count(HotelSearchCriteria criteria);

  Optional<HotelRecord> findById(UUID id);

  Optional<HotelRecord> findByName(String name);

  void add(HotelRecord hotel);
}
