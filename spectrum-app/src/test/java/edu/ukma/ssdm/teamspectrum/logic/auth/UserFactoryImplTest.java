/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.elytron.security.common.BcryptUtil;
import org.junit.jupiter.api.Test;

class UserFactoryImplTest {

  @Test
  void normalizesEmail() {
    var expected = "person@example.com";
    assertEquals(expected, new UserFactoryImpl().normalizeEmail(" PERSON@Example.COM "));
    assertEquals(expected, new UserFactoryImpl().normalizeEmail("person@example.com"));
  }

  @Test
  void createsClientWithEmailAndHashedPassword() {
    var email = "person@example.com";
    var password = "password123456";
    User user = new UserFactoryImpl().create(email, password);

    assertNotNull(user.id());
    assertEquals(email, user.email());
    assertEquals(Role.CLIENT, user.role());
    assertNotEquals(password, user.passwordHash());
    assertTrue(BcryptUtil.matches(password, user.passwordHash()));
  }
}
