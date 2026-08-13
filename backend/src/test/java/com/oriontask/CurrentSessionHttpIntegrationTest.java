package com.oriontask;

import static org.assertj.core.api.Assertions.assertThat;

import com.oriontask.identity.application.port.out.PasswordHasher;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "oriontask.security.session-hmac-key=VGhpcy1pcy1hLXRlc3QtaG1hYy1rZXktd2l0aC1zMi1ieXRocy0xMjM0NTY=",
      "server.servlet.session.cookie.secure=true"
    })
@Testcontainers
class CurrentSessionHttpIntegrationTest {
  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

  @LocalServerPort private int port;
  @Autowired private JdbcTemplate jdbc;
  @Autowired private PasswordHasher passwordHasher;
  private final ObjectMapper json = new ObjectMapper();

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void returnsOnlyStoredIdentityWithoutCsrf() throws Exception {
    AccountSession account = accountSession(" User@Example.Com ");

    HttpResponse<String> response = current(account.sessionToken());

    assertThat(response.statusCode()).isEqualTo(200);
    JsonNode body = json.readTree(response.body());
    assertThat(body.propertyNames()).containsExactlyInAnyOrder("accountId", "email");
    assertThat(body.path("accountId").asText()).isEqualTo(account.accountId().toString());
    assertThat(body.path("email").asText()).isEqualTo("user@example.com");
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void doesNotLeakSessionValidityForAbsentExpiredOrRevokedSessions() throws Exception {
    HttpResponse<String> absent = current(null);
    AccountSession expired = accountSession("expired@example.com");
    jdbc.update(
        "update identity_authentication_sessions set absolute_expires_at = current_timestamp - interval '1 second' where account_id = ?",
        expired.accountId());
    HttpResponse<String> expiredResponse = current(expired.sessionToken());
    AccountSession revoked = accountSession("revoked@example.com");
    jdbc.update(
        "update identity_authentication_sessions set revoked_at = current_timestamp where account_id = ?",
        revoked.accountId());
    HttpResponse<String> revokedResponse = current(revoked.sessionToken());

    assertThat(absent.statusCode()).isEqualTo(401);
    assertThat(expiredResponse.statusCode()).isEqualTo(401);
    assertThat(revokedResponse.statusCode()).isEqualTo(401);
    assertThat(expiredResponse.body()).isEqualTo(absent.body());
    assertThat(revokedResponse.body()).isEqualTo(absent.body());
    assertThat(absent.body()).doesNotContain("accountId", "email");
  }

  private AccountSession accountSession(String suppliedEmail) throws Exception {
    String email = suppliedEmail.trim().toLowerCase(java.util.Locale.ROOT);
    UUID accountId = UUID.randomUUID();
    jdbc.update(
        "insert into identity_accounts (id, normalized_email, password_hash, created_at) values (?, ?, ?, current_timestamp)",
        accountId,
        email,
        passwordHasher.hash("long-password-session"));
    HttpResponse<String> csrf = csrf();
    HttpResponse<String> login =
        request(
            "POST",
            "/api/v1/sessions",
            "{\"email\":\"" + suppliedEmail + "\",\"password\":\"long-password-session\"}",
            "JSESSIONID=" + cookie(csrf, "JSESSIONID"),
            token(csrf));
    return new AccountSession(accountId, cookie(login, "__Host-oriontask-session"));
  }

  private HttpResponse<String> current(String sessionToken) throws Exception {
    return request(
        "GET",
        "/api/v1/session",
        null,
        sessionToken == null ? null : "__Host-oriontask-session=" + sessionToken,
        null);
  }

  private HttpResponse<String> csrf() throws Exception {
    return request("GET", "/api/v1/csrf", null, null, null);
  }

  private HttpResponse<String> request(
      String method, String path, String body, String technicalSession, String csrfToken)
      throws Exception {
    HttpRequest.Builder request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
    if (technicalSession != null) {
      request.header("Cookie", technicalSession);
    }
    if (csrfToken != null) {
      request.header("X-CSRF-TOKEN", csrfToken);
    }
    if (body != null) {
      request.header("Content-Type", "application/json");
    }
    request.method(
        method,
        body == null
            ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofString(body));
    return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
  }

  private String token(HttpResponse<String> response) throws Exception {
    return json.readTree(response.body()).path("token").asText();
  }

  private static String cookie(HttpResponse<String> response, String name) {
    return response.headers().allValues("Set-Cookie").stream()
        .filter(value -> value.startsWith(name + "="))
        .map(value -> value.substring(name.length() + 1).split(";", 2)[0])
        .findFirst()
        .orElseThrow();
  }

  private record AccountSession(UUID accountId, String sessionToken) {}
}
