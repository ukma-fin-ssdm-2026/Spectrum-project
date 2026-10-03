/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record Hotel(UUID hotelId, @Schema(examples = {nameExample}) String name, Address address) {
  static final String nameExample = "Grand Budapest";
}
