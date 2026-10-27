/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RoleTest {
  @ParameterizedTest
  @MethodSource("roleCases")
  void enumValuesUseExpectedRoleConstants(Role value, String expected) {
    assertEquals(expected, value.toString());
  }

  private static Stream<Arguments> roleCases() {
    return Stream.of(
        Arguments.of(Role.CLIENT, Role.Values.CLIENT),
        Arguments.of(Role.WORKER, Role.Values.WORKER),
        Arguments.of(Role.ADMIN, Role.Values.ADMIN));
  }
}
