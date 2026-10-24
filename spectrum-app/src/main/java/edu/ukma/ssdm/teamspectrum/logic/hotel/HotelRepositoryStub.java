/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.hotel;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

public class HotelRepositoryStub implements HotelRepository {
  private final ConcurrentMap<UUID, HotelRecord> hotels = new ConcurrentHashMap<>();

  @Override
  public List<HotelRecord> search(HotelSearchCriteria criteria, int offset, int limit) {
    return filtered(criteria)
        .sorted(Comparator.comparingDouble(HotelRecord::rating).reversed())
        .skip(offset)
        .limit(limit)
        .toList();
  }

  @Override
  public long count(HotelSearchCriteria criteria) {
    return filtered(criteria).count();
  }

  private Stream<HotelRecord> filtered(HotelSearchCriteria criteria) {
    return hotels.values().stream()
        .filter(
            h ->
                criteria.location() == null
                    || h.address().city().equalsIgnoreCase(criteria.location()))
        .filter(
            h ->
                criteria.minPrice() == null
                    || h.pricePerNight().compareTo(criteria.minPrice()) >= 0)
        .filter(
            h ->
                criteria.maxPrice() == null
                    || h.pricePerNight().compareTo(criteria.maxPrice()) <= 0)
        .filter(h -> criteria.minRating() == null || h.rating() >= criteria.minRating())
        .filter(h -> criteria.maxRating() == null || h.rating() <= criteria.maxRating())
        .filter(
            h -> criteria.amenities().isEmpty() || h.amenities().containsAll(criteria.amenities()));
  }

  @Override
  public Optional<HotelRecord> findById(UUID id) {
    return Optional.ofNullable(hotels.get(id));
  }

  @Override
  public Optional<HotelRecord> findByName(String name) {
    if (name == null) {
      return Optional.empty();
    }
    return hotels.values().stream().filter(h -> sameName(h.name(), name)).findFirst();
  }

  private static boolean sameName(String existing, String requested) {
    return existing.equalsIgnoreCase(requested.trim());
  }

  @Override
  public void add(HotelRecord hotel) {
    hotels.put(hotel.id(), hotel);
  }
}
