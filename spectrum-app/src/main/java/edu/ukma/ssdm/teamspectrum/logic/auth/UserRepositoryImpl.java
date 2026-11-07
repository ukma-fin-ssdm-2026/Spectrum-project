/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.Users.USERS;

import edu.ukma.ssdm.teamspectrum.db.jooq.enums.UserRole;
import edu.ukma.ssdm.teamspectrum.db.jooq.tables.records.UsersRecord;
import edu.ukma.ssdm.teamspectrum.logic.common.db.DatabaseTimeoutPredicate;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.smallrye.faulttolerance.api.ExponentialBackoff;
import io.smallrye.faulttolerance.api.RetryWhen;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.jooq.DSLContext;

@ApplicationScoped
public class UserRepositoryImpl implements UserRepository {
  private static final int QUERY_TIMEOUT_SECONDS = 5;

  private final DSLContext dsl;

  @Inject
  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "DSLContext is an intentionally shared injected database dependency.")
  public UserRepositoryImpl(DSLContext dsl) {
    this.dsl = dsl;
  }

  @Override
  @Retry(maxRetries = 3, delay = 200, jitter = 100)
  @ExponentialBackoff(factor = 2, maxDelay = 1000)
  @RetryWhen(exception = DatabaseTimeoutPredicate.class)
  public Optional<User> findByEmail(String email) {
    return dsl.selectFrom(USERS)
        .where(USERS.EMAIL.eq(email))
        .queryTimeout(QUERY_TIMEOUT_SECONDS)
        .fetchOptional(UserRepositoryImpl::toUser);
  }

  @Override
  @Retry(maxRetries = 3, delay = 200, jitter = 100)
  @ExponentialBackoff(factor = 2, maxDelay = 1000)
  @RetryWhen(exception = DatabaseTimeoutPredicate.class)
  public boolean add(User user) {
    return dsl.insertInto(USERS)
            .set(USERS.ID, user.id())
            .set(USERS.EMAIL, user.email())
            .set(USERS.PASSWORD_HASH, user.passwordHash())
            .set(USERS.ROLE, UserRole.valueOf(user.role().name()))
            .onConflict(USERS.EMAIL)
            .doNothing()
            .queryTimeout(QUERY_TIMEOUT_SECONDS)
            .execute()
        == 1;
  }

  private static User toUser(UsersRecord record) {
    return new User(
        record.getId(),
        record.getEmail(),
        record.getPasswordHash(),
        Role.valueOf(record.getRole().getLiteral()));
  }
}
