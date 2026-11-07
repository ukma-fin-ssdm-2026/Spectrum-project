/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class NonNegativeDurationTest {

  static Stream<Duration> validValues() {
    return Stream.of(
        Duration.ZERO, Duration.ofMillis(1), Duration.ofMillis(100), Duration.ofMinutes(1));
  }

  @ParameterizedTest
  @MethodSource("validValues")
  void acceptsZeroAndDurationsBeyondIntegerMilliseconds(Duration duration) {
    assertEquals(duration, new NonNegativeDuration(duration).value());
  }

  static Stream<Duration> negativeValues() {
    return Stream.of(Duration.ofNanos(-1), Duration.ofMillis(-100));
  }

  @ParameterizedTest
  @MethodSource("negativeValues")
  void rejectsNegativeValues(Duration duration) {
    assertThrows(IllegalArgumentException.class, () -> new NonNegativeDuration(duration));
  }

  @Test
  void rejectsNullOverflowingDurations() {
    assertThrows(NullPointerException.class, () -> new NonNegativeDuration(null));
  }

  @Test
  void rejectsOverflowingDurations() {
    assertThrows(
        ArithmeticException.class,
        () -> new NonNegativeDuration(Duration.ofSeconds(Long.MAX_VALUE)));
  }
}
