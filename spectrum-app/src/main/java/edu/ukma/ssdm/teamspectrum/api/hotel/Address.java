/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Address(
    @NotBlank(message = "City can't be empty") @Size(min = cityMinSize, max = cityMaxSize)
        String city,
    @NotBlank(message = "District can't be empty")
        @Size(min = districtMinSize, max = districtMaxSize)
        String district,
    @NotBlank(message = "Street can't be empty") @Size(min = streetMinSize, max = streetMaxSize)
        String street,
    @Min(buildingNumberMin) @Max(buildingNumberMax) int buildingNumber) {
  static final int cityMinSize = 3;
  static final int cityMaxSize = 30;
  static final int districtMinSize = 3;
  static final int districtMaxSize = 30;
  static final int streetMinSize = 3;
  static final int streetMaxSize = 30;
  static final int buildingNumberMin = 1;
  static final int buildingNumberMax = 100;
}
