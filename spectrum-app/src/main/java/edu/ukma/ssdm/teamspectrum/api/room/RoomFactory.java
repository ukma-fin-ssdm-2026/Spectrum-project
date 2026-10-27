/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.room;

import edu.ukma.ssdm.teamspectrum.api.common.error.ValidationError;
import edu.ukma.ssdm.teamspectrum.logic.room.CreateRoomCommand;
import edu.ukma.ssdm.teamspectrum.logic.room.RoomRecord;
import edu.ukma.ssdm.teamspectrum.logic.room.UpdateRoomCommand;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class RoomFactory {
  static final String allNullMessage = "at least one field must be provided";

  public CreateRoomCommand toLogic(CreateRoom request) {
    return new CreateRoomCommand(
        UUID.randomUUID(),
        request.roomNumber(),
        edu.ukma.ssdm.teamspectrum.logic.room.RoomType.valueOf(request.roomType().name()),
        org.javamoney.moneta.Money.of(
            request.pricePerNight().amount(), request.pricePerNight().currency()),
        edu.ukma.ssdm.teamspectrum.logic.room.RoomStatus.valueOf(request.roomStatus().name()),
        request.amenities());
  }

  public UpdateRoomCommand toLogic(UpdateRoom request) {
    if (request.roomNumber() == null
        && request.roomType() == null
        && request.pricePerNight() == null
        && request.roomStatus() == null
        && request.amenities() == null) {
      var error =
          new ValidationError(
              List.of(
                  new ValidationError.FieldError("roomNumber", allNullMessage),
                  new ValidationError.FieldError("roomType", allNullMessage),
                  new ValidationError.FieldError("pricePerNight", allNullMessage),
                  new ValidationError.FieldError("roomStatus", allNullMessage),
                  new ValidationError.FieldError("amenities", allNullMessage)));
      throw new WebApplicationException(
          Response.status(Response.Status.BAD_REQUEST).entity(error).build());
    }
    return new UpdateRoomCommand(
        request.roomNumber(),
        toLogic(request.roomType()),
        toLogic(request.pricePerNight()),
        toLogic(request.roomStatus()),
        request.amenities());
  }

  public Room toApi(RoomRecord room) {
    return new Room(
        room.id(),
        room.hotelId(),
        room.roomNumber(),
        toApi(room.roomType()),
        toApi(room.pricePerNight()),
        toApi(room.roomStatus()),
        room.amenities());
  }

  private org.javamoney.moneta.Money toLogic(edu.ukma.ssdm.teamspectrum.api.room.Money money) {
    return money == null ? null : org.javamoney.moneta.Money.of(money.amount(), money.currency());
  }

  private edu.ukma.ssdm.teamspectrum.api.room.Money toApi(org.javamoney.moneta.Money price) {
    return price == null
        ? null
        : new edu.ukma.ssdm.teamspectrum.api.room.Money(
            price.getNumber().numberValue(BigDecimal.class), price.getCurrency().getCurrencyCode());
  }

  public edu.ukma.ssdm.teamspectrum.logic.room.RoomType toLogic(RoomType type) {
    return type == null
        ? null
        : edu.ukma.ssdm.teamspectrum.logic.room.RoomType.valueOf(type.name());
  }

  private RoomType toApi(edu.ukma.ssdm.teamspectrum.logic.room.RoomType type) {
    return type == null ? null : RoomType.valueOf(type.name());
  }

  public edu.ukma.ssdm.teamspectrum.logic.room.RoomStatus toLogic(RoomStatus status) {
    return status == null
        ? null
        : edu.ukma.ssdm.teamspectrum.logic.room.RoomStatus.valueOf(status.name());
  }

  private RoomStatus toApi(edu.ukma.ssdm.teamspectrum.logic.room.RoomStatus status) {
    return status == null ? null : RoomStatus.valueOf(status.name());
  }
}
