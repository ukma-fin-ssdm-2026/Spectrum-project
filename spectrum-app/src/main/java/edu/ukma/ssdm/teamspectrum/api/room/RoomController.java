/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.room;

import edu.ukma.ssdm.teamspectrum.api.common.error.ValidationError;
import edu.ukma.ssdm.teamspectrum.api.common.page.PaginationParams;
import edu.ukma.ssdm.teamspectrum.logic.auth.Role;
import edu.ukma.ssdm.teamspectrum.logic.room.RoomMutationResult;
import edu.ukma.ssdm.teamspectrum.logic.room.RoomService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("v1/api/hotels/{hotelId}/rooms")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Rooms", description = "Hotel room management")
public class RoomController {
  private final RoomService roomService;
  private final RoomFactory roomFactory;
  private final RoomSearchCriteriaFactory searchCriteriaFactory;

  @Inject
  public RoomController(
      RoomService roomService,
      RoomFactory roomFactory,
      RoomSearchCriteriaFactory searchCriteriaFactory) {
    this.roomService = roomService;
    this.roomFactory = roomFactory;
    this.searchCriteriaFactory = searchCriteriaFactory;
  }

  @POST
  @Operation(summary = "Create a room", description = "Creates a room for the specified hotel")
  @APIResponse(
      responseCode = "201",
      description = "Room created",
      content = @Content(schema = @Schema(implementation = Room.class)))
  @APIResponse(
      responseCode = "400",
      description = "Validation failed",
      content = @Content(schema = @Schema(implementation = ValidationError.class)))
  @APIResponse(responseCode = "404", description = "Hotel not found")
  @APIResponse(responseCode = "409", description = "Room with such number already exists")
  @APIResponse(responseCode = "401", description = "Login required")
  @APIResponse(responseCode = "403", description = "Insufficient permissions")
  @RolesAllowed(Role.Values.WORKER)
  public Response create(@PathParam("hotelId") UUID hotelId, @Valid @NotNull CreateRoom request) {
    return switch (roomService.create(hotelId, roomFactory.toLogic(request))) {
      case RoomMutationResult.Success success ->
          Response.status(Response.Status.CREATED)
              .entity(roomFactory.toApi(success.room()))
              .build();
      case RoomMutationResult.RoomNumberConflict ignored ->
          Response.status(Response.Status.CONFLICT).build();
      case RoomMutationResult.NotFound ignored ->
          Response.status(Response.Status.NOT_FOUND).build();
    };
  }

  @PUT
  @Path("/{roomId}")
  @Operation(summary = "Update a room", description = "Updates the supplied fields of a room")
  @APIResponse(
      responseCode = "200",
      description = "Room updated",
      content = @Content(schema = @Schema(implementation = Room.class)))
  @APIResponse(
      responseCode = "400",
      description = "Validation failed",
      content = @Content(schema = @Schema(implementation = ValidationError.class)))
  @APIResponse(responseCode = "404", description = "Room or hotel not found")
  @APIResponse(responseCode = "409", description = "Room with such number already exists")
  @APIResponse(responseCode = "401", description = "Login required")
  @APIResponse(responseCode = "403", description = "Insufficient permissions")
  @RolesAllowed(Role.Values.WORKER)
  public Response update(
      @PathParam("hotelId") UUID hotelId,
      @PathParam("roomId") UUID roomId,
      @Valid @NotNull UpdateRoom request) {
    return switch (roomService.update(hotelId, roomId, roomFactory.toLogic(request))) {
      case RoomMutationResult.Success success ->
          Response.ok(roomFactory.toApi(success.room())).build();
      case RoomMutationResult.RoomNumberConflict ignored ->
          Response.status(Response.Status.CONFLICT).build();
      case RoomMutationResult.NotFound ignored ->
          Response.status(Response.Status.NOT_FOUND).build();
    };
  }

  @GET
  @Operation(
      summary = "Fetch rooms",
      description = "Returns a paginated list of rooms for the hotel")
  @APIResponse(
      responseCode = "200",
      description = "Successful retrieval",
      content = @Content(schema = @Schema(implementation = RoomPageResponse.class)))
  @APIResponse(
      responseCode = "400",
      description = "Validation failed",
      content = @Content(schema = @Schema(implementation = ValidationError.class)))
  public Response getRooms(
      @PathParam("hotelId") UUID hotelId,
      @BeanParam @Valid GetRoomsParams params,
      @BeanParam @Valid PaginationParams pagination) {
    var criteria = searchCriteriaFactory.create(params, pagination);
    var result = roomService.findPage(hotelId, criteria);
    var items = result.items().stream().map(roomFactory::toApi).toList();
    return Response.ok(new RoomPageResponse(items, result.totalItems(), pagination)).build();
  }

  @GET
  @Path("/{roomId}")
  @Operation(
      summary = "Fetch a room by ID",
      description = "Returns one room by its unique identifier")
  @APIResponse(
      responseCode = "200",
      description = "Successful retrieval",
      content = @Content(schema = @Schema(implementation = Room.class)))
  @APIResponse(responseCode = "404", description = "Room or hotel not found")
  @APIResponse(
      responseCode = "400",
      description = "Validation failed",
      content = @Content(schema = @Schema(implementation = ValidationError.class)))
  public Response getRoom(@PathParam("hotelId") UUID hotelId, @PathParam("roomId") UUID roomId) {
    return roomService
        .findById(hotelId, roomId)
        .map(roomFactory::toApi)
        .map(room -> Response.ok(room).build())
        .orElse(Response.status(Response.Status.NOT_FOUND).build());
  }
}
