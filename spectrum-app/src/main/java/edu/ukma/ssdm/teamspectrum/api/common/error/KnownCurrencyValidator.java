/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.common.error;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.UnknownCurrencyException;

public class KnownCurrencyValidator implements ConstraintValidator<KnownCurrency, String> {
  @Override
  public boolean isValid(String currencyCode, ConstraintValidatorContext context) {
    if (currencyCode == null) {
      return true;
    }

    try {
      CurrencyUnit currency = Monetary.getCurrency(currencyCode);
      return currency != null;
    } catch (UnknownCurrencyException exception) {
      return false;
    }
  }
}
