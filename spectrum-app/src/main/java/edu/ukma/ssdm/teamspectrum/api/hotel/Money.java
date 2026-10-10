/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record Money(
    @NotNull @Positive @DecimalMax(maxAmountExample) @Schema(examples = {amountExample})
        BigDecimal amount,
    @NotBlank(message = "Currency can't be empty")
        @Size(min = currencySize, max = currencySize)
        @Pattern(regexp = "[A-Z]{3}")
        @Schema(examples = {currencyExample})
        String currency) {
  static final String maxAmountExample = "1000000000";
  static final String amountExample = "145.00";
  static final int currencySize = 3;
  static final String currencyExample = "USD";
}
