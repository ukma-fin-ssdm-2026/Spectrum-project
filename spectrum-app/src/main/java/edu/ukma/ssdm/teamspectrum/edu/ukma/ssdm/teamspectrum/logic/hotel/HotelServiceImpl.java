/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.edu.ukma.ssdm.teamspectrum.logic.hotel;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class HotelServiceImpl implements HotelService {
  private final HotelRepository repository;

  @Inject
  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "The repository is an intentionally shared injected dependency.")
  public HotelServiceImpl(HotelRepository repository) {
    this.repository = repository;
  }

  @Override
  public HotelSearchPage search(HotelSearchCriteria criteria) {
    int offset = Math.multiplyExact(criteria.page(), criteria.limit());
    return new HotelSearchPage(
        repository.search(criteria, offset, criteria.limit()), repository.count(criteria));
  }

  @Override
  public HotelRecord create(String name, HotelAddress address) {
    HotelRecord hotel =
        new HotelRecord(
            UUID.randomUUID(), name, address, null, BigDecimal.ZERO, 0, java.util.Set.of());
    repository.add(hotel);
    return hotel;
  }

  @Override
  public Optional<HotelRecord> findById(UUID id) {
    return repository.findById(id);
  }
}
