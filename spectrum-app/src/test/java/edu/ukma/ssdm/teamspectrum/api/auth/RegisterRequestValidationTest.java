/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.auth;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RegisterRequestValidationTest {
  private static ValidatorFactory validatorFactory;
  private static Validator validator;

  @BeforeAll
  static void setUpValidator() {
    validatorFactory = Validation.buildDefaultValidatorFactory();
    validator = validatorFactory.getValidator();
  }

  @AfterAll
  static void closeValidatorFactory() {
    validatorFactory.close();
  }

  @Test
  void acceptsValidEmailAndPassword() {
    assertTrue(
        validator.validate(new RegisterRequest("user@example.com", "validPassword12")).isEmpty());
  }

  @Test
  void emailRejectsBlankValue() {
    assertInvalid("email", new RegisterRequest(" ", "validPassword12"));
  }

  @Test
  void emailRejectsMalformedValue() {
    assertInvalid("email", new RegisterRequest("not-an-email", "validPassword12"));
  }

  @Test
  void emailRejectsValueLongerThanMaximum() {
    assertInvalid(
        "email", new RegisterRequest("a".repeat(245) + "@example.com", "validPassword12"));
  }

  @Test
  void passwordRejectsBlankValue() {
    assertInvalid("password", new RegisterRequest("user@example.com", " "));
  }

  @Test
  void passwordRejectsValueShorterThanMinimum() {
    assertInvalid("password", new RegisterRequest("user@example.com", "short"));
  }

  @Test
  void passwordRejectsValueLongerThanMaximum() {
    assertInvalid("password", new RegisterRequest("user@example.com", "p".repeat(73)));
  }

  private void assertInvalid(String property, RegisterRequest request) {
    assertTrue(
        validator.validate(request).stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals(property)),
        () -> "Expected a validation violation for " + property);
  }
}
