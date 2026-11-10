/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import java.util.Optional;

public interface UserRepository {
  Optional<User> findByEmail(String email);

  boolean existsByRole(Role role);

  boolean add(User user);
}
