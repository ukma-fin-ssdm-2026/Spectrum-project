/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.common.repository;

import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.QuarkusTestProfile.TestResourceEntry;
import java.util.List;
import java.util.Map;

public class RepositoryTestProfile implements QuarkusTestProfile {
  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of("JWT_SECRET", "test-secret-for-user-repository");
  }

  @Override
  public List<TestResourceEntry> testResources() {
    return List.of(
        new TestResourceEntry(
            RepositoryDatabaseTestResource.class,
            Map.of(
                "DB_URL", "jdbc:postgresql://localhost:5433/spectrum",
                "DB_USERNAME", "spectrum",
                "DB_PASSWORD", "spectrum")));
  }
}
