/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HotelSortByTest {
  @ParameterizedTest
  @MethodSource("sortByCases")
  void enumValuesUseExpectedSortByConstants(HotelSortBy value, String expected) {
    assertEquals(expected, value.toString());
  }

  private static Stream<Arguments> sortByCases() {
    return Stream.of(
        Arguments.of(HotelSortBy.PricePerNight, HotelSortBy.Values.PricePerNight),
        Arguments.of(HotelSortBy.Rating, HotelSortBy.Values.Rating));
  }
}
