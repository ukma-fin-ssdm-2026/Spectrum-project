/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.health;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.ukma.ssdm.teamspectrum.logic.common.repository.RepositoryTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(RepositoryTestProfile.class)
class DatabaseHealthCheckTest {
  @Test
  void readinessIsUpWhenDatabaseResponds() {
    var response =
        given()
            .when()
            .get("/q/health/ready")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"))
            .extract()
            .jsonPath();

    List<Map<String, Object>> checks = response.getList("checks");
    var databaseCheck =
        checks.stream()
            .filter(check -> check.get("name").equals("Database connections health check"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Database health check is missing"));

    assertEquals("UP", databaseCheck.get("status"));
    assertEquals("UP", ((Map<?, ?>) databaseCheck.get("data")).get("<default>"));
  }
}
