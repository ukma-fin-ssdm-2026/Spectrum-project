/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.hotel;

import edu.ukma.ssdm.teamspectrum.logic.hotel.HotelAlreadyExistsException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class HotelAlreadyExistsExceptionMapper
    implements ExceptionMapper<HotelAlreadyExistsException> {
  @Override
  public Response toResponse(HotelAlreadyExistsException e) {
    return Response.status(Response.Status.CONFLICT).build();
  }
}
