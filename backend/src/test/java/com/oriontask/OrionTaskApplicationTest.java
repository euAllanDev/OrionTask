package com.oriontask;

import static org.assertj.core.api.Assertions.assertThat;

import com.oriontask.identity.application.port.out.PasswordHasher;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
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
    properties =
        "oriontask.security.session-hmac-key=VGhpcy1pcy1hLXRlc3QtaG1hYy1rZXktd2l0aC0zMi1ieXRlcy0xMjM0NTY=")
@Testcontainers
class OrionTaskApplicationTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

  @LocalServerPort private int port;

  @org.springframework.beans.factory.annotation.Autowired private JdbcTemplate jdbcTemplate;

  @org.springframework.beans.factory.annotation.Autowired private PasswordHasher passwordHasher;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void contextLoadsAndHealthDoesNotExposeDetails() throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/actuator/health"))
            .GET()
            .build();
    HttpResponse<String> response =
        HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

    JsonNode json = objectMapper.readTree(response.body());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(json.path("status").asText()).isEqualTo("UP");
    assertThat(json.has("components")).isFalse();
    assertThat(json.has("details")).isFalse();
    assertThat(json.has("groups")).isFalse();
  }

  @Test
  void registrationProtectsCredentialsAndPreventsEnumerationAndAbuse() throws Exception {
    HttpResponse<String> created =
        register(Map.of("email", " User@Example.com ", "password", "long-password-1"));
    HttpResponse<String> duplicate =
        register(Map.of("email", "user@example.com", "password", "long-password-2"));
    HttpResponse<String> unexpectedField =
        register(
            Map.of("email", "other@example.com", "password", "long-password-3", "role", "ADMIN"));

    assertThat(created.statusCode()).isEqualTo(202);
    assertThat(duplicate.statusCode()).isEqualTo(202);
    assertThat(unexpectedField.statusCode()).isEqualTo(400);
    assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from identity_accounts where normalized_email = ?",
                Integer.class,
                "user@example.com"))
        .isEqualTo(1);
    assertThat(
            jdbcTemplate.queryForObject(
                "select password_hash from identity_accounts where normalized_email = ?",
                String.class,
                "user@example.com"))
        .startsWith("$argon2id$")
        .doesNotContain("long-password-1");

    for (int index = 0; index < 3; index++) {
      assertThat(
              register(
                      Map.of(
                          "email", "user" + index + "@example.com", "password", "long-password-4"))
                  .statusCode())
          .isEqualTo(202);
    }
    assertThat(
            register(Map.of("email", "limited@example.com", "password", "long-password-5"))
                .statusCode())
        .isEqualTo(429);
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void authenticationUsesCsrfAndPersistsOnlySessionTokenDerivation() throws Exception {
    String email = "login@example.com";
    String password = "long-password-6";
    jdbcTemplate.update(
        "insert into identity_accounts (id, normalized_email, password_hash, created_at) values (?, ?, ?, current_timestamp)",
        UUID.randomUUID(),
        email,
        passwordHasher.hash(password));

    HttpResponse<String> csrf = csrf();
    String csrfToken = objectMapper.readTree(csrf.body()).path("token").asText();
    String csrfCookie = cookieValue(csrf, "XSRF-TOKEN");
    HttpResponse<String> login =
        login(Map.of("email", email, "password", password), csrfToken, csrfCookie);

    assertThat(login.statusCode()).isEqualTo(204);
    String sessionCookie = cookieValue(login, "__Host-oriontask-session");
    assertThat(sessionCookie).isNotBlank();
    assertThat(login.headers().allValues("Set-Cookie").getFirst())
        .contains("Secure", "HttpOnly", "SameSite=Lax", "Path=/")
        .doesNotContain("Domain=");
    assertThat(
            jdbcTemplate.queryForObject(
                "select token_derivation from identity_authentication_sessions", String.class))
        .doesNotContain(sessionCookie);

    HttpResponse<String> renewedCsrf = csrf();
    HttpResponse<String> secondLogin =
        login(
            Map.of("email", email, "password", password),
            objectMapper.readTree(renewedCsrf.body()).path("token").asText(),
            cookieValue(renewedCsrf, "XSRF-TOKEN"));
    assertThat(secondLogin.statusCode()).isEqualTo(204);

    HttpResponse<String> csrfForLogout = csrf();
    HttpResponse<String> logout =
        logout(
            objectMapper.readTree(csrfForLogout.body()).path("token").asText(),
            cookieValue(csrfForLogout, "XSRF-TOKEN"),
            sessionCookie);

    assertThat(logout.statusCode()).isEqualTo(204);
    assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from identity_authentication_sessions where revoked_at is not null",
                Integer.class))
        .isEqualTo(1);
    assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from identity_authentication_sessions where revoked_at is null",
                Integer.class))
        .isEqualTo(1);

    HttpResponse<String> csrfAfterLogout = csrf();
    HttpResponse<String> repeatedLogout =
        logout(
            objectMapper.readTree(csrfAfterLogout.body()).path("token").asText(),
            cookieValue(csrfAfterLogout, "XSRF-TOKEN"),
            sessionCookie);

    assertThat(repeatedLogout.statusCode()).isEqualTo(204);

    HttpResponse<String> csrfWithoutSession = csrf();
    HttpResponse<String> logoutWithoutSession =
        logout(
            objectMapper.readTree(csrfWithoutSession.body()).path("token").asText(),
            cookieValue(csrfWithoutSession, "XSRF-TOKEN"),
            "");
    assertThat(logoutWithoutSession.statusCode()).isEqualTo(204);
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void limitsLoginAttemptsByOriginWithRetryAfter() throws Exception {
    HttpResponse<String> csrf = csrf();
    String csrfToken = objectMapper.readTree(csrf.body()).path("token").asText();
    String csrfCookie = cookieValue(csrf, "XSRF-TOKEN");

    for (int index = 0; index < 20; index++) {
      HttpResponse<String> response =
          login(
              Map.of("email", "unknown" + index + "@example.com", "password", "incorrect-password"),
              csrfToken,
              csrfCookie);
      assertThat(response.statusCode()).isEqualTo(401);
    }

    HttpResponse<String> blocked =
        login(
            Map.of("email", "unknown-extra@example.com", "password", "incorrect-password"),
            csrfToken,
            csrfCookie);

    assertThat(blocked.statusCode()).isEqualTo(429);
    assertThat(blocked.headers().firstValue("Retry-After")).hasValue("900");
  }

  private HttpResponse<String> register(Map<String, String> payload) throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/accounts"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
            .build();
    return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> csrf() throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/csrf"))
            .GET()
            .build();
    return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> login(
      Map<String, String> payload, String csrfToken, String csrfCookie) throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/sessions"))
            .header("Content-Type", "application/json")
            .header("X-XSRF-TOKEN", csrfToken)
            .header("Cookie", "XSRF-TOKEN=" + csrfCookie)
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
            .build();
    return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> logout(String csrfToken, String csrfCookie, String sessionCookie)
      throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/session"))
            .header("X-XSRF-TOKEN", csrfToken)
            .header(
                "Cookie",
                "XSRF-TOKEN="
                    + csrfCookie
                    + (sessionCookie.isEmpty()
                        ? ""
                        : "; __Host-oriontask-session=" + sessionCookie))
            .DELETE()
            .build();
    return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
  }

  private static String cookieValue(HttpResponse<String> response, String name) {
    return response.headers().allValues("Set-Cookie").stream()
        .filter(value -> value.startsWith(name + "="))
        .map(value -> value.substring(name.length() + 1, value.indexOf(';')))
        .findFirst()
        .orElseThrow();
  }
}
