/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.common.db;

import edu.ukma.ssdm.teamspectrum.logic.common.PositiveDuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

public final class DBUtils {
  private DBUtils() {}

  /**
   * Sets the timeout for the current PostgreSQL transaction. Must be called inside a transaction.
   * Sub-millisecond precision is truncated.
   *
   * @throws IllegalArgumentException if the timeout is below one millisecond
   */
  public static void setTransactionTimeout(DSLContext context, PositiveDuration timeout) {
    int millis = Math.toIntExact(timeout.value().toMillis());
    context.setLocal("transaction_timeout", DSL.inline(millis)).execute();
  }
}
