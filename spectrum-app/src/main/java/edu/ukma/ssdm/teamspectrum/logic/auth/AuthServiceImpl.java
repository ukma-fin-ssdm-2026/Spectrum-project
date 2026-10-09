/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import edu.ukma.ssdm.teamspectrum.api.auth.IssuedToken;
import edu.ukma.ssdm.teamspectrum.api.auth.LoginRequest;
import edu.ukma.ssdm.teamspectrum.api.auth.RegisterRequest;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AuthServiceImpl implements AuthService {

  private static final String issuer = "spectrum-app";
  private static final Duration TOKEN_LIFETIME = Duration.ofMinutes(15);

  private final UserRepository userRepository;
  private final String jwtSecret;

  @Inject
  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "This is an intentionally shared repository dependency.")
  public AuthServiceImpl(
      UserRepository userRepository, @ConfigProperty(name = "JWT_SECRET") String jwtSecret) {
    this.userRepository = userRepository;
    this.jwtSecret = jwtSecret;
  }

  public IssuedToken register(RegisterRequest request) {
    String email = normalizeEmail(request.email());
    User user =
        new User(UUID.randomUUID(), email, BcryptUtil.bcryptHash(request.password()), Role.CLIENT);
    if (!userRepository.add(user)) {
      throw new EmailAlreadyRegisteredException();
    }
    return issueToken(user);
  }

  public IssuedToken login(LoginRequest request) {
    var user =
        userRepository
            .findByEmail(normalizeEmail(request.email()))
            .filter(u -> BcryptUtil.matches(request.password(), u.passwordHash()))
            .orElseThrow(InvalidCredentialsException::new);
    return issueToken(user);
  }

  private IssuedToken issueToken(User user) {
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

  private static String normalizeEmail(String email) {
    return email.trim().toLowerCase(java.util.Locale.ROOT);
  }
}
