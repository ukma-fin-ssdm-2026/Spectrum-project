/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import static org.junit.jupiter.api.Assertions.*;

import edu.ukma.ssdm.teamspectrum.api.auth.IssuedToken;
import edu.ukma.ssdm.teamspectrum.config.AppConfig.JwtConfig;
import edu.ukma.ssdm.teamspectrum.logic.common.PositiveDuration;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class TokenFactoryImplTest {

  @Test
  void createsSignedJwtWithUserClaimsAndConfiguredIssuerAndLifetime() {
    TokenFactoryImpl factory =
        new TokenFactoryImpl(
            new JwtConfig(
                "test-secret-that-is-long-enough-for-hmac",
                "custom-issuer",
                new PositiveDuration(Duration.ofMinutes(2))));
    User user = new User(UUID.randomUUID(), "person@example.com", "hash", Role.WORKER);

    IssuedToken issued = factory.create(user);

    String[] segments = issued.value().split("\\.");
    assertEquals(3, segments.length);
    String payload = new String(Base64.getUrlDecoder().decode(segments[1]), StandardCharsets.UTF_8);
    assertTrue(payload.contains(user.id().toString()));
    assertTrue(payload.contains(user.email()));
    assertTrue(payload.contains("WORKER"));
    assertTrue(payload.contains("custom-issuer"));
    assertEquals(120, issued.expiresIn());
  }
}
