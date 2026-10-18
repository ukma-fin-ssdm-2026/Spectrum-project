/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.logic.hotel;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HotelRepositoryStubTest {

  @Test
  void findByNameIgnoresLetterCase() {
    // Arrange
    var repository = new HotelRepositoryStub();
    repository.add(hotel("Spectrum Hotel"));

    // Act
    var result = repository.findByName("spectrum hotel");

    // Assert
    assertTrue(result.isPresent());
  }

  @Test
  void findByNameIgnoresSurroundingSpaces() {
    // Arrange
    var repository = new HotelRepositoryStub();
    repository.add(hotel("Spectrum Hotel"));

    // Act
    var result = repository.findByName("  Spectrum Hotel  ");

    // Assert
    assertTrue(result.isPresent());
  }

  private static HotelRecord hotel(String name) {
    return new HotelRecord(
        UUID.randomUUID(),
        name,
        new HotelAddress("Kyiv", null, "Main Street", 1),
        null,
        BigDecimal.TEN,
        4.5,
        Set.of("wifi"));
  }
}