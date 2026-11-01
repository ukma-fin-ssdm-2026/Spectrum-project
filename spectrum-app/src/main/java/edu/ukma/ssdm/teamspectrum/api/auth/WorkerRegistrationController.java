/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.auth;

import edu.ukma.ssdm.teamspectrum.api.common.error.ValidationError;
import edu.ukma.ssdm.teamspectrum.logic.auth.EmailAlreadyRegisteredException;
import edu.ukma.ssdm.teamspectrum.logic.auth.InvalidWorkerInvitationException;
import edu.ukma.ssdm.teamspectrum.logic.auth.WorkerInvitationService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("v1/auth/register/worker")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "Account registration and login")
public class WorkerRegistrationController {
  private final WorkerInvitationService invitationService;

  @Inject
  public WorkerRegistrationController(WorkerInvitationService invitationService) {
    this.invitationService = invitationService;
  }

  @POST
  @Operation(summary = "Complete worker registration using a one-time invitation")
  @APIResponse(
      responseCode = "201",
      description = "Worker account registered",
      content = @Content(schema = @Schema(implementation = AuthResponse.class)))
  @APIResponse(
      responseCode = "400",
      description = "Validation failed",
      content = @Content(schema = @Schema(implementation = ValidationError.class)))
  @APIResponse(responseCode = "409", description = "Email is already registered")
  @APIResponse(
      responseCode = "410",
      description = "Invitation is invalid, expired, or already used")
  public Response register(@Valid @NotNull CompleteWorkerRegistrationRequest request) {
    try {
      return AuthController.authResponse(
          Response.Status.CREATED,
          invitationService.completeRegistration(request.token(), request.password()));
    } catch (EmailAlreadyRegisteredException e) {
      return Response.status(Response.Status.CONFLICT).build();
    } catch (InvalidWorkerInvitationException e) {
      return Response.status(Response.Status.GONE).build();
    }
  }
}
