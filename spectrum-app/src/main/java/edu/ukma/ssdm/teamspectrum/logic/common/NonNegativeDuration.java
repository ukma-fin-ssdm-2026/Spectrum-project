/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.common;

import java.time.Duration;
import java.util.Objects;

/** A duration representable in milliseconds, either zero or at least one millisecond. */
public record NonNegativeDuration(Duration value) {
  public NonNegativeDuration {
    Objects.requireNonNull(value, "Duration must not be null");
    long millis = value.toMillis();
    if (value.isNegative() || (!value.isZero() && millis == 0)) {
      throw new IllegalArgumentException("Duration must be zero or at least one millisecond");
    }
  }
}
