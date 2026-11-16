/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompleteWorkerRegistrationRequest(
    @NotBlank @Size(max = 128) String token, @NotBlank @Size(min = 12, max = 72) String password) {}
