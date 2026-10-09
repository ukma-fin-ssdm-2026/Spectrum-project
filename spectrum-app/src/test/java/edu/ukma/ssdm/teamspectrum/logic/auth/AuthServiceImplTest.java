/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import edu.ukma.ssdm.teamspectrum.api.auth.IssuedToken;
import edu.ukma.ssdm.teamspectrum.api.auth.LoginRequest;
import edu.ukma.ssdm.teamspectrum.api.auth.RegisterRequest;
import io.quarkus.elytron.security.common.BcryptUtil;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuthServiceImplTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final UserFactory userFactory = mock(UserFactory.class);
  private final TokenFactory tokenFactory = mock(TokenFactory.class);
  private final AuthServiceImpl service =
      new AuthServiceImpl(userRepository, userFactory, tokenFactory);

  @Test
  void registerNormalizesEmailSavesUserAndReturnsIssuedToken() {
    User user = new User(UUID.randomUUID(), "person@example.com", "hash", Role.CLIENT);
    IssuedToken token = new IssuedToken("issued", 900);
    var password = "password123456";
    when(userFactory.create(user.email(), password)).thenReturn(user);
    when(userRepository.findByEmail(user.email())).thenReturn(Optional.empty());
    when(userRepository.add(user)).thenReturn(true);
    when(tokenFactory.create(user)).thenReturn(token);

    IssuedToken result = service.register(new RegisterRequest("  PERSON@Example.COM ", password));

    assertSame(token, result);
    verify(userFactory).create(user.email(), password);
    verify(userRepository).add(user);
    verify(tokenFactory).create(user);
  }

  @Test
  void registerThrowsWhenEmailAlreadyExists() {
    var user = new User(UUID.randomUUID(), "person@example.com", "hash", Role.CLIENT);
    var password = "password123456";
    when(userFactory.create(user.email(), password)).thenReturn(user);
    when(userRepository.findByEmail(user.email())).thenReturn(Optional.of(user));

    assertThrows(
        EmailAlreadyRegisteredException.class,
        () -> service.register(new RegisterRequest(user.email(), password)));
    verifyNoInteractions(tokenFactory);
  }

  @Test
  void loginFindsNormalizedEmailAndReturnsTokenForMatchingPassword() {
    User user =
        new User(
            UUID.randomUUID(), "person@example.com", BcryptUtil.bcryptHash("correct"), Role.CLIENT);
    IssuedToken token = new IssuedToken("issued", 900);
    when(userRepository.findByEmail(user.email())).thenReturn(Optional.of(user));
    when(tokenFactory.create(user)).thenReturn(token);

    IssuedToken result = service.login(new LoginRequest(" PERSON@EXAMPLE.COM ", "correct"));

    assertSame(token, result);
    verify(userRepository).findByEmail(user.email());
    verify(tokenFactory).create(user);
  }

  @Test
  void loginRejectsMissingUser() {
    var email = "person@example.com";
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
    assertThrows(
        InvalidCredentialsException.class, () -> service.login(new LoginRequest(email, "correct")));
    verifyNoInteractions(tokenFactory);
  }

  @Test
  void loginRejectsInvalidPassword() {
    var email = "person@example.com";
    var user = new User(UUID.randomUUID(), email, BcryptUtil.bcryptHash("right"), Role.CLIENT);
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    assertThrows(
        InvalidCredentialsException.class, () -> service.login(new LoginRequest(email, "wrong")));
    verifyNoInteractions(tokenFactory);
  }
}
