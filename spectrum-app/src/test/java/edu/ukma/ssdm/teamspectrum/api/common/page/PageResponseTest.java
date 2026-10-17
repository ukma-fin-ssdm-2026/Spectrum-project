/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.common.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class PageResponseTest {
  List<String> items = List.of("one", "two");
  int totalItems = 11;
  int page = 1;
  int limit = 10;
  PaginationParams params = new PaginationParams(page, limit);
  PageResponse<String> response = new PageResponse<>(items, totalItems, params);

  @Test
  void returnsItemsUnchanged() {
    assertIterableEquals(items, response.getItems());
  }

  @Test
  void returnsPage() {
    assertEquals(params.page(), response.getCurrentPage());
  }

  @Test
  void returnsLimit() {
    assertEquals(params.limit(), response.getLimit());
  }

  @Test
  void calculatesTotalPagesByRoundingUp() {
    assertEquals(items.size(), response.getTotalPages());
  }

  @Test
  void calculatesTotalPagesForEmptyResult() {
    assertEquals(1, new PageResponse<String>(List.of(), 0, params).getTotalPages());
  }

  @Test
  void rejectsNullItems() {
    assertThrows(
        NullPointerException.class,
        () -> new PageResponse<String>(null, 0, new PaginationParams(0, 10)));
  }

  @Test
  void rejectsNullPaginationParams() {
    assertThrows(NullPointerException.class, () -> new PageResponse<>(List.of(), 0, null));
  }
}
