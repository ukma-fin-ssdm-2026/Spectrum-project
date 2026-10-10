/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.ukma.ssdm.teamspectrum.logic.hotel.HotelAddress;
import edu.ukma.ssdm.teamspectrum.logic.hotel.HotelRecord;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HotelFactoryTest {
  private HotelAddressFactory addressFactory;
  private HotelFactory hotelFactory;

  @BeforeEach
  void setUp() {
    addressFactory = mock(HotelAddressFactory.class);
    hotelFactory = new HotelFactory(addressFactory);
  }

  @Test
  void mapsCreateHotelToHotelRecord() {
    Address apiAddress = new Address("Kyiv", "Podil", "Skovorody", 34);
    HotelAddress logicAddress = new HotelAddress("Kyiv", "Podil", "Skovorody", 34);
    when(addressFactory.create(apiAddress)).thenReturn(logicAddress);
    CreateHotel request = new CreateHotel("Grand Budapest", apiAddress);

    HotelRecord mapped = hotelFactory.create(request);

    assertNotNull(mapped.id());
    assertEquals("Grand Budapest", mapped.name());
    assertEquals(logicAddress, mapped.address());
    assertEquals(BigDecimal.ZERO, mapped.pricePerNight());
    assertEquals(0, mapped.rating());
    assertEquals(Set.of(), mapped.amenities());
    verify(addressFactory).create(apiAddress);
  }

  @Test
  void mapsHotelRecordToHotel() {
    UUID id = UUID.randomUUID();
    HotelAddress logicAddress = new HotelAddress("Kyiv", "Podil", "Skovorody", 34);
    Address apiAddress = new Address("Kyiv", "Podil", "Skovorody", 34);
    when(addressFactory.create(logicAddress)).thenReturn(apiAddress);
    HotelRecord record =
        new HotelRecord(
            id,
            "Grand Budapest",
            logicAddress,
            "hotel.png",
            new BigDecimal("125.50"),
            4.7,
            Set.of("wifi", "parking"));

    Hotel mapped = hotelFactory.create(record);

    assertEquals(id, mapped.hotelId());
    assertEquals("Grand Budapest", mapped.name());
    assertEquals(apiAddress, mapped.address());
    assertEquals("hotel.png", mapped.pictureUrl());
    assertEquals(new BigDecimal("125.50"), mapped.pricePerNight());
    assertEquals(4.7, mapped.rating());
    assertEquals(Set.of("wifi", "parking"), mapped.amenities());
    verify(addressFactory).create(logicAddress);
  }
}
