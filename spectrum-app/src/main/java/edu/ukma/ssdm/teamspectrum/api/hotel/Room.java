/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record Room(
    @Schema(examples = {Hotel.idExample}) UUID roomId,
    @Schema(examples = {Hotel.idExample}) UUID hotelId,
    @Schema(examples = {CreateRoom.roomNumberExample}) String roomNumber,
    @Schema(examples = {RoomType.example}) RoomType roomType,
    Money pricePerNight,
    @Schema(examples = {RoomStatus.example}) RoomStatus roomStatus) {}
