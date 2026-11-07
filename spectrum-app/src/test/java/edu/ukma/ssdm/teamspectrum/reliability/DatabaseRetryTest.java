/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.reliability;

import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.Hotels.HOTELS;
import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.Rooms.ROOMS;
import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.Users.USERS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.ukma.ssdm.teamspectrum.config.AppConfig;
import edu.ukma.ssdm.teamspectrum.config.AppConfig.DbConfig;
import edu.ukma.ssdm.teamspectrum.config.AppConfig.JwtConfig;
import edu.ukma.ssdm.teamspectrum.config.AppConfig.RetryConfig;
import edu.ukma.ssdm.teamspectrum.logic.auth.UserRepository;
import edu.ukma.ssdm.teamspectrum.logic.hotel.HotelRepository;
import edu.ukma.ssdm.teamspectrum.logic.room.RoomPage;
import edu.ukma.ssdm.teamspectrum.logic.room.RoomRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jooq.DSLContext;
import org.jooq.Isolation;
import org.jooq.TransactionalCallable;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(DatabaseRetryTest.Profile.class)
class DatabaseRetryTest {
  @Inject AppConfig config;
  @Inject JwtConfig jwtConfig;
  @Inject DbConfig dbConfig;
  @Inject RetryConfig retryConfig;
  @InjectMock DSLContext dsl;
  @Inject UserRepository users;
  @Inject HotelRepository hotels;
  @Inject RoomRepository rooms;

  public static class Profile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of(
          "DB_URL", "jdbc:postgresql://127.0.0.1:1/retry_test",
          "DB_USERNAME", "test",
          "DB_PASSWORD", "test",
          "JWT_SECRET", "test-secret-for-database-retry-0123456789",
          "quarkus.flyway.migrate-at-start", "false",
          "quarkus.datasource.jdbc.initial-size", "0",
          "quarkus.datasource.jdbc.min-size", "0",
          "app.db.retry.max-retries", "2",
          "app.db.query-timeout-seconds", "7",
          "app.db.transaction-timeout", "PT12S");
    }
  }

  @Test
  void retriesWrappedTimeoutsUpToConfiguredLimitThenPropagatesOriginalFailure() {
    var failure = new DataAccessException("query", new SQLTimeoutException());
    when(dsl.selectFrom(USERS)).thenThrow(failure);

    assertSame(failure, assertThrows(DataAccessException.class, () -> users.findByEmail("a@b.c")));
    verify(dsl, times(3)).selectFrom(USERS);
  }

  @Test
  void retriesPostgresStatementTimeoutsInHotelRepository() {
    var failure = new DataAccessException("query", new SQLException("statement timeout", "57014"));
    when(dsl.selectFrom(HOTELS)).thenThrow(failure);

    assertSame(
        failure, assertThrows(DataAccessException.class, () -> hotels.findById(UUID.randomUUID())));
    verify(dsl, times(3)).selectFrom(HOTELS);
  }

  @Test
  void retriesPostgresTransactionTimeoutsInRoomRepository() {
    var failure =
        new DataAccessException("query", new SQLException("transaction timeout", "25P04"));
    when(dsl.selectFrom(ROOMS)).thenThrow(failure);

    assertSame(
        failure,
        assertThrows(
            DataAccessException.class, () -> rooms.findById(UUID.randomUUID(), UUID.randomUUID())));
    verify(dsl, times(3)).selectFrom(ROOMS);
  }

  @Test
  void exposesTheSameNestedConfigurationInstancesWithOverriddenSettings() {
    assertSame(config.jwt(), jwtConfig);
    assertSame(config.db(), dbConfig);
    assertSame(dbConfig.retry(), retryConfig);
    assertEquals(2, retryConfig.maxRetries());
    assertEquals(7, dbConfig.queryTimeoutSeconds());
    assertEquals(java.time.Duration.ofSeconds(12), dbConfig.transactionTimeout().value());
  }

  @Test
  void restartsWholeTransactionAndReturnsResultWhenRetrySucceeds() {
    var failure = new DataAccessException("query", new SQLTimeoutException());
    var expected = new RoomPage(List.of(), 0);
    when(dsl.transactionResult(
            org.mockito.ArgumentMatchers.<TransactionalCallable<RoomPage>>any(),
            eq(Isolation.REPEATABLE_READ)))
        .thenThrow(failure)
        .thenReturn(expected);

    assertSame(expected, rooms.findPage(UUID.randomUUID(), null, 0, 10));
    verify(dsl, times(2))
        .transactionResult(
            org.mockito.ArgumentMatchers.<TransactionalCallable<RoomPage>>any(),
            eq(Isolation.REPEATABLE_READ));
  }

  @Test
  void doesNotRetryConstraintViolations() {
    var failure = new DataAccessException("query", new SQLException("duplicate key", "23505"));
    when(dsl.selectFrom(USERS)).thenThrow(failure);

    assertSame(failure, assertThrows(DataAccessException.class, () -> users.findByEmail("a@b.c")));
    verify(dsl).selectFrom(USERS);
  }
}
