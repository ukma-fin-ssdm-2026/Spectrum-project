/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.auth;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import edu.ukma.ssdm.teamspectrum.api.ControllerTestProfile;
import edu.ukma.ssdm.teamspectrum.config.AppConfig.JwtConfig;
import edu.ukma.ssdm.teamspectrum.logic.auth.EmailAlreadyRegisteredException;
import edu.ukma.ssdm.teamspectrum.logic.auth.Role;
import edu.ukma.ssdm.teamspectrum.logic.auth.TokenFactory;
import edu.ukma.ssdm.teamspectrum.logic.auth.TokenFactoryImpl;
import edu.ukma.ssdm.teamspectrum.logic.auth.User;
import edu.ukma.ssdm.teamspectrum.logic.auth.WorkerInvitation;
import edu.ukma.ssdm.teamspectrum.logic.auth.WorkerInvitationService;
import edu.ukma.ssdm.teamspectrum.logic.common.PositiveDuration;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestProfile(WorkerInvitationControllerTest.InvitationTestProfile.class)
@Tag("integration")
class WorkerInvitationControllerTest {
  @InjectMock WorkerInvitationService invitationService;

  static final String jwtSecret = "test-secret-for-worker-invitation-controller-0123456789";
  final TokenFactory tokenFactory =
      new TokenFactoryImpl(
          new JwtConfig(jwtSecret, "spectrum-app", new PositiveDuration(Duration.ofMinutes(15))));
  final String invitationsPath = "/v1/api/admin/worker-invitations";
  final String createBody =
      """
      {"email":"worker@example.com"}
      """;

  public static class InvitationTestProfile extends ControllerTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      var overrides = new HashMap<>(super.getConfigOverrides());
      overrides.put("JWT_SECRET", jwtSecret);
      return overrides;
    }
  }

  @Test
  void createReturnsCreatedAndInvitationForAdmin() {
    var invitation =
        new WorkerInvitation(
            "https://example.com/register/worker?token=test-token",
            Instant.parse("2026-12-01T12:00:00Z"));
    when(invitationService.createInvitation("worker@example.com")).thenReturn(invitation);

    given()
        .contentType(ContentType.JSON)
        .cookie(AuthController.authTokenName, token(Role.ADMIN))
        .body(createBody)
        .when()
        .post(invitationsPath)
        .then()
        .statusCode(201)
        .contentType(ContentType.JSON)
        .body("registrationUrl", equalTo(invitation.registrationUrl()))
        .body("expiresAt", equalTo(invitation.expiresAt().toString()));

    verify(invitationService).createInvitation("worker@example.com");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{\"email\":\"invalid\"}",
        "{\"email\":\"\"}",
        "{\"email\":\"   \"}",
        "{}",
        "null"
      })
  void createReturnsBadRequestForInvalidRequest(String body) {
    given()
        .contentType(ContentType.JSON)
        .cookie(AuthController.authTokenName, token(Role.ADMIN))
        .body(body)
        .when()
        .post(invitationsPath)
        .then()
        .statusCode(400);

    verifyNoInteractions(invitationService);
  }

  @Test
  void createReturnsBadRequestForEmailExceedingMaxLength() {
    given()
        .contentType(ContentType.JSON)
        .cookie(AuthController.authTokenName, token(Role.ADMIN))
        .body(Map.of("email", "a".repeat(243) + "@example.com"))
        .when()
        .post(invitationsPath)
        .then()
        .statusCode(400);

    verifyNoInteractions(invitationService);
  }

  @Test
  void createReturnsUnauthorizedWithoutToken() {
    given()
        .contentType(ContentType.JSON)
        .body(createBody)
        .when()
        .post(invitationsPath)
        .then()
        .statusCode(401);

    verifyNoInteractions(invitationService);
  }

  @ParameterizedTest
  @EnumSource(
      value = Role.class,
      names = {"CLIENT", "WORKER"})
  void createReturnsForbiddenForNonAdmin(Role role) {
    given()
        .contentType(ContentType.JSON)
        .cookie(AuthController.authTokenName, token(role))
        .body(createBody)
        .when()
        .post(invitationsPath)
        .then()
        .statusCode(403);

    verifyNoInteractions(invitationService);
  }

  @Test
  void createReturnsConflictWhenEmailAlreadyRegistered() {
    when(invitationService.createInvitation("worker@example.com"))
        .thenThrow(new EmailAlreadyRegisteredException());

    given()
        .contentType(ContentType.JSON)
        .cookie(AuthController.authTokenName, token(Role.ADMIN))
        .body(createBody)
        .when()
        .post(invitationsPath)
        .then()
        .statusCode(409)
        .body(equalTo(""));

    verify(invitationService).createInvitation("worker@example.com");
  }

  private String token(Role role) {
    var user = new User(UUID.randomUUID(), "staff@example.com", "hash", role);
    return tokenFactory.create(user).value();
  }
}
