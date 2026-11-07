/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.common.db;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Predicate;

/** Recognizes database timeouts even when wrapped by jOOQ or transaction handling. */
public final class DatabaseTimeoutPredicate implements Predicate<Throwable> {
  private static final String STATEMENT_CANCEL = "57014";
  private static final String TRANSACTION_TIMEOUT = "25P04";

  @Override
  public boolean test(Throwable failure) {
    Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
    for (Throwable cause = failure; cause != null && visited.add(cause); cause = cause.getCause()) {
      if (cause instanceof SQLTimeoutException) {
        return true;
      }
      if (cause instanceof SQLException sqlException) {
        // PostgreSQL reports statement cancellation (including query timeouts) as 57014
        // and transaction_timeout as 25P04.
        if (STATEMENT_CANCEL.equals(sqlException.getSQLState())
            || TRANSACTION_TIMEOUT.equals(sqlException.getSQLState())) {
          return true;
        }
      }
    }
    return false;
  }
}
