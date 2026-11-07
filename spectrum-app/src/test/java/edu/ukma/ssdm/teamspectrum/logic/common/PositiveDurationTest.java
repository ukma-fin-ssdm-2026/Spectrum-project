/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class PositiveDurationTest {
  @Test
  void preservesPositiveDurationsIncludingNanoseconds() {
    Duration value = Duration.ofMillis(1);
    assertEquals(value, new PositiveDuration(value).value());
  }

  static Stream<Duration> invalidDurations() {
    return Stream.of(Duration.ZERO, Duration.ofNanos(-1), Duration.ofNanos(1));
  }

  @ParameterizedTest
  @MethodSource("invalidDurations")
  void rejectsInvalidDurations(Duration duration) {
    assertThrows(IllegalArgumentException.class, () -> new PositiveDuration(duration));
  }

  @Test
  void rejectsNull() {
    assertThrows(NullPointerException.class, () -> new PositiveDuration(null));
  }
}
