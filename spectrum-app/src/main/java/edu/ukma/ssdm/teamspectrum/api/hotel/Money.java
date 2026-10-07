/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record Money(
    @Positive @Max(maxAmount) BigDecimal amount,
    @NotBlank(message = "Currency can't be empty") @Size(min = currencySize, max = currencySize)
        String currency) {
  static final long maxAmount = 1000_000_000;
  static final int currencySize = 3;
}
