/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.auth;

import edu.ukma.ssdm.teamspectrum.api.common.error.ValidationError;
import edu.ukma.ssdm.teamspectrum.logic.auth.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("v1/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "Account registration and login")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @POST
  @Path("register")
  @Operation(summary = "Register an account")
  @APIResponse(
      responseCode = "201",
      description = "Account registered",
      content = @Content(schema = @Schema(implementation = AuthResponse.class)))
  @APIResponse(responseCode = "409", description = "Email is already registered")
  @APIResponse(
      responseCode = "400",
      description = "Validation failed",
      content = @Content(schema = @Schema(implementation = ValidationError.class)))
  public Response register(@Valid @NotNull RegisterRequest request) {
    try {
      return authResponse(Response.Status.CREATED, authService.register(request));
    } catch (EmailAlreadyRegisteredException e) {
      return Response.status(Response.Status.CONFLICT).build();
    }
  }

  @POST
  @Path("login")
  @Operation(summary = "Log in and receive an access token")
  @APIResponse(
      responseCode = "200",
      description = "Login successful",
      content = @Content(schema = @Schema(implementation = AuthResponse.class)))
  @APIResponse(responseCode = "401", description = "Invalid email or password")
  @APIResponse(
      responseCode = "400",
      description = "Validation failed",
      content = @Content(schema = @Schema(implementation = ValidationError.class)))
  public Response login(@Valid @NotNull LoginRequest request) {
    try {
      return authResponse(Response.Status.OK, authService.login(request));
    } catch (InvalidCredentialsException e) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
  }

  private static Response authResponse(Response.Status status, IssuedToken token) {
    NewCookie cookie =
        new NewCookie.Builder("access_token")
            .value(token.value())
            .path("/")
            .maxAge(Math.toIntExact(token.expiresIn()))
            .secure(true)
            .httpOnly(true)
            .sameSite(NewCookie.SameSite.LAX)
            .build();
    return Response.status(status)
        .cookie(cookie)
        .entity(new AuthResponse(token.expiresIn()))
        .build();
  }
}
