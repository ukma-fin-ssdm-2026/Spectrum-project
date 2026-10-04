/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.common.page;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

public record PaginationParams(
    @QueryParam("page") @DefaultValue("0") int page,
    @QueryParam("limit") @DefaultValue("10") int limit
) {}
