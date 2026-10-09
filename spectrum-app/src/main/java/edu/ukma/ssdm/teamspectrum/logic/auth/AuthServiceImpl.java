/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import edu.ukma.ssdm.teamspectrum.api.auth.IssuedToken;
import edu.ukma.ssdm.teamspectrum.api.auth.LoginRequest;
import edu.ukma.ssdm.teamspectrum.api.auth.RegisterRequest;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final UserFactory userFactory;
  private final TokenFactory tokenFactory;

  @Inject
  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "This is an intentionally shared repository dependency.")
  public AuthServiceImpl(
      UserRepository userRepository, UserFactory userFactory, TokenFactory tokenFactory) {
    this.userRepository = userRepository;
    this.userFactory = userFactory;
    this.tokenFactory = tokenFactory;
  }

  public IssuedToken register(RegisterRequest request) {
    String email = normalizeEmail(request.email());
    User user = userFactory.create(email, request.password());
    if (userRepository.findByEmail(user.email()).isPresent()) {
      throw new EmailAlreadyRegisteredException();
    } else {
      userRepository.add(user);
      return tokenFactory.create(user);
    }
  }

  public IssuedToken login(LoginRequest request) {
    var user =
        userRepository
            .findByEmail(normalizeEmail(request.email()))
            .filter(u -> BcryptUtil.matches(request.password(), u.passwordHash()))
            .orElseThrow(InvalidCredentialsException::new);
    return tokenFactory.create(user);
  }

  private static String normalizeEmail(String email) {
    return email.trim().toLowerCase(java.util.Locale.ROOT);
  }
}
