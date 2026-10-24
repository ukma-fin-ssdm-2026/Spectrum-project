/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserRepositoryStub implements UserRepository {
  private final ConcurrentMap<String, User> users = new ConcurrentHashMap<>();

  @Override
  public Optional<User> findByEmail(String email) {
    return Optional.ofNullable(users.get(email));
  }

  @Override
  public boolean add(User user) {
    return users.putIfAbsent(user.email(), user) == null;
  }
}
