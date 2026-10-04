/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.common.error;

import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ValidationError {
  private final List<FieldError> fields;

  public ValidationError(List<FieldError> fields) {
    this.fields = fields;
  }

  public List<FieldError> getFields() {
    return fields;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (ValidationError) obj;
    return Objects.equals(this.fields, that.fields);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fields);
  }

  @Override
  public String toString() {
    return "ValidationError[" + "fields=" + fields + ']';
  }

  public record FieldError(
      @Schema(examples = {"name"}) String path,
      @Schema(examples = {"size must be between 3 and 40"}) String message) {}
}
