/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.config;

import edu.ukma.ssdm.teamspectrum.logic.common.NonNegativeDuration;
import edu.ukma.ssdm.teamspectrum.logic.common.PositiveDuration;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

/** Immutable application settings, assembled once at startup. */
public record AppConfig(JwtConfig jwt, DbConfig db) {
  public AppConfig {
    Objects.requireNonNull(jwt, "JWT configuration must not be null");
    Objects.requireNonNull(db, "Database configuration must not be null");
  }

  public record JwtConfig(String secret, String issuer, PositiveDuration tokenLifetime) {
    public JwtConfig {
      if (Objects.requireNonNull(secret, "JWT secret must not be null").isBlank()) {
        throw new IllegalArgumentException("JWT secret must not be blank");
      }
      if (Objects.requireNonNull(issuer, "JWT issuer must not be null").isBlank()) {
        throw new IllegalArgumentException("JWT issuer must not be blank");
      }
      if (Objects.requireNonNull(tokenLifetime, "Token lifetime must not be null")
              .value()
              .getSeconds()
          < 1) {
        throw new IllegalArgumentException("Token lifetime must be at least one second");
      }
    }

    @Override
    public @NonNull String toString() {
      return "JwtConfig[secret=<redacted>, issuer="
          + issuer
          + ", tokenLifetime="
          + tokenLifetime
          + "]";
    }
  }

  public record DbConfig(
      PositiveDuration queryTimeout, PositiveDuration transactionTimeout, RetryConfig retry) {
    public DbConfig {
      if (Objects.requireNonNull(queryTimeout, "Query timeout must not be null")
              .value()
              .getSeconds()
          < 1) {
        throw new IllegalArgumentException("Query timeout must be at least one second");
      }
      if (queryTimeout.value().toSeconds() > Integer.MAX_VALUE) {
        throw new ArithmeticException("Query timeout exceeds the integer second range");
      }
      Objects.requireNonNull(transactionTimeout, "Transaction timeout must not be null");
      if (transactionTimeout.value().toMillis() > Integer.MAX_VALUE) {
        throw new ArithmeticException("Transaction timeout exceeds the integer millisecond range");
      }
      Objects.requireNonNull(retry, "Retry configuration must not be null");
    }

    /** jOOQ accepts a query timeout in whole seconds. */
    public int queryTimeoutSeconds() {
      return Math.toIntExact(queryTimeout.value().toSeconds());
    }
  }

  public record RetryConfig(
      int maxRetries,
      NonNegativeDuration delay,
      NonNegativeDuration jitter,
      int backoffFactor,
      NonNegativeDuration maxDelay) {
    public RetryConfig {
      Objects.requireNonNull(delay, "Retry delay must not be null");
      Objects.requireNonNull(jitter, "Retry jitter must not be null");
      Objects.requireNonNull(maxDelay, "Maximum retry delay must not be null");
      if (maxRetries < 0 || backoffFactor < 1 || maxDelay.value().compareTo(delay.value()) < 0) {
        throw new IllegalArgumentException("Invalid database retry settings");
      }
    }
  }
}
