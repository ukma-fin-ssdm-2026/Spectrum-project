/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.auth;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("unit")
class CompleteWorkerRegistrationRequestValidationTest {
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
  void acceptsValidTokenAndPassword() {
    assertTrue(
        validator
            .validate(new CompleteWorkerRegistrationRequest("invitation-token", "validPassword12"))
            .isEmpty());
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 128})
  void tokenAcceptsLengthBoundaries(int length) {
    assertTrue(
        validator
            .validate(new CompleteWorkerRegistrationRequest("t".repeat(length), "validPassword12"))
            .isEmpty());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "\t\n"})
  void tokenRejectsNullOrBlankValue(String token) {
    assertInvalid("token", new CompleteWorkerRegistrationRequest(token, "validPassword12"));
  }

  @Test
  void tokenRejectsValueLongerThanMaximum() {
    assertInvalid(
        "token", new CompleteWorkerRegistrationRequest("t".repeat(129), "validPassword12"));
  }

  @ParameterizedTest
  @ValueSource(ints = {12, 72})
  void passwordAcceptsLengthBoundaries(int length) {
    assertTrue(
        validator
            .validate(new CompleteWorkerRegistrationRequest("invitation-token", "p".repeat(length)))
            .isEmpty());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "\t\n", "            "})
  void passwordRejectsNullOrBlankValue(String password) {
    assertInvalid("password", new CompleteWorkerRegistrationRequest("invitation-token", password));
  }

  @ParameterizedTest
  @ValueSource(ints = {11, 73})
  void passwordRejectsLengthOutsideBounds(int length) {
    assertInvalid(
        "password", new CompleteWorkerRegistrationRequest("invitation-token", "p".repeat(length)));
  }

  private void assertInvalid(String property, CompleteWorkerRegistrationRequest request) {
    assertTrue(
        validator.validate(request).stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals(property)),
        () -> "Expected a validation violation for " + property);
  }
}
