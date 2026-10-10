/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.ukma.ssdm.teamspectrum.logic.hotel.HotelAddress;
import org.junit.jupiter.api.Test;

class HotelAddressFactoryTest {
  private final HotelAddressFactory factory = new HotelAddressFactory();

  @Test
  void mapsApiAddressToLogicAddress() {
    Address apiAddress = new Address("Kyiv", "Podil", "Skovorody", 34);

    HotelAddress logicAddress = factory.create(apiAddress);

    assertEquals(new HotelAddress("Kyiv", "Podil", "Skovorody", 34), logicAddress);
  }

  @Test
  void mapsLogicAddressToApiAddress() {
    HotelAddress logicAddress = new HotelAddress("Kyiv", "Podil", "Skovorody", 34);

    Address apiAddress = factory.create(logicAddress);

    assertEquals(new Address("Kyiv", "Podil", "Skovorody", 34), apiAddress);
  }
}
