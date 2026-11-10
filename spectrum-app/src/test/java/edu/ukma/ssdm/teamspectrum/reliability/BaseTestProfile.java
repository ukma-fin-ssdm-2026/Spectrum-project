/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.reliability;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class BaseTestProfile implements QuarkusTestProfile {
  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of(
        "DB_URL",
        "jdbc:postgresql://127.0.0.1:1/base_test",
        "DB_USERNAME",
        "test",
        "DB_PASSWORD",
        "test",
        "JWT_SECRET",
        "test-secret-for-base-tests-base",
        "quarkus.flyway.migrate-at-start",
        "false",
        "quarkus.datasource.jdbc.initial-size",
        "0",
        "quarkus.datasource.jdbc.min-size",
        "0",
        "bootstrap.admin.email",
        "test@example.com",
        "bootstrap.admin.password",
        "test-secret-for-user-repository");
  }
}
