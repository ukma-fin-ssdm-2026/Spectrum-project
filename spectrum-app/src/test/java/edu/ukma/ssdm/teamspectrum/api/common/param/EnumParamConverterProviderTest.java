/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.common.param;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.ukma.ssdm.teamspectrum.api.common.SortOrder;
import edu.ukma.ssdm.teamspectrum.api.hotel.HotelSortBy;
import edu.ukma.ssdm.teamspectrum.api.room.RoomSortBy;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ext.ParamConverter;
import java.lang.annotation.Annotation;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Characterization tests: they record the current behavior of the converter before it is
 * refactored, including the odd parts (the enum constant name is rejected, matching is
 * case-sensitive, other enums are not handled).
 */
@Tag("unit")
class EnumParamConverterProviderTest {
  private final EnumParamConverterProvider provider = new EnumParamConverterProvider();

  @Test
  void convertsSortOrderFromItsWireValue() {
    var converter = converter(SortOrder.class);

    assertEquals(SortOrder.Asc, converter.fromString("ASC"));
    assertEquals(SortOrder.Desc, converter.fromString("DESC"));
  }

  @Test
  void convertsRoomSortByFromItsWireValue() {
    var converter = converter(RoomSortBy.class);

    assertEquals(RoomSortBy.RoomNumber, converter.fromString("ROOM_NUMBER"));
    assertEquals(RoomSortBy.PricePerNight, converter.fromString("PRICE_PER_NIGHT"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"Asc", "asc", " ASC", "", "UP"})
  void rejectsSortOrderNamesAndValuesThatDoNotMatchExactly(String value) {
    var converter = converter(SortOrder.class);

    var exception = assertThrows(BadRequestException.class, () -> converter.fromString(value));

    assertEquals(400, exception.getResponse().getStatus());
    assertEquals(
        "Invalid value '%s' for SortOrder. Allowed values: ASC, DESC".formatted(value),
        exception.getMessage());
  }

  @Test
  void listsAllowedRoomSortByValuesInDeclarationOrder() {
    var converter = converter(RoomSortBy.class);

    var exception =
        assertThrows(BadRequestException.class, () -> converter.fromString("RoomNumber"));

    assertEquals(
        "Invalid value 'RoomNumber' for RoomSortBy. Allowed values: ROOM_NUMBER, PRICE_PER_NIGHT",
        exception.getMessage());
  }

  @Test
  void returnsNullForMissingValue() {
    assertNull(converter(SortOrder.class).fromString(null));
    assertNull(converter(RoomSortBy.class).fromString(null));
  }

  @Test
  void writesWireValueAndNull() {
    var converter = converter(SortOrder.class);

    assertEquals("DESC", converter.toString(SortOrder.Desc));
    assertNull(converter.toString(null));
  }

  @Test
  void returnsNoConverterForOtherTypes() {
    assertNull(provider.getConverter(String.class, String.class, new Annotation[0]));
    assertNull(provider.getConverter(HotelSortBy.class, HotelSortBy.class, new Annotation[0]));
  }

  private <T> ParamConverter<T> converter(Class<T> type) {
    var converter = provider.getConverter(type, type, new Annotation[0]);
    assertNotNull(converter);
    return converter;
  }
}
