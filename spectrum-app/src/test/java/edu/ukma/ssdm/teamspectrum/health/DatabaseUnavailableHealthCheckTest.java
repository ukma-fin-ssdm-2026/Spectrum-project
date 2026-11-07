/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.health;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.ukma.ssdm.teamspectrum.api.ControllerTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(DatabaseUnavailableHealthCheckTest.UnavailableDatabaseProfile.class)
class DatabaseUnavailableHealthCheckTest {
  public static class UnavailableDatabaseProfile extends ControllerTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      var config = new HashMap<>(super.getConfigOverrides());
      config.put("quarkus.datasource.jdbc.acquisition-timeout", "1S");
      return config;
    }
  }

  @Test
  void readinessIsDownWhenDatabaseCannotBeReached() {
    var response =
        given()
            .when()
            .get("/q/health/ready")
            .then()
            .statusCode(503)
            .body("status", equalTo("DOWN"))
            .extract()
            .jsonPath();

    List<Map<String, Object>> checks = response.getList("checks");
    var databaseCheck =
        checks.stream()
            .filter(check -> "Database connections health check".equals(check.get("name")))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Database health check is missing"));

    assertEquals("DOWN", databaseCheck.get("status"));
  }

  @Test
  void livenessStaysUpWhenDatabaseCannotBeReached() {
    given().when().get("/q/health/live").then().statusCode(200).body("status", equalTo("UP"));
  }
}
