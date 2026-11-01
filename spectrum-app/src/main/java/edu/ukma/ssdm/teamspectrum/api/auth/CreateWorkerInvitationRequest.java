/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateWorkerInvitationRequest(@NotBlank @Email @Size(max = 254) String email) {}
