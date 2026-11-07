/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.common;

import java.time.Duration;
import java.util.Objects;

/** A positive duration representable in milliseconds, with at least one millisecond. */
public record PositiveDuration(Duration value) {
  public PositiveDuration {
    Objects.requireNonNull(value, "Duration must not be null");
    long millis = value.toMillis();
    if (value.isZero() || value.isNegative() || millis == 0) {
      throw new IllegalArgumentException("Duration must be at least one millisecond");
    }
  }
}
