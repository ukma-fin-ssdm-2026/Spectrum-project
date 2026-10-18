/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.auth;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LoginRequestValidationTest {
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
    assertTrue(validator.validate(new LoginRequest("user@example.com", "password")).isEmpty());
  }

  @Test
  void emailRejectsBlankValue() {
    assertInvalid("email", new LoginRequest(" ", "password"));
  }

  @Test
  void emailRejectsMalformedValue() {
    assertInvalid("email", new LoginRequest("not-an-email", "password"));
  }

  @Test
  void passwordRejectsBlankValue() {
    assertInvalid("password", new LoginRequest("user@example.com", " "));
  }

  private void assertInvalid(String property, LoginRequest request) {
    assertTrue(
        validator.validate(request).stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals(property)),
        () -> "Expected a validation violation for " + property);
  }
}
