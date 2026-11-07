/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.config;

import edu.ukma.ssdm.teamspectrum.config.AppConfig.DbConfig;
import edu.ukma.ssdm.teamspectrum.config.AppConfig.JwtConfig;
import edu.ukma.ssdm.teamspectrum.config.AppConfig.RetryConfig;
import edu.ukma.ssdm.teamspectrum.logic.common.NonNegativeDuration;
import edu.ukma.ssdm.teamspectrum.logic.common.PositiveDuration;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import java.time.Duration;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AppConfigProducer {
  @Produces
  @Singleton
  @Startup
  AppConfig appConfig(
      @ConfigProperty(name = "app.jwt.secret") String secret,
      @ConfigProperty(name = "app.jwt.issuer") String issuer,
      @ConfigProperty(name = "app.jwt.token-lifetime") Duration tokenLifetime,
      @ConfigProperty(name = "app.db.query-timeout-seconds") int queryTimeoutSeconds,
      @ConfigProperty(name = "app.db.transaction-timeout") Duration transactionTimeout,
      @ConfigProperty(name = "app.db.retry.max-retries") int maxRetries,
      @ConfigProperty(name = "app.db.retry.delay-millis") long delayMillis,
      @ConfigProperty(name = "app.db.retry.jitter-millis") long jitterMillis,
      @ConfigProperty(name = "app.db.retry.backoff-factor") int backoffFactor,
      @ConfigProperty(name = "app.db.retry.max-delay-millis") long maxDelayMillis) {
    return new AppConfig(
        new JwtConfig(secret, issuer, new PositiveDuration(tokenLifetime)),
        new DbConfig(
            new PositiveDuration(Duration.ofSeconds(queryTimeoutSeconds)),
            new PositiveDuration(transactionTimeout),
            new RetryConfig(
                maxRetries,
                new NonNegativeDuration(Duration.ofMillis(delayMillis)),
                new NonNegativeDuration(Duration.ofMillis(jitterMillis)),
                backoffFactor,
                new NonNegativeDuration(Duration.ofMillis(maxDelayMillis)))));
  }

  @Produces
  @Singleton
  JwtConfig jwtConfig(AppConfig config) {
    return config.jwt();
  }

  @Produces
  @Singleton
  DbConfig dbConfig(AppConfig config) {
    return config.db();
  }

  @Produces
  @Singleton
  RetryConfig retryConfig(AppConfig config) {
    return config.db().retry();
  }
}
