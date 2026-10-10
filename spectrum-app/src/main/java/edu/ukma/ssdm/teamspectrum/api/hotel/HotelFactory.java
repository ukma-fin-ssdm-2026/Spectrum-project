/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import edu.ukma.ssdm.teamspectrum.edu.ukma.ssdm.teamspectrum.logic.hotel.HotelRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class HotelFactory {
  private final HotelAddressFactory addressFactory;

  @Inject
  public HotelFactory(HotelAddressFactory addressFactory) {
    this.addressFactory = addressFactory;
  }

  public HotelRecord create(CreateHotel request) {
    return new HotelRecord(
        UUID.randomUUID(),
        request.name(),
        addressFactory.create(request.address()),
        null,
        BigDecimal.ZERO,
        0,
        Set.of());
  }

  public Hotel create(HotelRecord hotel) {
    return new Hotel(
        hotel.id(),
        hotel.name(),
        addressFactory.create(hotel.address()),
        hotel.pictureUrl(),
        hotel.pricePerNight(),
        hotel.rating(),
        hotel.amenities());
  }
}
