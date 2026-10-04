/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import edu.ukma.ssdm.teamspectrum.api.common.error.ValidationError;
import edu.ukma.ssdm.teamspectrum.api.common.page.PageResponse;
import edu.ukma.ssdm.teamspectrum.api.common.page.PaginationParams;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("v1/hotels")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Hotels", description = "Hotel management")
public class HotelController {
  Map<UUID, Hotel> hotels = new ConcurrentHashMap<>();

  @POST
  @Operation(summary = "Create a hotel", description = "Creates a new hotel")
  @APIResponse(
      responseCode = "201",
      description = "Hotel created",
      content = @Content(schema = @Schema(implementation = Hotel.class)))
  @APIResponse(
      responseCode = "400",
      description = "Validation failed",
      content = @Content(schema = @Schema(implementation = ValidationError.class)))
  public Response create(@Valid @NotNull CreateHotel request) {
    Hotel created = new Hotel(UUID.randomUUID(), request.name(), request.address());
    hotels.put(created.hotelId(), created);
    return Response.status(Response.Status.CREATED).entity(created).build();
  }

  @GET
  @Operation(summary = "Fetch all hotels", description = "Returns a paginated list of all hotels")
  @APIResponse(
      responseCode = "200",
      description = "Successful retrieval",
      content = @Content(schema = @Schema(implementation = HotelPageResponse.class)))
  @APIResponse(
      responseCode = "400",
      description = "Validation failed",
      content = @Content(schema = @Schema(implementation = ValidationError.class)))
  public Response getHotels(@BeanParam @Valid PaginationParams params) {
    long total = hotels.size();
    List<Hotel> items =
        hotels.values().stream()
            .skip((long) params.page() * params.limit())
            .limit(params.limit())
            .toList();

    return Response.ok(new PageResponse<>(items, total, params)).build();
  }

  @GET
  @Path("/{hotelId}")
  @Operation(
      summary = "Fetch a hotel by ID",
      description = "Returns a single hotel by its unique identifier")
  @APIResponse(
      responseCode = "200",
      description = "Successful retrieval",
      content = @Content(schema = @Schema(implementation = Hotel.class)))
  @APIResponse(responseCode = "404", description = "Hotel not found")
  @APIResponse(
      responseCode = "400",
      description = "Validation failed",
      content = @Content(schema = @Schema(implementation = ValidationError.class)))
  public Response getHotel(@PathParam("hotelId") UUID hotelId) {
    Hotel hotel = hotels.get(hotelId);
    if (hotel == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(hotel).build();
  }
}
