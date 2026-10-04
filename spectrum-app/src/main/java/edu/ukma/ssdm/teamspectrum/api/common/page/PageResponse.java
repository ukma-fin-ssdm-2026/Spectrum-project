/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.common.page;

import java.util.List;
import java.util.Objects;

public class PageResponse<T> {
  private final List<T> items;
  private final long totalItems;
  private final int totalPages;
  private final int currentPage;
  private final int limit;

  public PageResponse(List<T> items, long totalItems, PaginationParams params) {
    this.items = items;
    this.totalItems = totalItems;
    this.totalPages = (int) Math.max(1, Math.ceil((double) totalItems / params.limit()));
    this.currentPage = params.page();
    this.limit = params.limit();
  }

  public List<T> items() {
    return items;
  }

  public long totalItems() {
    return totalItems;
  }

  public int totalPages() {
    return totalPages;
  }

  public int currentPage() {
    return currentPage;
  }

  public int limit() {
    return limit;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (PageResponse) obj;
    return Objects.equals(this.items, that.items)
        && this.totalItems == that.totalItems
        && this.totalPages == that.totalPages
        && this.currentPage == that.currentPage
        && this.limit == that.limit;
  }

  @Override
  public int hashCode() {
    return Objects.hash(items, totalItems, totalPages, currentPage, limit);
  }

  @Override
  public String toString() {
    return "PageResponse["
        + "items="
        + items
        + ", "
        + "totalItems="
        + totalItems
        + ", "
        + "totalPages="
        + totalPages
        + ", "
        + "currentPage="
        + currentPage
        + ", "
        + "limit="
        + limit
        + ']';
  }
}
