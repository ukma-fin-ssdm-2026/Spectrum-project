/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import edu.ukma.ssdm.teamspectrum.logic.hotel.HotelAddress;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HotelAddressFactory {
  public HotelAddress create(Address address) {
    return new HotelAddress(
        address.city(), address.district(), address.street(), address.buildingNumber());
  }

  public Address create(HotelAddress address) {
    return new Address(
        address.city(), address.district(), address.street(), address.buildingNumber());
  }
}
