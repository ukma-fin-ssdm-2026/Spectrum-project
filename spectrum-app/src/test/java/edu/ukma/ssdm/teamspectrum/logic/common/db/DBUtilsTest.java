/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.common.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.ukma.ssdm.teamspectrum.logic.common.PositiveDuration;
import java.time.Duration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

class DBUtilsTest {
  @Test
  void executesLocalTimeoutInMillisecondsWithoutBindParameters() {
    DSLContext context =
        DSL.using(
            new MockConnection(
                execution -> {
                  assertEquals(
                      """
                      set local "transaction_timeout" = 10000\
                      """,
                      execution.sql());
                  assertEquals(0, execution.bindings().length);
                  return new MockResult[] {new MockResult(0)};
                }),
            SQLDialect.POSTGRES);

    DBUtils.setTransactionTimeout(context, new PositiveDuration(Duration.ofMillis(10_000)));
  }

  @Test
  void rejectsDurationsThatWouldDisableTimeoutOrOverflow() {
    DSLContext context = DSL.using(SQLDialect.POSTGRES);
    assertThrows(
        ArithmeticException.class,
        () ->
            DBUtils.setTransactionTimeout(
                context, new PositiveDuration(Duration.ofMillis((long) Integer.MAX_VALUE + 1))));
  }
}
