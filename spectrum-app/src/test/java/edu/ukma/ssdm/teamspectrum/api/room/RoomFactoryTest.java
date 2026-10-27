/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.room;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.ukma.ssdm.teamspectrum.api.common.error.ValidationError;
import edu.ukma.ssdm.teamspectrum.logic.room.CreateRoomCommand;
import edu.ukma.ssdm.teamspectrum.logic.room.RoomRecord;
import edu.ukma.ssdm.teamspectrum.logic.room.UpdateRoomCommand;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RoomFactoryTest {
  private static final String CREATE_ROOM_NUMBER = "204";
  private static final BigDecimal CREATE_PRICE_AMOUNT = new BigDecimal("145.15");
  private static final String USD = "USD";
  private static final Set<String> CREATE_AMENITIES = Set.of("Wi-Fi", "Balcony");

  private static final String UPDATE_ROOM_NUMBER = "205";
  private static final BigDecimal UPDATE_PRICE_AMOUNT = new BigDecimal("210.5");
  private static final String EUR = "EUR";
  private static final Set<String> UPDATE_AMENITIES = Set.of("Kitchen");

  private static final Set<String> UPDATE_ERROR_FIELDS =
      Set.of("roomNumber", "roomType", "pricePerNight", "roomStatus", "amenities");

  private static final String API_ROOM_NUMBER = "306";
  private static final BigDecimal API_PRICE_AMOUNT = new BigDecimal("99.95");
  private static final Set<String> API_AMENITIES = Set.of("Sea view");

  private final RoomFactory roomFactory = new RoomFactory();

  @Test
  void mapsCreateRoomToLogicCommand() {
    CreateRoom request =
        new CreateRoom(
            CREATE_ROOM_NUMBER,
            RoomType.DOUBLE,
            new Money(CREATE_PRICE_AMOUNT, USD),
            RoomStatus.ACTIVE,
            CREATE_AMENITIES);

    CreateRoomCommand mapped = roomFactory.toLogic(request);

    assertNotNull(mapped.roomId());
    assertEquals(CREATE_ROOM_NUMBER, mapped.roomNumber());
    assertEquals(edu.ukma.ssdm.teamspectrum.logic.room.RoomType.DOUBLE, mapped.roomType());
    assertEquals(
        CREATE_PRICE_AMOUNT, mapped.pricePerNight().getNumber().numberValue(BigDecimal.class));
    assertEquals(USD, mapped.pricePerNight().getCurrency().getCurrencyCode());
    assertEquals(edu.ukma.ssdm.teamspectrum.logic.room.RoomStatus.ACTIVE, mapped.roomStatus());
    assertEquals(CREATE_AMENITIES, mapped.amenities());
  }

  @Test
  void mapsUpdateRoomToLogicCommand() {
    UpdateRoom request =
        new UpdateRoom(
            UPDATE_ROOM_NUMBER,
            RoomType.SUITE,
            new Money(UPDATE_PRICE_AMOUNT, EUR),
            RoomStatus.INACTIVE,
            UPDATE_AMENITIES);

    UpdateRoomCommand mapped = roomFactory.toLogic(request);

    assertEquals(UPDATE_ROOM_NUMBER, mapped.roomNumber());
    assertEquals(edu.ukma.ssdm.teamspectrum.logic.room.RoomType.SUITE, mapped.roomType());
    assertEquals(
        UPDATE_PRICE_AMOUNT, mapped.pricePerNight().getNumber().numberValue(BigDecimal.class));
    assertEquals(EUR, mapped.pricePerNight().getCurrency().getCurrencyCode());
    assertEquals(edu.ukma.ssdm.teamspectrum.logic.room.RoomStatus.INACTIVE, mapped.roomStatus());
    assertEquals(UPDATE_AMENITIES, mapped.amenities());
  }

  @Test
  void rejectsUpdateRoomWithNoFields() {
    WebApplicationException exception =
        assertThrows(
            WebApplicationException.class,
            () -> roomFactory.toLogic(new UpdateRoom(null, null, null, null, null)));

    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), exception.getResponse().getStatus());
    ValidationError error = (ValidationError) exception.getResponse().getEntity();
    assertEquals(
        UPDATE_ERROR_FIELDS,
        error.getFields().stream()
            .map(ValidationError.FieldError::path)
            .collect(java.util.stream.Collectors.toSet()));
    assertEquals(
        Set.of(RoomFactory.allNullMessage),
        error.getFields().stream()
            .map(ValidationError.FieldError::message)
            .collect(java.util.stream.Collectors.toSet()));
  }

  @Test
  void mapsRoomRecordToApiRoom() {
    UUID roomId = UUID.randomUUID();
    UUID hotelId = UUID.randomUUID();
    RoomRecord record =
        new RoomRecord(
            roomId,
            hotelId,
            API_ROOM_NUMBER,
            edu.ukma.ssdm.teamspectrum.logic.room.RoomType.SINGLE,
            org.javamoney.moneta.Money.of(API_PRICE_AMOUNT, USD),
            edu.ukma.ssdm.teamspectrum.logic.room.RoomStatus.INACTIVE,
            API_AMENITIES);

    Room mapped = roomFactory.toApi(record);

    assertEquals(roomId, mapped.roomId());
    assertEquals(hotelId, mapped.hotelId());
    assertEquals(API_ROOM_NUMBER, mapped.roomNumber());
    assertEquals(RoomType.SINGLE, mapped.roomType());
    assertEquals(API_PRICE_AMOUNT, mapped.pricePerNight().amount());
    assertEquals(USD, mapped.pricePerNight().currency());
    assertEquals(RoomStatus.INACTIVE, mapped.roomStatus());
    assertEquals(API_AMENITIES, mapped.amenities());
  }

  @Test
  void mapsApiRoomTypeToLogicRoomType() {
    assertEquals(
        edu.ukma.ssdm.teamspectrum.logic.room.RoomType.SUITE, roomFactory.toLogic(RoomType.SUITE));
  }

  @Test
  void mapsApiRoomStatusToLogicRoomStatus() {
    assertEquals(
        edu.ukma.ssdm.teamspectrum.logic.room.RoomStatus.INACTIVE,
        roomFactory.toLogic(RoomStatus.INACTIVE));
  }
}
