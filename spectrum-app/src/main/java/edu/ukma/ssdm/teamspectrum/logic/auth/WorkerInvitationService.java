/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import edu.ukma.ssdm.teamspectrum.api.auth.IssuedToken;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class WorkerInvitationService {
  private static final Duration INVITATION_LIFETIME = Duration.ofDays(7);

  private final Logger logger = LoggerFactory.getLogger(WorkerInvitationService.class);
  private final SecureRandom random = new SecureRandom();
  private final WorkerInvitationRepository invitationRepository;
  private final UserRepository userRepository;
  private final UserFactory userFactory;
  private final TokenFactory tokenFactory;

  @ConfigProperty(
      name = "worker.invitation.registration-url",
      defaultValue = "http://localhost:8080/register/worker")
  String registrationUrl;

  @Inject
  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "These injected service dependencies are intentionally shared.")
  public WorkerInvitationService(
      WorkerInvitationRepository invitationRepository,
      UserRepository userRepository,
      UserFactory userFactory,
      TokenFactory tokenFactory) {
    this.invitationRepository = invitationRepository;
    this.userRepository = userRepository;
    this.userFactory = userFactory;
    this.tokenFactory = tokenFactory;
  }

  public WorkerInvitation createInvitation(String email) {
    String normalizedEmail = userFactory.normalizeEmail(email);
    if (userRepository.findByEmail(normalizedEmail).isPresent()) {
      throw new EmailAlreadyRegisteredException();
    }

    String token = generateToken();
    Instant expiresAt = Instant.now().plus(INVITATION_LIFETIME);
    invitationRepository.create(normalizedEmail, hashToken(token), expiresAt);
    String url =
        UriBuilder.fromUri(URI.create(registrationUrl))
            .queryParam("token", token)
            .build()
            .toString();
    logger.info("Created worker invitation for {}", normalizedEmail);
    return new WorkerInvitation(url, expiresAt);
  }

  public IssuedToken completeRegistration(String token, String password) {
    String tokenHash = hashToken(token);
    if (!invitationRepository.isActive(tokenHash)) {
      throw new InvalidWorkerInvitationException();
    }
    User user =
        invitationRepository.consumeAndRegister(
            tokenHash, UUID.randomUUID(), BcryptUtil.bcryptHash(password));
    logger.info("Registered worker account: {}", user.email());
    return tokenFactory.create(user);
  }

  private String generateToken() {
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private static String hashToken(String token) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is unavailable", e);
    }
  }
}
