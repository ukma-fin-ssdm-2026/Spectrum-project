/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import edu.ukma.ssdm.teamspectrum.api.auth.IssuedToken;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.Set;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class TokenFactoryImpl implements TokenFactory {

  private static final String issuer = "spectrum-app";
  private static final Duration TOKEN_LIFETIME = Duration.ofMinutes(15);

  private final String jwtSecret;

  public TokenFactoryImpl(@ConfigProperty(name = "JWT_SECRET") String jwtSecret) {
    this.jwtSecret = jwtSecret;
  }

  @Override
  public IssuedToken create(User user) {
    String token =
        Jwt.issuer(issuer)
            .subject(user.id().toString())
            .upn(user.email())
            .groups(Set.of(user.role().toString()))
            .expiresIn(TOKEN_LIFETIME)
            .jws()
            .signWithSecret(jwtSecret);
    return new IssuedToken(token, TOKEN_LIFETIME.toSeconds());
  }
}
