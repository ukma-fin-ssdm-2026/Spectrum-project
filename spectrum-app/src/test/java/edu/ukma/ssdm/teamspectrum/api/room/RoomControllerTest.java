/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.room;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.ukma.ssdm.teamspectrum.api.ControllerTestProfile;
import edu.ukma.ssdm.teamspectrum.api.auth.AuthController;
import edu.ukma.ssdm.teamspectrum.api.common.page.PaginationParams;
import edu.ukma.ssdm.teamspectrum.logic.auth.Role;
import edu.ukma.ssdm.teamspectrum.logic.auth.TokenFactoryImpl;
import edu.ukma.ssdm.teamspectrum.logic.auth.User;
import edu.ukma.ssdm.teamspectrum.logic.room.CreateRoomCommand;
import edu.ukma.ssdm.teamspectrum.logic.room.RoomMutationResult;
import edu.ukma.ssdm.teamspectrum.logic.room.RoomPage;
import edu.ukma.ssdm.teamspectrum.logic.room.RoomRecord;
import edu.ukma.ssdm.teamspectrum.logic.room.RoomSearchCriteria;
import edu.ukma.ssdm.teamspectrum.logic.room.RoomService;
import edu.ukma.ssdm.teamspectrum.logic.room.RoomSortBy;
import edu.ukma.ssdm.teamspectrum.logic.room.UpdateRoomCommand;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(ControllerTestProfile.class)
class RoomControllerTest {
  private static final String JWT_SECRET = "test-secret-for-controller-tests";

  @InjectMock RoomService roomService;
  @InjectMock RoomFactory roomFactory;
  @InjectMock RoomSearchCriteriaFactory searchCriteriaFactory;

  private static final UUID HOTEL_ID = UUID.randomUUID();
  private static final UUID ROOM_ID = UUID.randomUUID();
  private static final String BASE_PATH = "/v1/api/hotels/" + HOTEL_ID + "/rooms";
  private static final String ROOM_NUMBER = "204";
  private static final BigDecimal ROOM_PRICE_AMOUNT = BigDecimal.valueOf(100.5);
  private static final String ROOM_CURRENCY = "USD";
  private static final String ROOM_AMENITY = "Wi-Fi";
  private static final String INVALID_ROOM_NUMBER = " ";
  private static final CreateRoom CREATE_REQUEST =
      new CreateRoom(
          ROOM_NUMBER,
          RoomType.DOUBLE,
          new Money(ROOM_PRICE_AMOUNT, ROOM_CURRENCY),
          RoomStatus.ACTIVE,
          Set.of(ROOM_AMENITY));
  private static final CreateRoomCommand CREATE_COMMAND =
      new CreateRoomCommand(
          ROOM_ID,
          ROOM_NUMBER,
          edu.ukma.ssdm.teamspectrum.logic.room.RoomType.DOUBLE,
          org.javamoney.moneta.Money.of(ROOM_PRICE_AMOUNT, ROOM_CURRENCY),
          edu.ukma.ssdm.teamspectrum.logic.room.RoomStatus.ACTIVE,
          Set.of(ROOM_AMENITY));
  private static final UpdateRoom UPDATE_REQUEST =
      new UpdateRoom(ROOM_NUMBER, null, null, null, null);
  private static final UpdateRoomCommand UPDATE_COMMAND =
      new UpdateRoomCommand(ROOM_NUMBER, null, null, null, null);

  private static final RoomRecord ROOM_RECORD =
      new RoomRecord(
          ROOM_ID,
          HOTEL_ID,
          ROOM_NUMBER,
          edu.ukma.ssdm.teamspectrum.logic.room.RoomType.DOUBLE,
          org.javamoney.moneta.Money.of(ROOM_PRICE_AMOUNT, ROOM_CURRENCY),
          edu.ukma.ssdm.teamspectrum.logic.room.RoomStatus.ACTIVE,
          Set.of(ROOM_AMENITY));
  private static final Room API_ROOM =
      new Room(
          ROOM_ID,
          HOTEL_ID,
          ROOM_NUMBER,
          RoomType.DOUBLE,
          new Money(ROOM_PRICE_AMOUNT, ROOM_CURRENCY),
          RoomStatus.ACTIVE,
          Set.of(ROOM_AMENITY));

  private static final String validCreateRequest =
      """
      {"roomNumber":"%s","roomType":"DOUBLE","pricePerNight":{"amount":%s,"currency":"%s"},"roomStatus":"ACTIVE","amenities":["%s"]}
      """
          .formatted(ROOM_NUMBER, ROOM_PRICE_AMOUNT.toPlainString(), ROOM_CURRENCY, ROOM_AMENITY);

  private static final String validUpdateRequest =
      """
      {"roomNumber":"%s"}
      """
          .formatted(ROOM_NUMBER);

  private static final String invalidUpdateRequest =
      """
      {"roomNumber":"%s"}
      """
          .formatted(INVALID_ROOM_NUMBER);

