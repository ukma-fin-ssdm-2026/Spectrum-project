/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.common;

import java.time.Duration;
import java.util.Objects;

public record PositiveDuration(Duration value) {
  public PositiveDuration {
    Objects.requireNonNull(value, "Duration must not be null");
    int millis = Math.toIntExact(value.toMillis());
    if (value.isZero() || value.isNegative() || millis == 0) {
      throw new IllegalArgumentException("Duration must be be at least one millisecond");
    }
  }
}
