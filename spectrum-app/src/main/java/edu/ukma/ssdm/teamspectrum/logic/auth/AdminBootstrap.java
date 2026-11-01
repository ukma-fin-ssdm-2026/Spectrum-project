/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.util.Locale;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Creates the initial administrator when explicitly enabled by the deployment operator.
 */
@ApplicationScoped
public class AdminBootstrap {
  private final UserRepository userRepository;

  @ConfigProperty(name = "bootstrap.admin.enabled", defaultValue = "false")
  boolean enabled;

  @ConfigProperty(name = "bootstrap.admin.email", defaultValue = "")
  String email;

  @ConfigProperty(name = "bootstrap.admin.password", defaultValue = "")
  String password;

  @Inject
  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "This is an intentionally shared repository dependency.")
  public AdminBootstrap(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  void onStart(@Observes StartupEvent event) {
    if (!enabled || userRepository.existsByRole(Role.ADMIN)) {
      return;
    }

    if (email.isBlank() || password.isBlank()) {
      throw new IllegalStateException(
          "Bootstrap admin email and password must be configured when bootstrap is enabled");
    }

    String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
    User admin =
        new User(UUID.randomUUID(), normalizedEmail, BcryptUtil.bcryptHash(password), Role.ADMIN);

    if (!userRepository.add(admin)) {
      throw new IllegalStateException(
          "Could not create bootstrap admin; the configured email may already be in use");
    }
  }
}
