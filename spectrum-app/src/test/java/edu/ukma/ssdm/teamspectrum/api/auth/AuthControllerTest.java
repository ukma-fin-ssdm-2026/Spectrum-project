/* (C)2026 */
package edu.ukma.ssdm.teamspectrum.api.auth;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import edu.ukma.ssdm.teamspectrum.logic.auth.AuthService;
import edu.ukma.ssdm.teamspectrum.logic.auth.EmailAlreadyRegisteredException;
import edu.ukma.ssdm.teamspectrum.logic.auth.InvalidCredentialsException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(AuthControllerTest.AuthTestProfile.class)
class AuthControllerTest {
  @InjectMock AuthService authService;

  final String registerPath = "/v1/auth/register";
  final String loginPath = "/v1/auth/login";

  public static class AuthTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of("JWT_SECRET", "test-secret-for-auth-controller");
    }
  }

  @Test
  void registerReturnsCreatedAndAuthTokenCookie() {
    var tokenValue = "test-token";
    when(authService.register(new RegisterRequest("user@example.com", "validPassword12")))
        .thenReturn(new IssuedToken(tokenValue, 900));

    Response response =
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {"email":"user@example.com","password":"validPassword12"}
                """)
            .when()
            .post(registerPath);

    response.then().statusCode(201);
    assertEquals(tokenValue, response.getCookie(AuthController.authTokenName));
  }

  @Test
  void registerReturnsBadRequestForInvalidRequest() {
    given()
        .contentType(ContentType.JSON)
        .body(
            """
            {"email":"invalid","password":"validPassword12"}
            """)
        .when()
        .post(registerPath)
        .then()
        .statusCode(400);
  }

  @Test
  void loginReturnsUnauthorizedForInvalidCredentials() {
    when(authService.login(new LoginRequest("user@example.com", "validPassword12")))
        .thenThrow(new InvalidCredentialsException());

    given()
        .contentType(ContentType.JSON)
        .body(
            """
            {"email":"user@example.com","password":"validPassword12"}
            """)
        .when()
        .post(loginPath)
        .then()
        .statusCode(401);
  }

  @Test
  void loginReturnsSuccessAndAuthTokenCookie() {
    var tokenValue = "test-token";
    when(authService.login(new LoginRequest("user@example.com", "validPassword12")))
        .thenReturn(new IssuedToken(tokenValue, 900));

    Response response =
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {"email":"user@example.com","password":"validPassword12"}
                """)
            .when()
            .post(loginPath);

    response.then().statusCode(200);
    assertEquals(tokenValue, response.getCookie(AuthController.authTokenName));
  }

  @Test
  void registerReturnsConflictWhenEmailAlreadyExists() {
    when(authService.register(new RegisterRequest("user@example.com", "validPassword12")))
        .thenThrow(new EmailAlreadyRegisteredException());

    given()
        .contentType(ContentType.JSON)
        .body(
            """
            {"email":"user@example.com","password":"validPassword12"}
            """)
        .when()
        .post(registerPath)
        .then()
        .statusCode(409);
  }
}
