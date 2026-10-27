/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.room;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RoomValidationTests {
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

  @ParameterizedTest
  @MethodSource("validRoomAmenities")
  void createRoomAcceptsValidValuesAndDefaultsNullAmenities(Set<String> amenities) {
    assertValid(createRoom("204", RoomType.DOUBLE, validMoney(), RoomStatus.ACTIVE, amenities));
  }

  static Stream<Set<String>> validRoomAmenities() {
    return Stream.of(Set.of(CreateRoom.amenitiesExample), null);
  }

  @ParameterizedTest
  @MethodSource("invalidCreateRoomNumbers")
  void createRoomRoomNumberRejectsNullAndBlank(String roomNumber) {
    assertInvalid(
        "roomNumber",
        createRoom(roomNumber, RoomType.DOUBLE, validMoney(), RoomStatus.ACTIVE, Set.of()));
  }

  static Stream<String> invalidCreateRoomNumbers() {
    return Stream.of(null, "   ");
  }

  @ParameterizedTest
  @MethodSource("roomNumbersOutsideSize")
  void createRoomRoomNumberRejectsValuesOutsideSize(String roomNumber) {
    assertInvalid(
        "roomNumber",
        createRoom(roomNumber, RoomType.DOUBLE, validMoney(), RoomStatus.ACTIVE, Set.of()));
  }

  static Stream<String> roomNumbersOutsideSize() {
    return Stream.of(
        "x".repeat(CreateRoom.roomNumberMinSize - 1), "x".repeat(CreateRoom.roomNumberMaxSize + 1));
  }

  @Test
  void createRoomRoomTypeRejectsNull() {
    assertInvalid("roomType", createRoom("204", null, validMoney(), RoomStatus.ACTIVE, Set.of()));
  }

  @ParameterizedTest
  @MethodSource("invalidCreateRoomPrices")
  void createRoomPricePerNightRejectsNullAndValidatesNestedMoney(String property, Money price) {
    assertInvalid(property, createRoom("204", RoomType.DOUBLE, price, RoomStatus.ACTIVE, Set.of()));
  }

  static Stream<Arguments> invalidCreateRoomPrices() {
    return Stream.of(
        Arguments.of("pricePerNight", null),
        Arguments.of("pricePerNight.amount", new Money(BigDecimal.ZERO, "USD")));
  }

  @Test
  void createRoomRoomStatusRejectsNull() {
    assertInvalid("roomStatus", createRoom("204", RoomType.DOUBLE, validMoney(), null, Set.of()));
  }

  @ParameterizedTest
  @MethodSource("invalidAmenities")
  void createRoomAmenitiesEnforcesCollectionSizeAndElementSize(Set<String> amenities) {
    assertInvalid(
        "amenities",
        createRoom("204", RoomType.DOUBLE, validMoney(), RoomStatus.ACTIVE, amenities));
  }

  static Stream<Set<String>> invalidAmenities() {
    return Stream.of(
        oversizedAmenities(),
        Set.of(" "),
        Set.of("W".repeat(CreateRoom.amenityMinSize - 1)),
        Set.of("x".repeat(CreateRoom.amenityMaxSize + 1)));
  }

  @Test
  void updateRoomAcceptsAllOptionalPropertiesAsNull() {
    assertValid(new UpdateRoom(null, null, null, null, null));
  }

  @ParameterizedTest
  @MethodSource("invalidUpdateRoomNumbers")
  void updateRoomRoomNumberRejectsWhitespaceAndValuesOutsideSize(String roomNumber) {
    assertInvalid("roomNumber", new UpdateRoom(roomNumber, null, null, null, null));
  }

  static Stream<String> invalidUpdateRoomNumbers() {
    return Stream.of(
        "   ",
        "1".repeat(CreateRoom.roomNumberMinSize - 1),
        "x".repeat(CreateRoom.roomNumberMaxSize + 1));
  }

  @Test
  void updateRoomPricePerNightValidatesNestedMoney() {
    assertInvalid(
        "pricePerNight.amount",
        new UpdateRoom(null, null, new Money(BigDecimal.ZERO, "USD"), null, null));
  }

  @ParameterizedTest
  @MethodSource("invalidAmenities")
  void updateRoomAmenitiesEnforcesCollectionAndElementConstraints(Set<String> amenities) {
    assertInvalid("amenities", new UpdateRoom(null, null, null, null, amenities));
  }

  @ParameterizedTest
  @MethodSource("invalidMoneyAmounts")
  void moneyAmountRejectsNullZeroNegativeAndValuesAboveMaximum(BigDecimal amount) {
    assertInvalid("amount", new Money(amount, "USD"));
  }

  static Stream<BigDecimal> invalidMoneyAmounts() {
    return Stream.of(
        null, BigDecimal.ZERO, new BigDecimal("-0.01"), new BigDecimal("1000000000.01"));
  }

  @ParameterizedTest
  @MethodSource("invalidMoneyCurrencies")
  void moneyCurrencyRejectsBlankWrongLengthAndUnknownCurrency(String currency) {
    assertInvalid("currency", new Money(BigDecimal.ONE, currency));
  }

  static Stream<String> invalidMoneyCurrencies() {
    return Stream.of(null, " ", "US", "USDD", "ZZZ");
  }

  @Test
  void moneyAcceptsValidAmountAndCurrency() {
    assertValid(validMoney());
  }

  @Test
  void getRoomsParamsAcceptsNullOptionalPropertiesAndDefaultsNullAmenities() {
    assertValid(new GetRoomsParams(null, null, null, null, null, null, null, null, null));
  }

  @Test
  void getRoomsParamsCheckInRejectsPastDate() {
    assertInvalid("checkIn", getRoomsParams(LocalDate.now().minusDays(1), null, null, null, null));
  }

  @Test
  void getRoomsParamsCheckOutRejectsPastDate() {
    assertInvalid("checkOut", getRoomsParams(null, LocalDate.now().minusDays(1), null, null, null));
  }

  @Test
  void getRoomsParamsMinPriceRejectsNegativeValue() {
    assertInvalid("minPrice", getRoomsParams(null, null, new BigDecimal("-0.01"), null, null));
  }

  @Test
  void getRoomsParamsMaxPriceRejectsNegativeValue() {
    assertInvalid("maxPrice", getRoomsParams(null, null, null, new BigDecimal("-0.01"), null));
  }

  @ParameterizedTest
  @MethodSource("invalidAmenities")
  void getRoomsParamsAmenitiesEnforcesCollectionAndElementSize(Set<String> amenities) {
    assertInvalid("amenities", getRoomsParams(null, null, null, null, amenities));
  }

  private static CreateRoom createRoom(
      String roomNumber, RoomType roomType, Money price, RoomStatus status, Set<String> amenities) {
    return new CreateRoom(roomNumber, roomType, price, status, amenities);
  }

  private static Money validMoney() {
    return new Money(BigDecimal.TEN, "USD");
  }

  private static Set<String> oversizedAmenities() {
    return IntStream.range(0, CreateRoom.maxAmenities + 1)
        .mapToObj(index -> "amenity" + index)
        .collect(Collectors.toSet());
  }

  private static GetRoomsParams getRoomsParams(
      LocalDate checkIn,
      LocalDate checkOut,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      Set<String> amenities) {
    return new GetRoomsParams(
        null, null, checkIn, checkOut, minPrice, maxPrice, amenities, null, null);
  }

  private void assertValid(Object value) {
    assertTrue(validator.validate(value).isEmpty(), () -> "Expected no violations for " + value);
  }

  private void assertInvalid(String property, Object value) {
    assertTrue(
        validator.validate(value).stream()
            .anyMatch(
                v ->
                    v.getPropertyPath().toString().equals(property)
                        || v.getPropertyPath().toString().startsWith(property + ".")
                        || v.getPropertyPath().toString().startsWith(property + "[")),
        () -> "Expected a validation violation for " + property);
  }
}
