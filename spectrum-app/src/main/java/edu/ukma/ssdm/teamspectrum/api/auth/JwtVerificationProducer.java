/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.auth;

import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import javax.crypto.spec.SecretKeySpec;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Verifies tokens signed by {@code TokenFactoryImpl}. SmallRye JWT reads {@code
 * smallrye.jwt.verify.secretkey} only as a JWK, and in dev and test mode Quarkus replaces it with a
 * generated RSA key, so the HS256 secret is set here.
 */
@ApplicationScoped
public class JwtVerificationProducer {
  static final String issuer = "spectrum-app";

  @Produces
  @Alternative
  @Priority(1)
  @ApplicationScoped
  JWTAuthContextInfo jwtAuthContextInfo(@ConfigProperty(name = "JWT_SECRET") String jwtSecret) {
    var key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    var info = new JWTAuthContextInfo(key, issuer);
    info.setSignatureAlgorithm(Set.of(SignatureAlgorithm.HS256));
    info.setTokenHeader("Cookie");
    info.setTokenCookie(AuthController.authTokenName);
    return info;
  }
}
