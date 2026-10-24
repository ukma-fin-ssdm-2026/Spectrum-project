/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import edu.ukma.ssdm.teamspectrum.api.auth.IssuedToken;
import edu.ukma.ssdm.teamspectrum.api.auth.LoginRequest;
import edu.ukma.ssdm.teamspectrum.api.auth.RegisterRequest;
import io.quarkus.elytron.security.common.BcryptUtil;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

class AuthServiceImplTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final UserFactory userFactory = mock(UserFactory.class);
  private final TokenFactory tokenFactory = mock(TokenFactory.class);
  private final AuthServiceImpl service =
      new AuthServiceImpl(userRepository, userFactory, tokenFactory);

  @Test
  void registerSavesUserAndReturnsIssuedToken() {
    User user = new User(UUID.randomUUID(), "person@example.com", "hash", Role.CLIENT);
    IssuedToken token = new IssuedToken("issued", 900);
    var password = "password123456";
    when(userFactory.normalizeEmail(user.email())).thenReturn(user.email());
    when(userFactory.create(user.email(), password)).thenReturn(user);
    when(userRepository.findByEmail(user.email())).thenReturn(Optional.empty());
    when(userRepository.add(user)).thenReturn(true);
    when(tokenFactory.create(user)).thenReturn(token);

    IssuedToken result = service.register(new RegisterRequest(user.email(), password));

    assertSame(token, result);
    verify(userFactory).create(user.email(), password);
    verify(userFactory).normalizeEmail(user.email());
    verify(userRepository).add(user);
    verify(tokenFactory).create(user);
  }

  @Test
  void registerDoesNotLogPassword() {
    var email = "person@example.com";
    var password = "secret-password-123";
    var user = new User(UUID.randomUUID(), email, "hash", Role.CLIENT);
    when(userFactory.normalizeEmail(email)).thenReturn(email);
    when(userFactory.create(email, password)).thenReturn(user);
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    var logger = Logger.getLogger(AuthServiceImpl.class.getName());
    var previousLevel = logger.getLevel();
    var logged = new StringBuilder();
    var handler =
        new Handler() {
          @Override
          public void publish(LogRecord record) {
            logged.append(record.getMessage()).append(' ');
            if (record.getParameters() != null) {
              logged.append(Arrays.toString(record.getParameters())).append(' ');
            }
          }

          @Override
          public void flush() {}

          @Override
          public void close() {}
        };
    logger.setLevel(Level.FINE);
    logger.addHandler(handler);
    try {
      service.register(new RegisterRequest(email, password));
    } finally {
      logger.removeHandler(handler);
      logger.setLevel(previousLevel);
    }

    assertFalse(
        logged.toString().contains(password), "Register logs must not contain the user's password");
  }

  @Test
  void registerThrowsWhenEmailAlreadyExists() {
    var user = new User(UUID.randomUUID(), "person@example.com", "hash", Role.CLIENT);
    var password = "password123456";
    when(userFactory.normalizeEmail(user.email())).thenReturn(user.email());
    when(userFactory.create(user.email(), password)).thenReturn(user);
    when(userRepository.findByEmail(user.email())).thenReturn(Optional.of(user));

    assertThrows(
        EmailAlreadyRegisteredException.class,
        () -> service.register(new RegisterRequest(user.email(), password)));
    verifyNoInteractions(tokenFactory);
  }

  @Test
  void loginReturnsTokenForMatchingPassword() {
    User user =
        new User(
            UUID.randomUUID(), "person@example.com", BcryptUtil.bcryptHash("correct"), Role.CLIENT);
    IssuedToken token = new IssuedToken("issued", 900);
    when(userRepository.findByEmail(user.email())).thenReturn(Optional.of(user));
    when(tokenFactory.create(user)).thenReturn(token);
    when(userFactory.normalizeEmail(user.email())).thenReturn(user.email());

    IssuedToken result = service.login(new LoginRequest(user.email(), "correct"));

    assertSame(token, result);
    verify(userRepository).findByEmail(user.email());
    verify(userFactory).normalizeEmail(user.email());
    verify(tokenFactory).create(user);
  }

  @Test
  void loginRejectsMissingUser() {
    var email = "person@example.com";
    when(userFactory.normalizeEmail(email)).thenReturn(email);
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
    assertThrows(
        InvalidCredentialsException.class, () -> service.login(new LoginRequest(email, "correct")));
    verifyNoInteractions(tokenFactory);
  }

  @Test
  void loginRejectsInvalidPassword() {
    var email = "person@example.com";
    when(userFactory.normalizeEmail(email)).thenReturn(email);
    var user = new User(UUID.randomUUID(), email, BcryptUtil.bcryptHash("right"), Role.CLIENT);
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    assertThrows(
        InvalidCredentialsException.class, () -> service.login(new LoginRequest(email, "wrong")));
    verifyNoInteractions(tokenFactory);
  }
}
