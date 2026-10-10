/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.hotel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HotelServiceImplTest {
  private HotelRepository repository;
  private HotelServiceImpl service;

  @BeforeEach
  void setUp() {
    repository = Mockito.mock(HotelRepository.class);
    service = new HotelServiceImpl(repository);
    when(repository.search(Mockito.any(), Mockito.anyInt(), Mockito.anyInt()))
        .thenReturn(List.of());
    when(repository.count(Mockito.any())).thenReturn(0L);
  }

  @Test
  void searchRequestsTheRequestedPageFromRepository() {
    var criteria = criteria(new BigDecimal("100"), 4.0, 5.0, Set.of("wifi"), 2, 5);
    service.search(criteria);
    verify(repository).search(criteria, 10, 5);
  }

  @Test
  void searchReadsTotalMatchingCountFromRepository() {
    var criteria = criteria(null, null, null, Set.of(), 0, 10);
    when(repository.count(criteria)).thenReturn(7L);
    var result = service.search(criteria);
    assertEquals(7, result.totalItems());
  }

  private static HotelSearchCriteria criteria(
      BigDecimal minPrice,
      Double minRating,
      Double maxRating,
      Set<String> amenities,
      int page,
      int limit) {
    return new HotelSearchCriteria(
        null, null, null, minPrice, null, minRating, maxRating, amenities, page, limit);
  }
}
