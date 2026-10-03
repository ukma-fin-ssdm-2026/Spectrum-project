/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateHotel(
    @NotBlank @Size(min = nameMinSize, max = nameMaxSize) String name,
    @NotNull @Valid Address address) {
  static final int nameMinSize = 3;
  static final int nameMaxSize = 40;
}
