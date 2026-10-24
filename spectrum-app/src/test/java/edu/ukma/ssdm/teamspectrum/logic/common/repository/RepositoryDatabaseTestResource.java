/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.common.repository;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class RepositoryDatabaseTestResource implements QuarkusTestResourceLifecycleManager {
  private String databaseUrl;
  private String username;
  private String password;
  private String schema;

  @Override
  public void init(Map<String, String> args) {
    databaseUrl = args.get("DB_URL");
    username = args.get("DB_USERNAME");
    password = args.get("DB_PASSWORD");
  }

  @Override
  public Map<String, String> start() {
    schema = "repository_test_" + UUID.randomUUID().toString().replace("-", "");

    try (Connection connection = DriverManager.getConnection(databaseUrl, username, password);
        var statement = connection.createStatement()) {
      statement.execute("CREATE SCHEMA " + schema);
    } catch (SQLException e) {
      throw new IllegalStateException("Could not create repository test schema", e);
    }

    String separator = databaseUrl.contains("?") ? "&" : "?";
    return Map.of(
        "DB_URL", databaseUrl + separator + "currentSchema=" + schema,
        "DB_USERNAME", username,
        "DB_PASSWORD", password,
        "quarkus.flyway.schemas", schema,
        "quarkus.flyway.default-schema", schema);
  }

  @Override
  public void stop() {
    if (schema == null) {
      return;
    }

    try (Connection connection = DriverManager.getConnection(databaseUrl, username, password);
        var statement = connection.createStatement()) {
      statement.execute("DROP SCHEMA " + schema + " CASCADE");
    } catch (SQLException e) {
      throw new IllegalStateException("Could not drop repository test schema " + schema, e);
    }
  }
}
