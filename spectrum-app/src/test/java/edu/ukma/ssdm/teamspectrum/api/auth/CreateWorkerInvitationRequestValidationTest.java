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
class CreateWorkerInvitationRequestValidationTest {
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
  void acceptsValidEmail() {
    assertTrue(
        validator.validate(new CreateWorkerInvitationRequest("worker@example.com")).isEmpty());
  }

  @Test
  void emailAcceptsMaximumLength() {
    assertTrue(
        validator.validate(new CreateWorkerInvitationRequest(emailWithLength(254))).isEmpty());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "\t\n"})
  void emailRejectsNullOrBlankValue(String email) {
    assertInvalid(new CreateWorkerInvitationRequest(email));
  }

  @ParameterizedTest
  @ValueSource(strings = {"not-an-email", "worker@", "@example.com", "worker example@example.com"})
  void emailRejectsMalformedValue(String email) {
    assertInvalid(new CreateWorkerInvitationRequest(email));
  }

  @Test
  void emailRejectsValueLongerThanMaximum() {
    assertInvalid(new CreateWorkerInvitationRequest(emailWithLength(255)));
  }

  private static String emailWithLength(int length) {
    String localPart = "a".repeat(64);
    String domainPrefix = "b".repeat(63) + "." + "c".repeat(63) + ".";
    String emailPrefix = localPart + "@" + domainPrefix;
    int remainingLength = length - emailPrefix.length();
    return emailPrefix + "d".repeat(remainingLength);
  }

  private void assertInvalid(CreateWorkerInvitationRequest request) {
    assertTrue(
        validator.validate(request).stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("email")),
        "Expected a validation violation for email");
  }
}
