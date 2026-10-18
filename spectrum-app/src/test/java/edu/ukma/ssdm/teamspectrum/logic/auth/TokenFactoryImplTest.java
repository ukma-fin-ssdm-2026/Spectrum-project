/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import static org.junit.jupiter.api.Assertions.*;

import edu.ukma.ssdm.teamspectrum.api.auth.IssuedToken;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TokenFactoryImplTest {

  @Test
  void createsSignedJwtWithUserClaimsAndFifteenMinuteLifetime() {
    TokenFactoryImpl factory = new TokenFactoryImpl("test-secret-that-is-long-enough-for-hmac");
    User user = new User(UUID.randomUUID(), "person@example.com", "hash", Role.WORKER);

    IssuedToken issued = factory.create(user);

    String[] segments = issued.value().split("\\.");
    assertEquals(3, segments.length);
    String payload = new String(Base64.getUrlDecoder().decode(segments[1]), StandardCharsets.UTF_8);
    assertTrue(payload.contains(user.id().toString()));
    assertTrue(payload.contains(user.email()));
    assertTrue(payload.contains("WORKER"));
    assertTrue(payload.contains("spectrum-app"));
    assertEquals(900, issued.expiresIn());
  }
}
