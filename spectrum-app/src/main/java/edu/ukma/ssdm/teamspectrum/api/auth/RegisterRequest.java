/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record RegisterRequest(
    @NotBlank @Email @Size(max = 254) @Schema(examples = {emailExample}) String email,
    @NotBlank @Size(min = 12, max = 72) @Schema(examples = {passwordExample}) String password) {
  public static final String emailExample = "example@gmail.com";
  public static final String passwordExample = "qwerty1234567"; // gitleaks:allow
}
