/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.reliability;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.ukma.ssdm.teamspectrum.logic.hotel.HotelService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import java.sql.SQLTimeoutException;
import java.util.UUID;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(BaseTestProfile.class)
@Tag("reliability")
class HotelTimeoutTest {
  @InjectMock HotelService hotelService;

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
