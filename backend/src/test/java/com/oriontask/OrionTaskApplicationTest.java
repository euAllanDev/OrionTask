package com.oriontask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.oriontask.identity.application.port.out.PasswordHasher;
import com.oriontask.organization.application.port.out.OrganizationStore;
import com.oriontask.organization.domain.model.Membership;
import com.oriontask.organization.domain.model.Organization;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
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
    properties = {
      "oriontask.security.session-hmac-key=VGhpcy1pcy1hLXRlc3QtaG1hYy1rZXktd2l0aC0zMi1ieXRlcy0xMjM0NTY=",
      "server.servlet.session.cookie.secure=true"
    })
@Testcontainers
class OrionTaskApplicationTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

  @LocalServerPort private int port;

  @org.springframework.beans.factory.annotation.Autowired private JdbcTemplate jdbcTemplate;

  @org.springframework.beans.factory.annotation.Autowired private PasswordHasher passwordHasher;

  @org.springframework.beans.factory.annotation.Autowired
  private OrganizationStore organizationStore;

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
    UUID accountId = createAccount(email, password);

    HttpResponse<String> csrf = csrf();
    String csrfToken = objectMapper.readTree(csrf.body()).path("token").asText();
    String csrfSession = cookieValue(csrf, "JSESSIONID");
    HttpResponse<String> login =
        login(Map.of("email", email, "password", password), csrfToken, csrfSession);

    assertThat(login.statusCode()).isEqualTo(204);
    String sessionCookie = cookieValue(login, "__Host-oriontask-session");
    assertThat(sessionCookie).isNotBlank();
    assertThat(login.headers().allValues("Set-Cookie").getFirst())
        .contains("Secure", "HttpOnly", "SameSite=Lax", "Path=/")
        .doesNotContain("Domain=");
    assertThat(
            jdbcTemplate.queryForObject(
                "select token_derivation from identity_authentication_sessions where account_id = ?",
                String.class,
                accountId))
        .doesNotContain(sessionCookie);

    HttpResponse<String> renewedCsrf = csrf();
    HttpResponse<String> secondLogin =
        login(
            Map.of("email", email, "password", password),
            objectMapper.readTree(renewedCsrf.body()).path("token").asText(),
            cookieValue(renewedCsrf, "JSESSIONID"));
    assertThat(secondLogin.statusCode()).isEqualTo(204);

    HttpResponse<String> csrfForLogout = csrf();
    HttpResponse<String> logout =
        logout(
            objectMapper.readTree(csrfForLogout.body()).path("token").asText(),
            cookieValue(csrfForLogout, "JSESSIONID"),
            sessionCookie);

    assertThat(logout.statusCode()).isEqualTo(204);
    assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from identity_authentication_sessions "
                    + "where account_id = ? and revoked_at is not null",
                Integer.class,
                accountId))
        .isEqualTo(1);
    assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from identity_authentication_sessions "
                    + "where account_id = ? and revoked_at is null",
                Integer.class,
                accountId))
        .isEqualTo(1);

    HttpResponse<String> csrfAfterLogout = csrf();
    HttpResponse<String> repeatedLogout =
        logout(
            objectMapper.readTree(csrfAfterLogout.body()).path("token").asText(),
            cookieValue(csrfAfterLogout, "JSESSIONID"),
            sessionCookie);

    assertThat(repeatedLogout.statusCode()).isEqualTo(204);

    HttpResponse<String> csrfWithoutSession = csrf();
    HttpResponse<String> logoutWithoutSession =
        logout(
            objectMapper.readTree(csrfWithoutSession.body()).path("token").asText(),
            cookieValue(csrfWithoutSession, "JSESSIONID"),
            "");
    assertThat(logoutWithoutSession.statusCode()).isEqualTo(204);
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void limitsLoginAttemptsByOriginWithRetryAfter() throws Exception {
    HttpResponse<String> csrf = csrf();
    String csrfToken = objectMapper.readTree(csrf.body()).path("token").asText();
    String csrfSession = cookieValue(csrf, "JSESSIONID");

    for (int index = 0; index < 20; index++) {
      HttpResponse<String> response =
          login(
              Map.of("email", "unknown" + index + "@example.com", "password", "incorrect-password"),
              csrfToken,
              csrfSession);
      assertThat(response.statusCode()).isEqualTo(401);
    }

    HttpResponse<String> blocked =
        login(
            Map.of("email", "unknown-extra@example.com", "password", "incorrect-password"),
            csrfToken,
            csrfSession);

    assertThat(blocked.statusCode()).isEqualTo(429);
    assertThat(blocked.headers().firstValue("Retry-After")).hasValue("900");
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void rejectsMissingInvalidAndPreviousCsrfTokens() throws Exception {
    String email = "csrf@example.com";
    String password = "long-password-7";
    createAccount(email, password);

    HttpResponse<String> initialCsrf = csrf();
    String initialToken = csrfToken(initialCsrf);
    String initialTechnicalSession = cookieValue(initialCsrf, "JSESSIONID");

    assertThat(
            loginWithoutCsrf(Map.of("email", email, "password", password), initialTechnicalSession)
                .statusCode())
        .isEqualTo(403);

    HttpResponse<String> invalidCsrf = csrf();
    assertThat(
            login(
                    Map.of("email", email, "password", password),
                    "invalid-csrf-token",
                    cookieValue(invalidCsrf, "JSESSIONID"))
                .statusCode())
        .isEqualTo(403);

    HttpResponse<String> loginCsrf = csrf();
    HttpResponse<String> login =
        login(
            Map.of("email", email, "password", password),
            csrfToken(loginCsrf),
            cookieValue(loginCsrf, "JSESSIONID"));
    assertThat(login.statusCode()).isEqualTo(204);

    assertThat(
            login(
                    Map.of("email", email, "password", password),
                    initialToken,
                    initialTechnicalSession)
                .statusCode())
        .isEqualTo(403);

    HttpResponse<String> logoutCsrf = csrf();
    HttpResponse<String> logout =
        logout(
            csrfToken(logoutCsrf),
            cookieValue(logoutCsrf, "JSESSIONID"),
            cookieValue(login, "__Host-oriontask-session"));
    assertThat(logout.statusCode()).isEqualTo(204);

    assertThat(
            login(
                    Map.of("email", email, "password", password),
                    csrfToken(logoutCsrf),
                    cookieValue(logoutCsrf, "JSESSIONID"))
                .statusCode())
        .isEqualTo(403);
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void rotatesTechnicalSessionAndInvalidatesCsrfTokenAfterLogin() throws Exception {
    String email = "rotation@example.com";
    String password = "long-password-rotation";
    createAccount(email, password);

    HttpResponse<String> initialCsrf = csrf();
    String initialToken = csrfToken(initialCsrf);
    String initialTechnicalSession = cookieValue(initialCsrf, "JSESSIONID");
    assertThat(
            initialCsrf.headers().allValues("Set-Cookie").stream()
                .filter(value -> value.startsWith("JSESSIONID="))
                .findFirst()
                .orElseThrow())
        .contains("HttpOnly", "Secure", "SameSite=Lax", "Path=/")
        .doesNotContain("Domain=");

    HttpResponse<String> login =
        login(Map.of("email", email, "password", password), initialToken, initialTechnicalSession);
    String rotatedTechnicalSession = cookieValue(login, "JSESSIONID");

    assertThat(login.statusCode()).isEqualTo(204);
    assertThat(rotatedTechnicalSession).isNotEqualTo(initialTechnicalSession);
    assertThat(
            login(
                    Map.of("email", email, "password", password),
                    initialToken,
                    initialTechnicalSession)
                .statusCode())
        .isEqualTo(403);

    HttpResponse<String> refreshedCsrf = csrf(rotatedTechnicalSession);
    assertThat(
            login(
                    Map.of("email", email, "password", password),
                    csrfToken(refreshedCsrf),
                    rotatedTechnicalSession)
                .statusCode())
        .isEqualTo(204);
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void logoutDoesNotRevealUnknownOrExpiredSessions() throws Exception {
    String email = "expired@example.com";
    String password = "long-password-8";
    createAccount(email, password);

    HttpResponse<String> unknownCsrf = csrf();
    assertThat(
            logout(
                    csrfToken(unknownCsrf),
                    cookieValue(unknownCsrf, "JSESSIONID"),
                    "unknown-session-token")
                .statusCode())
        .isEqualTo(204);

    HttpResponse<String> loginCsrf = csrf();
    HttpResponse<String> login =
        login(
            Map.of("email", email, "password", password),
            csrfToken(loginCsrf),
            cookieValue(loginCsrf, "JSESSIONID"));
    String sessionToken = cookieValue(login, "__Host-oriontask-session");
    jdbcTemplate.update(
        "update identity_authentication_sessions set absolute_expires_at = current_timestamp - interval '1 second'");

    HttpResponse<String> expiredCsrf = csrf();
    assertThat(
            logout(csrfToken(expiredCsrf), cookieValue(expiredCsrf, "JSESSIONID"), sessionToken)
                .statusCode())
        .isEqualTo(204);
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void logoutOfOneAccountSessionDoesNotRevokeAnotherAccountSession() throws Exception {
    String firstEmail = "first@example.com";
    String secondEmail = "second@example.com";
    String password = "long-password-9";
    UUID firstAccountId = createAccount(firstEmail, password);
    UUID secondAccountId = createAccount(secondEmail, password);

    HttpResponse<String> firstCsrf = csrf();
    HttpResponse<String> firstLogin =
        login(
            Map.of("email", firstEmail, "password", password),
            csrfToken(firstCsrf),
            cookieValue(firstCsrf, "JSESSIONID"));
    HttpResponse<String> secondCsrf = csrf();
    HttpResponse<String> secondLogin =
        login(
            Map.of("email", secondEmail, "password", password),
            csrfToken(secondCsrf),
            cookieValue(secondCsrf, "JSESSIONID"));

    HttpResponse<String> logoutCsrf = csrf();
    assertThat(
            logout(
                    csrfToken(logoutCsrf),
                    cookieValue(logoutCsrf, "JSESSIONID"),
                    cookieValue(firstLogin, "__Host-oriontask-session"))
                .statusCode())
        .isEqualTo(204);

    assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from identity_authentication_sessions "
                    + "where account_id = ? and revoked_at is null",
                Integer.class,
                firstAccountId))
        .isZero();
    assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from identity_authentication_sessions "
                    + "where account_id = ? and revoked_at is null",
                Integer.class,
                secondAccountId))
        .isEqualTo(1);
    assertThat(cookieValue(secondLogin, "__Host-oriontask-session")).isNotBlank();
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void organizationCreationUsesSessionIdentityAndCreatesOwnerMembership() throws Exception {
    String password = "long-password-organization";
    UUID firstAccountId = createAccount("organization-first@example.com", password);
    UUID secondAccountId = createAccount("organization-second@example.com", password);
    HttpResponse<String> firstLogin =
        authenticatedLogin("organization-first@example.com", password);
    String firstTechnicalSession = cookieValue(firstLogin, "JSESSIONID");
    HttpResponse<String> firstCsrf = csrf(firstTechnicalSession);

    HttpResponse<String> firstOrganization =
        createOrganization(
            Map.of("name", "  Shared Organization  "),
            csrfToken(firstCsrf),
            firstTechnicalSession,
            cookieValue(firstLogin, "__Host-oriontask-session"));

    assertThat(firstOrganization.statusCode()).isEqualTo(201);
    JsonNode firstBody = objectMapper.readTree(firstOrganization.body());
    UUID firstOrganizationId = UUID.fromString(firstBody.path("id").asText());
    assertThat(firstBody.path("name").asText()).isEqualTo("Shared Organization");
    assertThat(firstBody.path("createdAt").asText()).isNotBlank();
    assertThat(firstOrganization.headers().firstValue("Location"))
        .hasValue("/api/v1/organizations/" + firstOrganizationId);

    HttpResponse<String> secondLogin =
        authenticatedLogin("organization-second@example.com", password);
    String secondTechnicalSession = cookieValue(secondLogin, "JSESSIONID");
    HttpResponse<String> secondCsrf = csrf(secondTechnicalSession);
    HttpResponse<String> secondOrganization =
        createOrganization(
            Map.of("name", "Shared Organization"),
            csrfToken(secondCsrf),
            secondTechnicalSession,
            cookieValue(secondLogin, "__Host-oriontask-session"));

    assertThat(secondOrganization.statusCode()).isEqualTo(201);
    assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from organizations where name = ?",
                Integer.class,
                "Shared Organization"))
        .isEqualTo(2);
    assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from organization_memberships where organization_id = ? "
                    + "and account_id = ? and role = 'OWNER'",
                Integer.class,
                firstOrganizationId,
                firstAccountId))
        .isEqualTo(1);
    assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from organization_memberships where organization_id = ? "
                    + "and account_id = ?",
                Integer.class,
                firstOrganizationId,
                secondAccountId))
        .isZero();
    assertThatThrownBy(
            () ->
                jdbcTemplate.update(
                    "insert into organization_memberships "
                        + "(id, organization_id, account_id, role, created_at, updated_at) "
                        + "values (?, ?, ?, 'OWNER', current_timestamp, current_timestamp)",
                    UUID.randomUUID(),
                    firstOrganizationId,
                    firstAccountId))
        .isInstanceOf(Exception.class);
  }

  @Test
  void organizationPersistenceRollsBackWhenInitialMembershipCannotBeCreated() {
    int organizationCountBefore =
        jdbcTemplate.queryForObject("select count(*) from organizations", Integer.class);
    Instant now = Instant.now();
    UUID organizationId = UUID.randomUUID();

    assertThatThrownBy(
            () ->
                organizationStore.create(
                    new Organization(organizationId, "Rollback Organization", now, now),
                    new Membership(
                        UUID.randomUUID(),
                        organizationId,
                        UUID.randomUUID(),
                        Membership.Role.OWNER,
                        now,
                        now)))
        .isInstanceOf(Exception.class);

    assertThat(jdbcTemplate.queryForObject("select count(*) from organizations", Integer.class))
        .isEqualTo(organizationCountBefore);
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void organizationCreationRejectsInvalidContractsAndCsrf() throws Exception {
    String password = "long-password-invalid-organization";
    createAccount("organization-invalid@example.com", password);
    HttpResponse<String> login = authenticatedLogin("organization-invalid@example.com", password);
    String technicalSession = cookieValue(login, "JSESSIONID");
    String sessionToken = cookieValue(login, "__Host-oriontask-session");
    HttpResponse<String> csrf = csrf(technicalSession);
    int organizationCountBefore =
        jdbcTemplate.queryForObject("select count(*) from organizations", Integer.class);

    HttpResponse<String> unexpectedField =
        createOrganization(
            Map.of("name", "Valid", "accountId", UUID.randomUUID().toString()),
            csrfToken(csrf),
            technicalSession,
            sessionToken);
    HttpResponse<String> blankName =
        createOrganization(Map.of("name", "  "), csrfToken(csrf), technicalSession, sessionToken);
    HttpResponse<String> missingCsrf =
        createOrganizationWithoutCsrf(Map.of("name", "Valid"), technicalSession, sessionToken);

    assertThat(unexpectedField.statusCode()).isEqualTo(400);
    assertThat(blankName.statusCode()).isEqualTo(400);
    assertThat(missingCsrf.statusCode()).isEqualTo(403);
    assertThat(jdbcTemplate.queryForObject("select count(*) from organizations", Integer.class))
        .isEqualTo(organizationCountBefore);
  }

  private HttpResponse<String> register(Map<String, String> payload) throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/accounts"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
            .build();
    return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
  }

  private UUID createAccount(String email, String password) {
    UUID accountId = UUID.randomUUID();
    jdbcTemplate.update(
        "insert into identity_accounts (id, normalized_email, password_hash, created_at) values (?, ?, ?, current_timestamp)",
        accountId,
        email,
        passwordHasher.hash(password));
    return accountId;
  }

  private HttpResponse<String> csrf() throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/csrf"))
            .GET()
            .build();
    return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> csrf(String technicalSession) throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/csrf"))
            .header("Cookie", "JSESSIONID=" + technicalSession)
            .GET()
            .build();
    return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> login(
      Map<String, String> payload, String csrfToken, String csrfSession) throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/sessions"))
            .header("Content-Type", "application/json")
            .header("X-CSRF-TOKEN", csrfToken)
            .header("Cookie", "JSESSIONID=" + csrfSession)
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
            .build();
    return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> authenticatedLogin(String email, String password) throws Exception {
    HttpResponse<String> csrf = csrf();
    return login(
        Map.of("email", email, "password", password),
        csrfToken(csrf),
        cookieValue(csrf, "JSESSIONID"));
  }

  private HttpResponse<String> createOrganization(
      Map<String, String> payload, String csrfToken, String technicalSession, String sessionToken)
      throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/organizations"))
            .header("Content-Type", "application/json")
            .header("X-CSRF-TOKEN", csrfToken)
            .header(
                "Cookie",
                "JSESSIONID=" + technicalSession + "; __Host-oriontask-session=" + sessionToken)
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
            .build();
    return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> createOrganizationWithoutCsrf(
      Map<String, String> payload, String technicalSession, String sessionToken) throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/organizations"))
            .header("Content-Type", "application/json")
            .header(
                "Cookie",
                "JSESSIONID=" + technicalSession + "; __Host-oriontask-session=" + sessionToken)
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
            .build();
    return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> loginWithoutCsrf(Map<String, String> payload, String csrfSession)
      throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/sessions"))
            .header("Content-Type", "application/json")
            .header("Cookie", "JSESSIONID=" + csrfSession)
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
            .build();
    return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> logout(String csrfToken, String csrfSession, String sessionCookie)
      throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/session"))
            .header("X-CSRF-TOKEN", csrfToken)
            .header(
                "Cookie",
                "JSESSIONID="
                    + csrfSession
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

  private String csrfToken(HttpResponse<String> response) throws Exception {
    return objectMapper.readTree(response.body()).path("token").asText();
  }
}