  @Test
  void createReturnsCreated() {
    when(roomFactory.toLogic(CREATE_REQUEST)).thenReturn(CREATE_COMMAND);
    when(roomService.create(HOTEL_ID, CREATE_COMMAND))
        .thenReturn(new RoomMutationResult.Success(ROOM_RECORD));
    when(roomFactory.toApi(ROOM_RECORD)).thenReturn(API_ROOM);

    given()
        .contentType(ContentType.JSON)
        .cookie(AuthController.authTokenName, token(Role.WORKER))
        .body(validCreateRequest)
        .post(BASE_PATH)
        .then()
        .statusCode(201)
        .body("roomId", equalTo(ROOM_ID.toString()))
        .body("hotelId", equalTo(HOTEL_ID.toString()))
        .body("roomNumber", equalTo(ROOM_NUMBER))
        .body("roomType", equalTo(RoomType.DOUBLE.toString()))
        .body("pricePerNight.amount", equalTo(ROOM_PRICE_AMOUNT.floatValue()))
        .body("pricePerNight.currency", equalTo(ROOM_CURRENCY))
        .body("roomStatus", equalTo(RoomStatus.ACTIVE.toString()))
        .body("amenities", equalTo(List.of(ROOM_AMENITY)));
  }

  @Test
  void createReturnsBadRequestForInvalidRequest() {
    given()
        .contentType(ContentType.JSON)
        .cookie(AuthController.authTokenName, token(Role.WORKER))
        .body("{}")
        .post(BASE_PATH)
        .then()
        .statusCode(400)
        .body("fields", not(empty()));
  }

  @Test
  void createReturnsForbiddenForClientToken() {
    given()
        .contentType(ContentType.JSON)
        .cookie(AuthController.authTokenName, token(Role.CLIENT))
        .body(validCreateRequest)
        .post(BASE_PATH)
        .then()
        .statusCode(403);

    verify(roomService, never()).create(any(), any());
  }

  @Test
  void createReturnsNotFoundWhenHotelDoesNotExist() {
    when(roomFactory.toLogic(CREATE_REQUEST)).thenReturn(CREATE_COMMAND);
    when(roomService.create(HOTEL_ID, CREATE_COMMAND))
        .thenReturn(new RoomMutationResult.NotFound());
    given()
        .contentType(ContentType.JSON)
        .cookie(AuthController.authTokenName, token(Role.WORKER))
        .body(validCreateRequest)
        .post(BASE_PATH)
        .then()
        .statusCode(404)
        .body(equalTo(""));
  }

  @Test
  void createReturnsConflictWhenRoomNumberExists() {
    when(roomFactory.toLogic(CREATE_REQUEST)).thenReturn(CREATE_COMMAND);
    when(roomService.create(HOTEL_ID, CREATE_COMMAND))
        .thenReturn(new RoomMutationResult.RoomNumberConflict());
    given()
        .contentType(ContentType.JSON)
        .cookie(AuthController.authTokenName, token(Role.WORKER))
        .body(validCreateRequest)
        .post(BASE_PATH)
        .then()
        .statusCode(409)
        .body(equalTo(""));
  }

  @Test
  void updateReturnsOk() {
    when(roomFactory.toLogic(UPDATE_REQUEST)).thenReturn(UPDATE_COMMAND);
    when(roomService.update(HOTEL_ID, ROOM_ID, UPDATE_COMMAND))
        .thenReturn(new RoomMutationResult.Success(ROOM_RECORD));
    when(roomFactory.toApi(ROOM_RECORD)).thenReturn(API_ROOM);
    given()
        .contentType(ContentType.JSON)
        .cookie(AuthController.authTokenName, token(Role.WORKER))
        .body(validUpdateRequest)
        .put(BASE_PATH + "/" + ROOM_ID)
        .then()
        .statusCode(200)
        .body("roomId", equalTo(ROOM_ID.toString()))
        .body("hotelId", equalTo(HOTEL_ID.toString()))
        .body("roomNumber", equalTo(ROOM_NUMBER))
        .body("roomType", equalTo(RoomType.DOUBLE.toString()))
        .body("pricePerNight.amount", equalTo(ROOM_PRICE_AMOUNT.floatValue()))
        .body("pricePerNight.currency", equalTo(ROOM_CURRENCY))
        .body("roomStatus", equalTo(RoomStatus.ACTIVE.toString()))
        .body("amenities", equalTo(List.of(ROOM_AMENITY)));
  }

  @Test
  void updateReturnsBadRequestForInvalidRequest() {
    given()
        .contentType(ContentType.JSON)
        .cookie(AuthController.authTokenName, token(Role.WORKER))
        .body(invalidUpdateRequest)
        .put(BASE_PATH + "/" + ROOM_ID)
        .then()
        .statusCode(400)
        .body("fields", not(empty()));
  }

  @Test
  void updateReturnsForbiddenForClientToken() {
    given()
        .contentType(ContentType.JSON)
        .cookie(AuthController.authTokenName, token(Role.CLIENT))
        .body(validUpdateRequest)
        .put(BASE_PATH + "/" + ROOM_ID)
        .then()
        .statusCode(403);

    verify(roomService, never()).update(any(), any(), any());
  }

