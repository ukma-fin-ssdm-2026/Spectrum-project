/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.ukma.ssdm.teamspectrum.api.auth.AuthController;
import edu.ukma.ssdm.teamspectrum.logic.auth.Role;
import edu.ukma.ssdm.teamspectrum.logic.auth.TokenFactoryImpl;
import edu.ukma.ssdm.teamspectrum.logic.auth.User;
import edu.ukma.ssdm.teamspectrum.logic.hotel.HotelAddress;
import edu.ukma.ssdm.teamspectrum.logic.hotel.HotelAlreadyExistsException;
import edu.ukma.ssdm.teamspectrum.logic.hotel.HotelRecord;
import edu.ukma.ssdm.teamspectrum.logic.hotel.HotelService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(HotelControllerTest.HotelTestProfile.class)
class HotelControllerTest {
  @InjectMock HotelService hotelService;

  static final String jwtSecret = "test-secret-for-hotel-controller-0123456789";
  final String hotelsPath = "/v1/api/hotels";
  final String createBody =
      """
      {"name":"Grand Budapest",
       "address":{"city":"Kyiv","district":"Podil","street":"Skovorody","buildingNumber":34}}
      """;

  public static class HotelTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of(
          "DB_URL", "jdbc:postgresql://127.0.0.1:1/hotel_controller_test",
          "DB_USERNAME", "test",
          "DB_PASSWORD", "test",
          "JWT_SECRET", jwtSecret,
          "quarkus.flyway.migrate-at-start", "false",
          "quarkus.datasource.jdbc.initial-size", "0",
          "quarkus.datasource.jdbc.min-size", "0");
    }
  }

  // TP-17
  @Test
  void getHotelReturnsHotelForExistingId() {
    var hotel = hotel("Grand Budapest");
    when(hotelService.findById(hotel.id())).thenReturn(Optional.of(hotel));

    given()
        .when()
        .get(hotelsPath + "/" + hotel.id())
        .then()
        .statusCode(200)
        .body("hotelId", equalTo(hotel.id().toString()))
        .body("name", equalTo("Grand Budapest"))
        .body("address.city", equalTo("Kyiv"));
  }

  // TP-17
  @Test
  void getHotelReturnsNotFoundForUnknownId() {
    var unknownId = UUID.randomUUID();
    when(hotelService.findById(unknownId)).thenReturn(Optional.empty());

    given().when().get(hotelsPath + "/" + unknownId).then().statusCode(404);
  }

  // TP-18
  @Test
  void createReturnsUnauthorizedWithoutToken() {
    given()
        .contentType(ContentType.JSON)
        .body(createBody)
        .when()
        .post(hotelsPath)
        .then()
        .statusCode(401);

    verify(hotelService, never()).save(any());
  }

  // TP-18
  @Test
  void createReturnsForbiddenForClient() {
    given()
        .contentType(ContentType.JSON)
        .cookie(AuthController.authTokenName, token(Role.CLIENT))
        .body(createBody)
        .when()
        .post(hotelsPath)
        .then()
        .statusCode(403);

    verify(hotelService, never()).save(any());
  }

  // TP-18
  @Test
  void createReturnsCreatedForWorker() {
    when(hotelService.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    given()
        .contentType(ContentType.JSON)
        .cookie(AuthController.authTokenName, token(Role.WORKER))
        .body(createBody)
        .when()
        .post(hotelsPath)
        .then()
        .statusCode(201)
        .body("name", equalTo("Grand Budapest"));
  }

  // TP-18
  @Test
  void createReturnsConflictWhenHotelNameExists() {
    when(hotelService.save(any())).thenThrow(new HotelAlreadyExistsException("Grand Budapest"));

    given()
        .contentType(ContentType.JSON)
        .cookie(AuthController.authTokenName, token(Role.ADMIN))
        .body(createBody)
        .when()
        .post(hotelsPath)
        .then()
        .statusCode(409);
  }

  // Issued by the same factory as login, so the test also checks that login tokens are accepted
  private static String token(Role role) {
    var user = new User(UUID.randomUUID(), "staff@example.com", "hash", role);
    return new TokenFactoryImpl(jwtSecret).create(user).value();
  }

  private static HotelRecord hotel(String name) {
    return new HotelRecord(
        UUID.randomUUID(),
        name,
        new HotelAddress("Kyiv", "Podil", "Skovorody", 34),
        null,
        BigDecimal.valueOf(145),
        4.8,
        Set.of("Wi-Fi"));
  }
}
