/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record CreateHotel(
    @NotBlank @Size(min = nameMinSize, max = nameMaxSize) @Schema(examples = {Hotel.nameExample}) String name,
    @NotNull @Valid Address address) {
  static final int nameMinSize = 3;
  static final int nameMaxSize = 40;
}
