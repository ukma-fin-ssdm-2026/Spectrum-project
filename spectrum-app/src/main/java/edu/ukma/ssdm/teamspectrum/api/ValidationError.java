/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api;

import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record ValidationError(List<FieldError> fields) {
  public record FieldError(
      @Schema(examples = {"name"}) String path,
      @Schema(examples = {"size must be between 3 and 40"}) String message) {}
}
