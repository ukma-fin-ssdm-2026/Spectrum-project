/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.reliability;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.ukma.ssdm.teamspectrum.logic.hotel.HotelService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import java.sql.SQLTimeoutException;
import java.util.Map;
import java.util.UUID;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(HotelTimeoutTest.Profile.class)
class HotelTimeoutTest {
  @InjectMock HotelService hotelService;

  public static class Profile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of(
          "DB_URL", "jdbc:postgresql://127.0.0.1:1/hotel_timeout_test",
          "DB_USERNAME", "test",
          "DB_PASSWORD", "test",
          "JWT_SECRET", "test-secret-for-hotel-timeout-0123456789",
          "quarkus.flyway.migrate-at-start", "false",
          "quarkus.datasource.jdbc.initial-size", "0",
          "quarkus.datasource.jdbc.min-size", "0");
    }
  }

  @Test
  void getHotelReturnsInternalServerErrorWhenServiceTimesOut() {
    var hotelId = UUID.randomUUID();
    when(hotelService.findById(hotelId))
        .thenThrow(
            new DataAccessException(
                "Hotel lookup timed out", new SQLTimeoutException("Query timed out")));

    given().when().get("/v1/api/hotels/" + hotelId).then().statusCode(500);

    verify(hotelService).findById(hotelId);
  }
}
