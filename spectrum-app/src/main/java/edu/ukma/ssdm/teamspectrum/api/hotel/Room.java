/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import java.util.UUID;

public record Room(
    UUID roomId,
    UUID hotelId,
    String roomNumber,
    RoomType roomType,
    Money pricePerNight,
    RoomStatus roomStatus) {}
