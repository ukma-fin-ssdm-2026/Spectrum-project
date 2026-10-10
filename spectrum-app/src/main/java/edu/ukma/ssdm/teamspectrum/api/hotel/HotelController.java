/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import edu.ukma.ssdm.teamspectrum.api.common.error.ValidationError;
import edu.ukma.ssdm.teamspectrum.api.common.page.PageResponse;
import edu.ukma.ssdm.teamspectrum.api.common.page.PaginationParams;
import edu.ukma.ssdm.teamspectrum.edu.ukma.ssdm.teamspectrum.logic.hotel.HotelAddress;
import edu.ukma.ssdm.teamspectrum.edu.ukma.ssdm.teamspectrum.logic.hotel.HotelRecord;
import edu.ukma.ssdm.teamspectrum.edu.ukma.ssdm.teamspectrum.logic.hotel.HotelSearchCriteria;
import edu.ukma.ssdm.teamspectrum.edu.ukma.ssdm.teamspectrum.logic.hotel.HotelSearchPage;
import edu.ukma.ssdm.teamspectrum.edu.ukma.ssdm.teamspectrum.logic.hotel.HotelService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("v1/api/hotels")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Hotels", description = "Hotel management")
public class HotelController {
  private final HotelService hotelService;

  @Inject
  public HotelController(HotelService hotelService) {
    this.hotelService = hotelService;
  }

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
    HotelRecord saved = hotelService.create(request.name(), toLogicAddress(request.address()));
    Hotel created = toApiHotel(saved);
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
  public Response getHotels(
      @BeanParam @Valid PaginationParams params,
      @QueryParam("location") String location,
      @QueryParam("checkIn") LocalDate checkIn,
      @QueryParam("checkOut") LocalDate checkOut,
      @QueryParam("minPrice") BigDecimal minPrice,
      @QueryParam("maxPrice") BigDecimal maxPrice,
      @QueryParam("minRating") Double minRating,
      @QueryParam("maxRating") Double maxRating,
      @QueryParam("amenity") List<String> amenities) {
    HotelSearchPage result =
        hotelService.search(
            new HotelSearchCriteria(
                location,
                checkIn,
                checkOut,
                minPrice,
                maxPrice,
                minRating,
                maxRating,
                amenities == null ? Set.of() : Set.copyOf(amenities),
                params.page(),
                params.limit()));
    List<Hotel> items = result.items().stream().map(HotelController::toApiHotel).toList();
    return Response.ok(new PageResponse<>(items, result.totalItems(), params)).build();
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
    return hotelService
        .findById(hotelId)
        .map(HotelController::toApiHotel)
        .map(h -> Response.ok(h).build())
        .orElse(Response.status(Response.Status.NOT_FOUND).build());
  }

  private static Hotel toApiHotel(HotelRecord hotel) {
    return new Hotel(
        hotel.id(),
        hotel.name(),
        toApiAddress(hotel.address()),
        hotel.pictureUrl(),
        hotel.pricePerNight(),
        hotel.rating(),
        hotel.amenities());
  }

  private static HotelAddress toLogicAddress(Address address) {
    return new HotelAddress(
        address.city(), address.district(), address.street(), address.buildingNumber());
  }

  private static Address toApiAddress(HotelAddress address) {
    return new Address(
        address.city(), address.district(), address.street(), address.buildingNumber());
  }
}
