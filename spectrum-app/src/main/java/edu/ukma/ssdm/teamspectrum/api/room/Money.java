/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.room;

import edu.ukma.ssdm.teamspectrum.api.common.error.KnownCurrency;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "Money", description = "A monetary amount and its ISO 4217 currency")
public record Money(
    @NotNull @Positive @DecimalMax(maxAmountExample) @Schema(examples = {amountExample})
        BigDecimal amount,
    @NotBlank(message = "Currency can't be empty")
        @Size(min = currencySize, max = currencySize)
        @KnownCurrency
        @Schema(
            description = "A supported ISO 4217 currency code",
            examples = {currencyExample})
        String currency) {
  static final String maxAmountExample = "1000000000";
  static final String amountExample = "145.00";
  static final int currencySize = 3;
  static final String currencyExample = "USD";
}
