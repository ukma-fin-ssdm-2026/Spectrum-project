/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.auth;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import edu.ukma.ssdm.teamspectrum.api.ControllerTestProfile;
import edu.ukma.ssdm.teamspectrum.logic.auth.EmailAlreadyRegisteredException;
import edu.ukma.ssdm.teamspectrum.logic.auth.InvalidWorkerInvitationException;
import edu.ukma.ssdm.teamspectrum.logic.auth.WorkerInvitationService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@TestProfile(ControllerTestProfile.class)
@Tag("integration")
class WorkerRegistrationControllerTest {
  @InjectMock WorkerInvitationService invitationService;

  final String registerPath = "/v1/auth/register/worker";
  final String registerBody =
      """
      {"token":"invitation-token","password":"validPassword12"}
      """;

  @Test
  void registerReturnsCreatedAndAuthTokenCookie() {
    when(invitationService.completeRegistration("invitation-token", "validPassword12"))
        .thenReturn(new IssuedToken("test-token", 900));

    Response response =
        given().contentType(ContentType.JSON).body(registerBody).when().post(registerPath);

    response.then().statusCode(201).contentType(ContentType.JSON).body("expiresIn", equalTo(900));
    var cookie = response.getDetailedCookie(AuthController.authTokenName);
    assertEquals("test-token", cookie.getValue());
    assertEquals(AuthController.tokenPath, cookie.getPath());
    assertEquals(900, cookie.getMaxAge());
    assertTrue(cookie.isSecured());
    assertTrue(cookie.isHttpOnly());
    assertEquals("Lax", cookie.getSameSite());
    verify(invitationService).completeRegistration("invitation-token", "validPassword12");
  }

  @ParameterizedTest
  @MethodSource("invalidRequests")
  void registerReturnsBadRequestForInvalidRequest(String body) {
    Response response = given().contentType(ContentType.JSON).body(body).when().post(registerPath);

    response.then().statusCode(400);
    assertNull(response.getCookie(AuthController.authTokenName));
    verifyNoInteractions(invitationService);
  }

  static Stream<String> invalidRequests() {
    return Stream.of(
        "null",
        "{}",
        """
        {"password":"validPassword12"}
        """,
        """
        {"token":null,"password":"validPassword12"}
        """,
        """
        {"token":"","password":"validPassword12"}
        """,
        """
        {"token":" ","password":"validPassword12"}
        """,
        "{\"token\":\"" + "t".repeat(129) + "\",\"password\":\"validPassword12\"}",
        """
        {"token":"invitation-token"}
        """,
        """
        {"token":"invitation-token","password":null}
        """,
        """
        {"token":"invitation-token","password":""}
        """,
        """
        {"token":"invitation-token","password":"            "}
        """,
        "{\"token\":\"invitation-token\",\"password\":\"" + "p".repeat(11) + "\"}",
        "{\"token\":\"invitation-token\",\"password\":\"" + "p".repeat(73) + "\"}");
  }

  @Test
  void registerReturnsConflictWhenEmailAlreadyRegistered() {
    when(invitationService.completeRegistration("invitation-token", "validPassword12"))
        .thenThrow(new EmailAlreadyRegisteredException());

    Response response =
        given().contentType(ContentType.JSON).body(registerBody).when().post(registerPath);

    response.then().statusCode(409).body(equalTo(""));
    assertNull(response.getCookie(AuthController.authTokenName));
    verify(invitationService).completeRegistration("invitation-token", "validPassword12");
  }

  @Test
  void registerReturnsGoneWhenInvitationIsInvalidExpiredOrAlreadyUsed() {
    when(invitationService.completeRegistration("invitation-token", "validPassword12"))
        .thenThrow(new InvalidWorkerInvitationException());

    Response response =
        given().contentType(ContentType.JSON).body(registerBody).when().post(registerPath);

    response.then().statusCode(410).body(equalTo(""));
    assertNull(response.getCookie(AuthController.authTokenName));
    verify(invitationService).completeRegistration("invitation-token", "validPassword12");
  }
}
