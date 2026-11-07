/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import edu.ukma.ssdm.teamspectrum.api.auth.IssuedToken;
import edu.ukma.ssdm.teamspectrum.config.AppConfig.JwtConfig;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Set;

@ApplicationScoped
public class TokenFactoryImpl implements TokenFactory {

  private final JwtConfig config;

  public TokenFactoryImpl(JwtConfig config) {
    this.config = config;
  }

  @Override
  public IssuedToken create(User user) {
    String token =
        Jwt.issuer(config.issuer())
            .subject(user.id().toString())
            .upn(user.email())
            .groups(Set.of(user.role().toString()))
            .expiresIn(config.tokenLifetime().value())
            .jws()
            .signWithSecret(config.secret());
    return new IssuedToken(token, config.tokenLifetime().value().toSeconds());
  }
}
