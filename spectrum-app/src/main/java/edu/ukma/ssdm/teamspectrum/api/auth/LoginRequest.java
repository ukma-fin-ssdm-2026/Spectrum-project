/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record LoginRequest(
    @NotBlank @Email @Schema(examples = {RegisterRequest.emailExample}) String email,
    @NotBlank @Schema(examples = {RegisterRequest.passwordExample}) String password) {}
