/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.common.db;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.Test;

class DatabaseTimeoutPredicateTest {
  private final DatabaseTimeoutPredicate predicate = new DatabaseTimeoutPredicate();

  @Test
  void recognizesJdbcTimeoutsThroughTransactionAndJooqWrappers() {
    assertTrue(predicate.test(new SQLTimeoutException("timeout")));
    assertTrue(
        predicate.test(
            new RuntimeException(new DataAccessException("query", new SQLTimeoutException()))));
  }

  @Test
  void recognizesPostgresStatementAndTransactionTimeouts() {
    for (String state : new String[] {"57014", "25P04"}) {
      assertTrue(predicate.test(new DataAccessException("query", new SQLException("", state))));
    }
  }

  @Test
  void rejectsNonTimeoutFailuresAndUnknownCommitOutcomes() {
    for (String state : new String[] {"23505", "08006", "08007", "40001"}) {
      assertFalse(predicate.test(new DataAccessException("query", new SQLException("", state))));
    }
    assertFalse(predicate.test(new SQLException("timeout")));
    assertFalse(predicate.test(new IllegalArgumentException("invalid input")));
    assertFalse(predicate.test(null));
  }

  @Test
  void handlesCyclicCauses() {
    var first = new RuntimeException();
    var second = new RuntimeException(first);
    first.initCause(second);
    assertFalse(predicate.test(first));
  }
}
