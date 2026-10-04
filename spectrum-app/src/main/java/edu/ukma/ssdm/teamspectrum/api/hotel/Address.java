/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import jakarta.validation.constraints.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record Address(
    @NotBlank(message = "City can't be empty")
        @Size(min = cityMinSize, max = cityMaxSize)
        @Schema(examples = {cityExample})
        String city,
    @NotBlank(message = "District can't be empty")
        @Size(min = districtMinSize, max = districtMaxSize)
        @Schema(examples = {districtExample})
        String district,
    @NotBlank(message = "Street can't be empty")
        @Size(min = streetMinSize, max = streetMaxSize)
        @Schema(examples = {streetExample})
        String street,
    @NotNull
        @Min(buildingNumberMin)
        @Max(buildingNumberMax)
        @Schema(examples = {buildingNumberExample})
        Integer buildingNumber) {
  static final int cityMinSize = 3;
  static final int cityMaxSize = 30;
  static final String cityExample = "Kyiv";

  static final int districtMinSize = 3;
  static final int districtMaxSize = 30;
  static final String districtExample = "Podil's'kyi";

  static final int streetMinSize = 3;
  static final int streetMaxSize = 30;
  static final String streetExample = "Skovorody";

  static final int buildingNumberMin = 1;
  static final int buildingNumberMax = 100;
  static final String buildingNumberExample = "34";
}