  @Test
  void updateReturnsNotFoundWhenRoomDoesNotExist() {
    when(roomFactory.toLogic(UPDATE_REQUEST)).thenReturn(UPDATE_COMMAND);
    when(roomService.update(HOTEL_ID, ROOM_ID, UPDATE_COMMAND))
        .thenReturn(new RoomMutationResult.NotFound());
    given()
        .contentType(ContentType.JSON)
        .cookie(AuthController.authTokenName, token(Role.WORKER))
        .body(validUpdateRequest)
        .put(BASE_PATH + "/" + ROOM_ID)
        .then()
        .statusCode(404)
        .body(equalTo(""));
  }

  @Test
  void updateReturnsConflictWhenRoomNumberExists() {
    when(roomFactory.toLogic(UPDATE_REQUEST)).thenReturn(UPDATE_COMMAND);
    when(roomService.update(HOTEL_ID, ROOM_ID, UPDATE_COMMAND))
        .thenReturn(new RoomMutationResult.RoomNumberConflict());
    given()
        .contentType(ContentType.JSON)
        .cookie(AuthController.authTokenName, token(Role.WORKER))
        .body(validUpdateRequest)
        .put(BASE_PATH + "/" + ROOM_ID)
        .then()
        .statusCode(409)
        .body(equalTo(""));
  }

  @Test
  void getRoomsReturnsOk() {
    final GetRoomsParams getRoomsParams =
        new GetRoomsParams(null, null, null, null, null, null, Set.of(), null, null);
    final PaginationParams pagination = new PaginationParams(0, 10);
    final RoomSearchCriteria searchCriteria =
        new RoomSearchCriteria(
            null,
            null,
            null,
            null,
            null,
            null,
            Set.of(),
            RoomSortBy.PRICE_PER_NIGHT,
            edu.ukma.ssdm.teamspectrum.logic.common.SortOrder.ASC,
            pagination.page(),
            pagination.limit());
    when(searchCriteriaFactory.create(getRoomsParams, pagination)).thenReturn(searchCriteria);
    when(roomService.findPage(HOTEL_ID, searchCriteria))
        .thenReturn(new RoomPage(List.of(ROOM_RECORD), 1));
    when(roomFactory.toApi(ROOM_RECORD)).thenReturn(API_ROOM);
    given()
        .get(BASE_PATH)
        .then()
        .statusCode(200)
        .body("items.size()", equalTo(1))
        .body("items[0].roomId", equalTo(ROOM_ID.toString()))
        .body("items[0].hotelId", equalTo(HOTEL_ID.toString()))
        .body("items[0].roomNumber", equalTo(ROOM_NUMBER))
        .body("items[0].roomType", equalTo(RoomType.DOUBLE.toString()))
        .body("items[0].pricePerNight.amount", equalTo(ROOM_PRICE_AMOUNT.floatValue()))
        .body("items[0].pricePerNight.currency", equalTo(ROOM_CURRENCY))
        .body("items[0].roomStatus", equalTo(RoomStatus.ACTIVE.toString()))
        .body("items[0].amenities", equalTo(List.of(ROOM_AMENITY)))
        .body("totalItems", equalTo(1))
        .body("totalPages", equalTo(1))
        .body("currentPage", equalTo(0))
        .body("limit", equalTo(pagination.limit()));
  }

  @Test
  void getRoomsReturnsBadRequestForInvalidPagination() {
    given()
        .queryParam("page", -1)
        .get(BASE_PATH)
        .then()
        .statusCode(400)
        .body("fields", not(empty()));
  }

  @Test
  void getRoomReturnsOk() {
    when(roomService.findById(HOTEL_ID, ROOM_ID)).thenReturn(Optional.of(ROOM_RECORD));
    when(roomFactory.toApi(ROOM_RECORD)).thenReturn(API_ROOM);
    given()
        .get(BASE_PATH + "/" + ROOM_ID)
        .then()
        .statusCode(200)
        .body("roomId", equalTo(ROOM_ID.toString()))
        .body("hotelId", equalTo(HOTEL_ID.toString()))
        .body("roomNumber", equalTo(ROOM_NUMBER))
        .body("roomType", equalTo(RoomType.DOUBLE.toString()))
        .body("pricePerNight.amount", equalTo(ROOM_PRICE_AMOUNT.floatValue()))
        .body("pricePerNight.currency", equalTo(ROOM_CURRENCY))
        .body("roomStatus", equalTo(RoomStatus.ACTIVE.toString()))
        .body("amenities", equalTo(List.of(ROOM_AMENITY)));
  }

  @Test
  void getRoomReturnsNotFoundWhenRoomDoesNotExist() {
    when(roomService.findById(HOTEL_ID, ROOM_ID)).thenReturn(Optional.empty());
    given().get(BASE_PATH + "/" + ROOM_ID).then().statusCode(404).body(equalTo(""));
  }

  private static String token(Role role) {
    var user = new User(UUID.randomUUID(), "person@example.com", "hash", role);
    return new TokenFactoryImpl(JWT_SECRET).create(user).value();
  }
}
