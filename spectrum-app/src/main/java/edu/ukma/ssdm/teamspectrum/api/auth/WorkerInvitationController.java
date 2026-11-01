/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.auth;

import edu.ukma.ssdm.teamspectrum.api.common.error.ValidationError;
import edu.ukma.ssdm.teamspectrum.logic.auth.EmailAlreadyRegisteredException;
import edu.ukma.ssdm.teamspectrum.logic.auth.Role;
import edu.ukma.ssdm.teamspectrum.logic.auth.WorkerInvitation;
import edu.ukma.ssdm.teamspectrum.logic.auth.WorkerInvitationService;
import jakarta.annotation.security.RolesAllowed;
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

@Path("v1/api/admin/worker-invitations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Worker invitations", description = "Administrator-managed worker onboarding")
public class WorkerInvitationController {
  private final WorkerInvitationService invitationService;

  @Inject
  public WorkerInvitationController(WorkerInvitationService invitationService) {
    this.invitationService = invitationService;
  }

  @POST
  @RolesAllowed(Role.Values.ADMIN)
  @Operation(summary = "Create a one-time worker registration link")
  @APIResponse(
      responseCode = "201",
      description = "Worker invitation created",
      content = @Content(schema = @Schema(implementation = WorkerInvitationResponse.class)))
  @APIResponse(
      responseCode = "400",
      description = "Validation failed",
      content = @Content(schema = @Schema(implementation = ValidationError.class)))
  @APIResponse(responseCode = "401", description = "Not authenticated")
  @APIResponse(responseCode = "403", description = "Only administrators can invite workers")
  @APIResponse(responseCode = "409", description = "Email is already registered")
  public Response create(@Valid @NotNull CreateWorkerInvitationRequest request) {
    try {
      WorkerInvitation invitation = invitationService.createInvitation(request.email());
      return Response.status(Response.Status.CREATED)
          .entity(
              new WorkerInvitationResponse(invitation.registrationUrl(), invitation.expiresAt()))
          .build();
    } catch (EmailAlreadyRegisteredException e) {
      return Response.status(Response.Status.CONFLICT).build();
    }
  }
}
