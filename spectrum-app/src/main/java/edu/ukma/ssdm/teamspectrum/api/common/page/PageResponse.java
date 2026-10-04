/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.common.page;

import java.util.List;

public record PageResponse<T>(
    List<T> items,
    long totalItems,
    int totalPages,
    int currentPage,
    int limit
) {
    public PageResponse(List<T> items, long totalItems, PaginationParams params) {
        this(
            items,
            totalItems,
            (int) Math.max(1, Math.ceil((double) totalItems / params.limit())),
            params.page(),
            params.limit()
        );
    }
}
