/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SortOrderTest {
  @ParameterizedTest
  @MethodSource("sortOrderCases")
  void enumValuesUseExpectedSortOrderConstants(SortOrder value, String expected) {
    assertEquals(expected, value.toString());
  }

  private static Stream<Arguments> sortOrderCases() {
    return Stream.of(
        Arguments.of(SortOrder.Asc, SortOrder.Values.Asc),
        Arguments.of(SortOrder.Desc, SortOrder.Values.Desc));
  }
}
