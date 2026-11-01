/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import static edu.ukma.ssdm.teamspectrum.db.jooq.tables.Users.USERS;

import edu.ukma.ssdm.teamspectrum.db.jooq.enums.UserRole;
import edu.ukma.ssdm.teamspectrum.db.jooq.tables.records.UsersRecord;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import org.jooq.DSLContext;

@ApplicationScoped
public class UserRepositoryImpl implements UserRepository {
  private final DSLContext dsl;

  @Inject
  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "DSLContext is an intentionally shared injected database dependency.")
  public UserRepositoryImpl(DSLContext dsl) {
    this.dsl = dsl;
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return dsl.selectFrom(USERS)
        .where(USERS.EMAIL.eq(email))
        .fetchOptional(UserRepositoryImpl::toUser);
  }

  @Override
  public boolean existsByRole(Role role) {
    return dsl.selectOne()
        .from(USERS)
        .where(USERS.ROLE.eq(UserRole.valueOf(role.name())))
        .fetchOptional()
        .isPresent();
  }

  @Override
  public boolean add(User user) {
    return dsl.insertInto(USERS)
            .set(USERS.ID, user.id())
            .set(USERS.EMAIL, user.email())
            .set(USERS.PASSWORD_HASH, user.passwordHash())
            .set(USERS.ROLE, UserRole.valueOf(user.role().name()))
            .onConflict(USERS.EMAIL)
            .doNothing()
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
