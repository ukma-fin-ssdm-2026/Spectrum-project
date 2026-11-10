/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.Users.USERS;
import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.WorkerInvitations.WORKER_INVITATIONS;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ukma.ssdm.teamspectrum.db.jooq.enums.UserRole;
import edu.ukma.ssdm.teamspectrum.db.jooq.tables.records.WorkerInvitationsRecord;
import edu.ukma.ssdm.teamspectrum.logic.common.repository.RepositoryTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestProfile(RepositoryTestProfile.class)
@Tag("integration")
class WorkerInvitationRepositoryImplTest {
  private static final Instant NOW = Instant.parse("2026-01-15T12:00:00Z");

  @Inject WorkerInvitationRepository repository;
  @Inject DSLContext dsl;

  @BeforeEach
  @AfterEach
  void cleanUp() {
    dsl.deleteFrom(WORKER_INVITATIONS).execute();
    dsl.deleteFrom(USERS).execute();
  }

  @Test
  void createPersistsActiveInvitation() {
    String email = uniqueEmail();
    Instant expiresAt = futureExpiration();

    repository.create(email, "token-hash", NOW, expiresAt);

    var invitation = fetchInvitation("token-hash");
    assertNotNull(invitation.getId());
    assertEquals(email, invitation.getEmail());
    assertEquals(expiresAt, invitation.getExpiresAt().toInstant());
    assertEquals(NOW, invitation.getCreatedAt().toInstant());
    assertNull(invitation.getConsumedAt());
    assertNull(invitation.getRevokedAt());
    assertTrue(repository.isActive("token-hash", NOW));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 1})
  void createReplacesPendingInvitationEvenWhenExpired(int hoursToAdd) {
    String email = uniqueEmail();
    repository.create(email, "old-token", NOW.minus(1, DAYS), NOW.plus(hoursToAdd, HOURS));
    UUID originalId = fetchInvitation("old-token").getId();
    Instant expiresAt = futureExpiration().plus(1, HOURS);

    repository.create(email, "new-token", NOW, expiresAt);

    var replacement = fetchInvitation("new-token");
    assertEquals(1, dsl.fetchCount(WORKER_INVITATIONS));
    assertEquals(originalId, replacement.getId());
    assertEquals(email, replacement.getEmail());
    assertEquals(expiresAt, replacement.getExpiresAt().toInstant());
    assertEquals(NOW, replacement.getCreatedAt().toInstant());
    assertFalse(repository.isActive("old-token", NOW));
    assertTrue(repository.isActive("new-token", NOW));
  }

  @Test
  void createPreservesConsumedInvitation() {
    String email = uniqueEmail();
    repository.create(email, "old-token", NOW.minus(1, DAYS), futureExpiration());
    dsl.update(WORKER_INVITATIONS)
        .set(WORKER_INVITATIONS.CONSUMED_AT, NOW.atOffset(ZoneOffset.UTC))
        .where(WORKER_INVITATIONS.TOKEN_HASH.eq("old-token"))
        .execute();
    var original = fetchInvitation("old-token");

    repository.create(email, "new-token", NOW, futureExpiration());

    assertEquals(2, dsl.fetchCount(WORKER_INVITATIONS));
    assertEquals(original, fetchInvitation("old-token"));
    assertFalse(repository.isActive("old-token", NOW));
    assertTrue(repository.isActive("new-token", NOW));
  }

  @Test
  void createPreservesRevokedInvitation() {
    String email = uniqueEmail();
    repository.create(email, "old-token", NOW.minus(1, DAYS), futureExpiration());
    dsl.update(WORKER_INVITATIONS)
        .set(WORKER_INVITATIONS.REVOKED_AT, NOW.atOffset(ZoneOffset.UTC))
        .where(WORKER_INVITATIONS.TOKEN_HASH.eq("old-token"))
        .execute();
    var original = fetchInvitation("old-token");

    repository.create(email, "new-token", NOW, futureExpiration());

    assertEquals(2, dsl.fetchCount(WORKER_INVITATIONS));
    assertEquals(original, fetchInvitation("old-token"));
    assertFalse(repository.isActive("old-token", NOW));
    assertTrue(repository.isActive("new-token", NOW));
  }

  @Test
  void unknownInvitationCannotBeUsed() {
    assertFalse(repository.isActive("unknown-token", NOW));
    assertThrows(
        InvalidWorkerInvitationException.class,
        () ->
            repository.consumeAndRegister(
                "unknown-token", UUID.randomUUID(), "password-hash", NOW));

    assertEquals(0, dsl.fetchCount(WORKER_INVITATIONS));
    assertEquals(0, dsl.fetchCount(USERS));
  }

  @Test
  void expiredInvitationCannotBeUsed() {
    repository.create(uniqueEmail(), "expired-token", NOW.minus(1, DAYS), NOW.minus(1, HOURS));
    var original = fetchInvitation("expired-token");

    assertFalse(repository.isActive("expired-token", NOW));
    assertThrows(
        InvalidWorkerInvitationException.class,
        () ->
            repository.consumeAndRegister(
                "expired-token", UUID.randomUUID(), "password-hash", NOW));

    assertEquals(original, fetchInvitation("expired-token"));
    assertEquals(0, dsl.fetchCount(USERS));
  }

  @Test
  void consumedInvitationCannotBeUsed() {
    repository.create(uniqueEmail(), "consumed-token", NOW.minus(1, DAYS), futureExpiration());
    dsl.update(WORKER_INVITATIONS)
        .set(WORKER_INVITATIONS.CONSUMED_AT, NOW.atOffset(ZoneOffset.UTC))
        .where(WORKER_INVITATIONS.TOKEN_HASH.eq("consumed-token"))
        .execute();
    var original = fetchInvitation("consumed-token");

    assertFalse(repository.isActive("consumed-token", NOW));
    assertThrows(
        InvalidWorkerInvitationException.class,
        () ->
            repository.consumeAndRegister(
                "consumed-token", UUID.randomUUID(), "password-hash", NOW));

    assertEquals(original, fetchInvitation("consumed-token"));
    assertEquals(0, dsl.fetchCount(USERS));
  }

  @Test
  void revokedInvitationCannotBeUsed() {
    repository.create(uniqueEmail(), "revoked-token", NOW.minus(1, DAYS), futureExpiration());
    dsl.update(WORKER_INVITATIONS)
        .set(WORKER_INVITATIONS.REVOKED_AT, NOW.atOffset(ZoneOffset.UTC))
        .where(WORKER_INVITATIONS.TOKEN_HASH.eq("revoked-token"))
        .execute();
    var original = fetchInvitation("revoked-token");

    assertFalse(repository.isActive("revoked-token", NOW));
    assertThrows(
        InvalidWorkerInvitationException.class,
        () ->
            repository.consumeAndRegister(
                "revoked-token", UUID.randomUUID(), "password-hash", NOW));

    assertEquals(original, fetchInvitation("revoked-token"));
    assertEquals(0, dsl.fetchCount(USERS));
  }

  @Test
  void invitationExpiresAtSuppliedTime() {
    Instant expiresAt = futureExpiration();
    repository.create(uniqueEmail(), "token-hash", NOW, expiresAt);
    var original = fetchInvitation("token-hash");

    assertTrue(repository.isActive("token-hash", expiresAt.minusNanos(1000)));
    assertFalse(repository.isActive("token-hash", expiresAt));
    assertFalse(repository.isActive("token-hash", expiresAt.plusSeconds(1)));
    assertThrows(
        InvalidWorkerInvitationException.class,
        () ->
            repository.consumeAndRegister(
                "token-hash", UUID.randomUUID(), "password-hash", expiresAt));

    assertEquals(original, fetchInvitation("token-hash"));
    assertEquals(0, dsl.fetchCount(USERS));
  }

  @Test
  void consumeAndRegisterPersistsWorkerAndPreventsReuse() {
    String email = uniqueEmail();
    UUID userId = UUID.randomUUID();
    repository.create(email, "token-hash", NOW, futureExpiration());

    User user = repository.consumeAndRegister("token-hash", userId, "password-hash", NOW);

    assertEquals(new User(userId, email, "password-hash", Role.WORKER), user);
    var persisted = dsl.selectFrom(USERS).where(USERS.ID.eq(userId)).fetchSingle();
    assertEquals(email, persisted.getEmail());
    assertEquals("password-hash", persisted.getPasswordHash());
    assertEquals(UserRole.WORKER, persisted.getRole());
    var consumed = fetchInvitation("token-hash");
    assertEquals(NOW, consumed.getConsumedAt().toInstant());
    assertNull(consumed.getRevokedAt());
    assertFalse(repository.isActive("token-hash", NOW));
    assertThrows(
        InvalidWorkerInvitationException.class,
        () ->
            repository.consumeAndRegister("token-hash", UUID.randomUUID(), "other-password", NOW));
    assertEquals(consumed, fetchInvitation("token-hash"));
    assertEquals(1, dsl.fetchCount(USERS));
  }

  @Test
  void emailConflictRollsBackInvitationConsumptionAndPreservesExistingUser() {
    UUID existingId = UUID.randomUUID();
    String email = uniqueEmail();
    insertUser(existingId, email);
    var existing = dsl.selectFrom(USERS).where(USERS.ID.eq(existingId)).fetchSingle();
    repository.create(email, "token-hash", NOW, futureExpiration());
    var original = fetchInvitation("token-hash");

    assertThrows(
        EmailAlreadyRegisteredException.class,
        () -> repository.consumeAndRegister("token-hash", UUID.randomUUID(), "new-password", NOW));

    assertEquals(original, fetchInvitation("token-hash"));
    assertTrue(repository.isActive("token-hash", NOW));
    assertEquals(1, dsl.fetchCount(USERS));
    assertEquals(existing, dsl.selectFrom(USERS).where(USERS.ID.eq(existingId)).fetchSingle());
  }

  @Test
  void userIdConflictRollsBackInvitationConsumption() {
    UUID existingId = UUID.randomUUID();
    insertUser(existingId, uniqueEmail());
    String email = uniqueEmail();
    repository.create(email, "token-hash", NOW, futureExpiration());
    var original = fetchInvitation("token-hash");

    assertThrows(
        DataAccessException.class,
        () -> repository.consumeAndRegister("token-hash", existingId, "password-hash", NOW));

    assertEquals(original, fetchInvitation("token-hash"));
    assertTrue(repository.isActive("token-hash", NOW));
    assertEquals(1, dsl.fetchCount(USERS));
    assertFalse(dsl.fetchExists(USERS, USERS.EMAIL.eq(email)));
    assertEquals(
        Role.WORKER,
        repository
            .consumeAndRegister("token-hash", UUID.randomUUID(), "password-hash", NOW)
            .role());
  }

  private void insertUser(UUID id, String email) {
    dsl.insertInto(USERS)
        .set(USERS.ID, id)
        .set(USERS.EMAIL, email)
        .set(USERS.PASSWORD_HASH, "existing-password")
        .set(USERS.ROLE, UserRole.CLIENT)
        .execute();
  }

  private WorkerInvitationsRecord fetchInvitation(String tokenHash) {
    return dsl.selectFrom(WORKER_INVITATIONS)
        .where(WORKER_INVITATIONS.TOKEN_HASH.eq(tokenHash))
        .fetchOne();
  }

  private static Instant futureExpiration() {
    return NOW.plus(1, HOURS);
  }

  private static String uniqueEmail() {
    return UUID.randomUUID() + "@example.com";
  }
}
