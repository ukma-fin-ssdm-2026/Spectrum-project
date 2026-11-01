/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.Users.USERS;
import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.WorkerInvitations.WORKER_INVITATIONS;
import static org.jooq.impl.DSL.currentOffsetDateTime;
import static org.jooq.impl.DSL.excluded;
import static org.jooq.impl.DSL.selectOne;

import edu.ukma.ssdm.teamspectrum.db.jooq.enums.UserRole;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;

@ApplicationScoped
public class WorkerInvitationRepositoryImpl implements WorkerInvitationRepository {
  private final DSLContext dsl;

  @Inject
  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "DSLContext is an intentionally shared injected database dependency.")
  public WorkerInvitationRepositoryImpl(DSLContext dsl) {
    this.dsl = dsl;
  }

  @Override
  public void create(String email, String tokenHash, Instant expiresAt) {
    OffsetDateTime expiration = OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC);
    dsl.insertInto(WORKER_INVITATIONS)
        .set(WORKER_INVITATIONS.ID, UUID.randomUUID())
        .set(WORKER_INVITATIONS.EMAIL, email)
        .set(WORKER_INVITATIONS.TOKEN_HASH, tokenHash)
        .set(WORKER_INVITATIONS.CREATED_AT, currentOffsetDateTime())
        .set(WORKER_INVITATIONS.EXPIRES_AT, expiration)
        .onConflict(WORKER_INVITATIONS.EMAIL)
        .where(WORKER_INVITATIONS.CONSUMED_AT.isNull().and(WORKER_INVITATIONS.REVOKED_AT.isNull()))
        .doUpdate()
        .set(WORKER_INVITATIONS.TOKEN_HASH, excluded(WORKER_INVITATIONS.TOKEN_HASH))
        .set(WORKER_INVITATIONS.CREATED_AT, currentOffsetDateTime())
        .set(WORKER_INVITATIONS.EXPIRES_AT, expiration)
        .execute();
  }

  @Override
  public boolean isActive(String tokenHash) {
    return dsl.fetchExists(
        selectOne()
            .from(WORKER_INVITATIONS)
            .where(WORKER_INVITATIONS.TOKEN_HASH.eq(tokenHash))
            .and(WORKER_INVITATIONS.CONSUMED_AT.isNull())
            .and(WORKER_INVITATIONS.REVOKED_AT.isNull())
            .and(WORKER_INVITATIONS.EXPIRES_AT.gt(currentOffsetDateTime())));
  }

  @Override
  public User consumeAndRegister(String tokenHash, UUID userId, String passwordHash) {
    return dsl.transactionResult(
        configuration -> {
          DSLContext tx = DSL.using(configuration);
          Record1<String> invitation =
              tx.update(WORKER_INVITATIONS)
                  .set(WORKER_INVITATIONS.CONSUMED_AT, currentOffsetDateTime())
                  .where(WORKER_INVITATIONS.TOKEN_HASH.eq(tokenHash))
                  .and(WORKER_INVITATIONS.CONSUMED_AT.isNull())
                  .and(WORKER_INVITATIONS.REVOKED_AT.isNull())
                  .and(WORKER_INVITATIONS.EXPIRES_AT.gt(currentOffsetDateTime()))
                  .returningResult(WORKER_INVITATIONS.EMAIL)
                  .fetchOne();
          if (invitation == null) {
            throw new InvalidWorkerInvitationException();
          }
          String email = invitation.value1();

          int inserted =
              tx.insertInto(USERS)
                  .set(USERS.ID, userId)
                  .set(USERS.EMAIL, email)
                  .set(USERS.PASSWORD_HASH, passwordHash)
                  .set(USERS.ROLE, UserRole.WORKER)
                  .onConflict(USERS.EMAIL)
                  .doNothing()
                  .execute();
          if (inserted != 1) {
            throw new EmailAlreadyRegisteredException();
          }

          return new User(userId, email, passwordHash, Role.WORKER);
        });
  }
}
