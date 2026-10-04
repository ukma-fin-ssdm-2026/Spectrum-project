/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.common.page;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

public record PaginationParams(
    @QueryParam("page") @DefaultValue("0") @Min(0) int page,
    @QueryParam("limit") @DefaultValue("10") @Min(1) @Max(100) int limit) {}
