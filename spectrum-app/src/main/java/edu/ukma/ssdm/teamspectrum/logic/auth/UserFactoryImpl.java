/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class UserFactoryImpl implements UserFactory {

  @Override
  public String normalizeEmail(String email) {
    return email.trim().toLowerCase(java.util.Locale.ROOT);
  }

  @Override
  public User create(String email, String password) {
    return new User(UUID.randomUUID(), email, BcryptUtil.bcryptHash(password), Role.CLIENT);
  }
}
