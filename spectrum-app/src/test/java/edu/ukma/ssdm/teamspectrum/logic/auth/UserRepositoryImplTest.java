/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.Users.USERS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ukma.ssdm.teamspectrum.logic.common.repository.RepositoryTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(RepositoryTestProfile.class)
class UserRepositoryImplTest {
  @Inject UserRepository repository;
  @Inject DSLContext dsl;

  @BeforeEach
  @AfterEach
  void cleanUp() {
    dsl.deleteFrom(USERS).execute();
  }

  @Test
  void findByEmailReturnsEmptyWhenUserDoesNotExist() {
    String email = uniqueEmail();

    assertEquals(Optional.empty(), repository.findByEmail(email));
  }

  @Test
  void addAndFindByEmailRoundTrip() {
    User user = mkUser("hash", Role.WORKER);

    assertTrue(repository.add(user));
    assertEquals(Optional.of(user), repository.findByEmail(user.email()));
  }

  @Test
  void addReturnsFalseAndPreservesExistingUserWhenEmailAlreadyExists() {
    User original = mkUser("original-hash", Role.CLIENT);
    User duplicate = new User(UUID.randomUUID(), original.email(), "duplicate-hash", Role.ADMIN);

    assertTrue(repository.add(original));
    assertFalse(repository.add(duplicate));
    assertEquals(Optional.of(original), repository.findByEmail(original.email()));
  }

  private User mkUser(String passwordHash, Role role) {
    String email = uniqueEmail();
    return new User(UUID.randomUUID(), email, passwordHash, role);
  }

  private static String uniqueEmail() {
    return UUID.randomUUID() + "@example.com";
  }
}
