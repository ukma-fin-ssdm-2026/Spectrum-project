/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.common.error;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
  @Override
  public Response toResponse(ConstraintViolationException e) {
    List<ValidationError.FieldError> errors =
        e.getConstraintViolations().stream()
            .map(
                v ->
                    new ValidationError.FieldError(
                        StreamSupport.stream(v.getPropertyPath().spliterator(), false)
                            .skip(2) // skip method name and parameter name
                            .map(Path.Node::getName)
                            .collect(Collectors.joining(".")),
                        v.getMessage()))
            .toList();
    ValidationError body = new ValidationError(errors);
    return Response.status(400).entity(body).build();
  }
}
