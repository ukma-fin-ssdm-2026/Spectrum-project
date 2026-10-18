/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GetHotelsParamsValidationTest {
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
  void locationAcceptsValidValue() {
    assertValid(mkGeHotelParams("Kyiv", null, null, null, null, null, null, null));
  }

  @Test
  void locationRejectsValueShorterThanMinimum() {
    assertInvalid("location", mkGeHotelParams("K", null, null, null, null, null, null, null));
  }

  @Test
  void locationRejectsValueLongerThanMaximum() {
    assertInvalid(
        "location", mkGeHotelParams("x".repeat(101), null, null, null, null, null, null, null));
  }

  @Test
  void checkInAcceptsPresentOrFutureDate() {
    assertValid(
        mkGeHotelParams(null, LocalDate.now().plusDays(1), null, null, null, null, null, null));
  }

  @Test
  void checkInRejectsPastDate() {
    assertInvalid(
        "checkIn",
        mkGeHotelParams(null, LocalDate.now().minusDays(1), null, null, null, null, null, null));
  }

  @Test
  void checkOutAcceptsPresentOrFutureDate() {
    assertValid(
        mkGeHotelParams(null, null, LocalDate.now().plusDays(1), null, null, null, null, null));
  }

  @Test
  void checkOutRejectsPastDate() {
    assertInvalid(
        "checkOut",
        mkGeHotelParams(null, null, LocalDate.now().minusDays(1), null, null, null, null, null));
  }

  @Test
  void minPriceAcceptsNonNegativeValue() {
    assertValid(mkGeHotelParams(null, null, null, BigDecimal.ZERO, null, null, null, null));
  }

  @Test
  void minPriceRejectsNegativeValue() {
    assertInvalid(
        "minPrice",
        mkGeHotelParams(null, null, null, new BigDecimal("-0.01"), null, null, null, null));
  }

  @Test
  void maxPriceAcceptsNonNegativeValue() {
    assertValid(mkGeHotelParams(null, null, null, null, BigDecimal.ZERO, null, null, null));
  }

  @Test
  void maxPriceRejectsNegativeValue() {
    assertInvalid(
        "maxPrice",
        mkGeHotelParams(null, null, null, null, new BigDecimal("-0.01"), null, null, null));
  }

  @Test
  void minRatingAcceptsValueWithinRange() {
    assertValid(mkGeHotelParams(null, null, null, null, null, 3.0, null, null));
  }

  @Test
  void minRatingRejectsValueBelowMinimum() {
    assertInvalid("minRating", mkGeHotelParams(null, null, null, null, null, -0.1, null, null));
  }

  @Test
  void minRatingRejectsValueAboveMaximum() {
    assertInvalid("minRating", mkGeHotelParams(null, null, null, null, null, 5.1, null, null));
  }

  @Test
  void maxRatingAcceptsValueWithinRange() {
    assertValid(mkGeHotelParams(null, null, null, null, null, null, 3.0, null));
  }

  @Test
  void maxRatingRejectsValueBelowMinimum() {
    assertInvalid("maxRating", mkGeHotelParams(null, null, null, null, null, null, -0.1, null));
  }

  @Test
  void maxRatingRejectsValueAboveMaximum() {
    assertInvalid("maxRating", mkGeHotelParams(null, null, null, null, null, null, 5.1, null));
  }

  @Test
  void amenitiesAcceptValidCollectionAndValues() {
    assertValid(mkGeHotelParams(null, null, null, null, null, null, null, List.of("Wi-Fi")));
  }

  @Test
  void amenitiesRejectCollectionAboveMaximumSize() {
    assertInvalid(
        "amenities",
        mkGeHotelParams(
            null, null, null, null, null, null, null, java.util.Collections.nCopies(21, "Wi-Fi")));
  }

  @Test
  void amenityRejectsValueShorterThanMinimum() {
    assertInvalid(
        "amenities", mkGeHotelParams(null, null, null, null, null, null, null, List.of("W")));
  }

  @Test
  void amenityRejectsValueLongerThanMaximum() {
    assertInvalid(
        "amenities",
        mkGeHotelParams(null, null, null, null, null, null, null, List.of("x".repeat(41))));
  }

  private void assertValid(GetHotelsParams params) {
    assertTrue(validator.validate(params).isEmpty());
  }

  private void assertInvalid(String property, GetHotelsParams params) {
    assertTrue(
        validator.validate(params).stream()
            .anyMatch(v -> v.getPropertyPath().toString().startsWith(property)),
        () -> "Expected a validation violation for " + property);
  }

  private static GetHotelsParams mkGeHotelParams(
      String location,
      LocalDate checkIn,
      LocalDate checkOut,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      Double minRating,
      Double maxRating,
      List<String> amenities) {
    return new GetHotelsParams(
        location, checkIn, checkOut, minPrice, maxPrice, minRating, maxRating, amenities);
  }
}
