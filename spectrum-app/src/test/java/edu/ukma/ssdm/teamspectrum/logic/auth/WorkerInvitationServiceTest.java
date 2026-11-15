/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import edu.ukma.ssdm.teamspectrum.api.auth.IssuedToken;
import io.quarkus.elytron.security.common.BcryptUtil;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

@Tag("unit")
class WorkerInvitationServiceTest {
  private static final String EMAIL = "worker@example.com";
  private static final String TOKEN = "invitation-token";
  private static final String PASSWORD = "worker-password";

  private WorkerInvitationRepository invitationRepository;
  private UserRepository userRepository;
  private UserFactory userFactory;
  private TokenFactory tokenFactory;
  private WorkerInvitationService service;

  @BeforeEach
  void setUp() {
    invitationRepository = mock(WorkerInvitationRepository.class);
    userRepository = mock(UserRepository.class);
    userFactory = mock(UserFactory.class);
    tokenFactory = mock(TokenFactory.class);
    service =
        new WorkerInvitationService(
            invitationRepository, userRepository, userFactory, tokenFactory);
    service.registrationUrl = "https://example.com/register/worker?source=admin";
  }

  @Test
  void createInvitationNormalizesEmailAndStoresTokenHashWithSevenDayLifetime() {
    String inputEmail = " Worker@Example.com ";
    when(userFactory.normalizeEmail(inputEmail)).thenReturn(EMAIL);
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
    Instant before = Instant.now();

    WorkerInvitation invitation = service.createInvitation(inputEmail);

    Instant after = Instant.now();
    URI registrationUri = URI.create(invitation.registrationUrl());
    assertEquals("https", registrationUri.getScheme());
    assertEquals("example.com", registrationUri.getHost());
    assertEquals("/register/worker", registrationUri.getPath());
    assertTrue(registrationUri.getRawQuery().startsWith("source=admin&token="));
    String token = registrationUri.getRawQuery().substring("source=admin&token=".length());
    assertTrue(token.matches("[A-Za-z0-9_-]{43}"));
    assertEquals(32, Base64.getUrlDecoder().decode(token).length);
    var createdAt = ArgumentCaptor.forClass(Instant.class);
    var expiresAt = ArgumentCaptor.forClass(Instant.class);
    verify(invitationRepository)
        .create(eq(EMAIL), eq(hashToken(token)), createdAt.capture(), expiresAt.capture());
    assertFalse(createdAt.getValue().isBefore(before));
    assertFalse(createdAt.getValue().isAfter(after));
    assertEquals(Duration.ofDays(7), Duration.between(createdAt.getValue(), expiresAt.getValue()));
    assertEquals(expiresAt.getValue(), invitation.expiresAt());
    verify(userRepository).findByEmail(EMAIL);
    verifyNoInteractions(tokenFactory);
  }

  @Test
  void createInvitationRejectsAlreadyRegisteredEmail() {
    String inputEmail = " Worker@Example.com ";
    when(userFactory.normalizeEmail(inputEmail)).thenReturn(EMAIL);
    when(userRepository.findByEmail(EMAIL))
        .thenReturn(Optional.of(new User(UUID.randomUUID(), EMAIL, "hash", Role.CLIENT)));

    assertThrows(EmailAlreadyRegisteredException.class, () -> service.createInvitation(inputEmail));

    verify(userRepository).findByEmail(EMAIL);
    verifyNoInteractions(invitationRepository, tokenFactory);
  }

  @Test
  void completeRegistrationRejectsInactiveTokenWithoutRegisteringUser() {
    String tokenHash = hashToken(TOKEN);
    when(invitationRepository.isActive(eq(tokenHash), any(Instant.class))).thenReturn(false);

    assertThrows(
        InvalidWorkerInvitationException.class,
        () -> service.completeRegistration(TOKEN, PASSWORD));

    verify(invitationRepository).isActive(eq(tokenHash), any(Instant.class));
    Mockito.verifyNoMoreInteractions(invitationRepository);
    verifyNoInteractions(tokenFactory, userRepository, userFactory);
  }

  @Test
  void completeRegistrationHashesPasswordAndIssuesTokenForRegisteredWorker() {
    String tokenHash = hashToken(TOKEN);
    when(invitationRepository.isActive(eq(tokenHash), any(Instant.class))).thenReturn(true);
    User worker = new User(UUID.randomUUID(), EMAIL, "stored-hash", Role.WORKER);
    when(invitationRepository.consumeAndRegister(
            eq(tokenHash), any(UUID.class), anyString(), any(Instant.class)))
        .thenReturn(worker);
    IssuedToken issuedToken = new IssuedToken("access-token", 900);
    when(tokenFactory.create(worker)).thenReturn(issuedToken);
    Instant before = Instant.now();

    assertSame(issuedToken, service.completeRegistration(TOKEN, PASSWORD));

    Instant after = Instant.now();
    var checkedAt = ArgumentCaptor.forClass(Instant.class);
    var consumedAt = ArgumentCaptor.forClass(Instant.class);
    var passwordHash = ArgumentCaptor.forClass(String.class);
    InOrder order = Mockito.inOrder(invitationRepository, tokenFactory);
    order.verify(invitationRepository).isActive(eq(tokenHash), checkedAt.capture());
    order
        .verify(invitationRepository)
        .consumeAndRegister(
            eq(tokenHash), any(UUID.class), passwordHash.capture(), consumedAt.capture());
    order.verify(tokenFactory).create(worker);
    assertTrue(BcryptUtil.matches(PASSWORD, passwordHash.getValue()));
    assertFalse(checkedAt.getValue().isBefore(before));
    assertFalse(consumedAt.getValue().isBefore(checkedAt.getValue()));
    assertFalse(consumedAt.getValue().isAfter(after));
    verifyNoInteractions(userRepository, userFactory);
  }

  @Test
  void completeRegistrationDoesNotIssueTokenWhenInvitationBecomesInvalid() {
    String tokenHash = hashToken(TOKEN);
    when(invitationRepository.isActive(eq(tokenHash), any(Instant.class))).thenReturn(true);
    var failure = new InvalidWorkerInvitationException();
    when(invitationRepository.consumeAndRegister(
            eq(tokenHash), any(UUID.class), anyString(), any(Instant.class)))
        .thenThrow(failure);

    assertSame(
        failure,
        assertThrows(
            InvalidWorkerInvitationException.class,
            () -> service.completeRegistration(TOKEN, PASSWORD)));

    verifyNoInteractions(tokenFactory);
  }

  @Test
  void completeRegistrationDoesNotIssueTokenWhenEmailAlreadyRegistered() {
    String tokenHash = hashToken(TOKEN);
    when(invitationRepository.isActive(eq(tokenHash), any(Instant.class))).thenReturn(true);
    var failure = new EmailAlreadyRegisteredException();
    when(invitationRepository.consumeAndRegister(
            eq(tokenHash), any(UUID.class), anyString(), any(Instant.class)))
        .thenThrow(failure);

    assertSame(
        failure,
        assertThrows(
            EmailAlreadyRegisteredException.class,
            () -> service.completeRegistration(TOKEN, PASSWORD)));

    verifyNoInteractions(tokenFactory);
  }

  private static String hashToken(String token) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new AssertionError(e);
    }
  }
}
