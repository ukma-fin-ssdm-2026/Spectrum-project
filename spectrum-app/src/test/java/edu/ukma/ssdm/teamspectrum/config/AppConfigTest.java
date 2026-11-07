/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.ukma.ssdm.teamspectrum.config.AppConfig.DbConfig;
import edu.ukma.ssdm.teamspectrum.config.AppConfig.JwtConfig;
import edu.ukma.ssdm.teamspectrum.config.AppConfig.RetryConfig;
import edu.ukma.ssdm.teamspectrum.logic.common.NonNegativeDuration;
import edu.ukma.ssdm.teamspectrum.logic.common.PositiveDuration;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class AppConfigTest {
  @Test
  void permitsZeroRetryDelaysAndLongTokenLifetimes() {
    var zero = nonNegativeMillis(0);
    var retry = new RetryConfig(0, zero, zero, 1, zero);
    assertEquals(Duration.ZERO, retry.delay().value());
    var jwt = new JwtConfig("secret", "spectrum-app", new PositiveDuration(Duration.ofDays(30)));
    assertEquals(Duration.ofDays(30), jwt.tokenLifetime().value());
  }

  @Test
  void rejectsTimeoutsThatOverflowDatabaseApiLimits() {
    var retry =
        new RetryConfig(
            3, nonNegativeMillis(200), nonNegativeMillis(100), 2, nonNegativeMillis(1000));
    var normalTimeout = new PositiveDuration(Duration.ofSeconds(5));
    assertThrows(
        ArithmeticException.class,
        () ->
            new DbConfig(
                new PositiveDuration(Duration.ofSeconds((long) Integer.MAX_VALUE + 1)),
                normalTimeout,
                retry));
    assertThrows(
        ArithmeticException.class,
        () ->
            new DbConfig(
                normalTimeout,
                new PositiveDuration(Duration.ofMillis((long) Integer.MAX_VALUE + 1)),
                retry));
  }

  private static NonNegativeDuration nonNegativeMillis(long value) {
    return new NonNegativeDuration(Duration.ofMillis(value));
  }

  @Test
  void rejectsSettingsThatWouldDisableTimeoutsOrCreateInvalidTokens() {
    var retry =
        new RetryConfig(
            3, nonNegativeMillis(200), nonNegativeMillis(100), 2, nonNegativeMillis(1000));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new DbConfig(
                new PositiveDuration(Duration.ofMillis(999)),
                new PositiveDuration(Duration.ofSeconds(10)),
                retry));
    assertThrows(
        IllegalArgumentException.class,
        () -> new JwtConfig(" ", "spectrum-app", new PositiveDuration(Duration.ofMinutes(15))));
    assertThrows(
        IllegalArgumentException.class,
        () -> new JwtConfig("secret", " ", new PositiveDuration(Duration.ofMinutes(15))));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new JwtConfig("secret", "spectrum-app", new PositiveDuration(Duration.ofMillis(999))));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new RetryConfig(
                -1, nonNegativeMillis(200), nonNegativeMillis(100), 2, nonNegativeMillis(1000)));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new RetryConfig(
                3, nonNegativeMillis(200), nonNegativeMillis(100), 2, nonNegativeMillis(100)));
  }

  @Test
  void doesNotExposeJwtSecretWhenConfigurationIsPrinted() {
    var config =
        new AppConfig(
            new JwtConfig(
                "sensitive-test-secret",
                "spectrum-app",
                new PositiveDuration(Duration.ofMinutes(15))),
            new DbConfig(
                new PositiveDuration(Duration.ofSeconds(5)),
                new PositiveDuration(Duration.ofSeconds(10)),
                new RetryConfig(
                    3,
                    nonNegativeMillis(200),
                    nonNegativeMillis(100),
                    2,
                    nonNegativeMillis(1000))));
    assertFalse(config.toString().contains("sensitive-test-secret"));
  }
}
