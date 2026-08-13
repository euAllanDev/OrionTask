package com.oriontask;

import static org.assertj.core.api.Assertions.assertThat;

import com.oriontask.identity.application.port.out.PasswordHasher;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.time.Instant;
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
class ListOrganizationsHttpIntegrationTest {
  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

  @LocalServerPort private int port;
  @Autowired private JdbcTemplate jdbc;
  @Autowired private PasswordHasher passwordHasher;
  private final ObjectMapper json = new ObjectMapper();

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void listsOnlyCurrentMembershipsWithAllRolesInFixedOrderWithoutCsrf() throws Exception {
    AccountSession account = accountSession("organizations@example.com");
    UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000001");
    UUID technician = UUID.fromString("00000000-0000-0000-0000-000000000002");
    UUID admin = UUID.fromString("00000000-0000-0000-0000-000000000003");
    createOrganization(owner, "Owner", Instant.EPOCH);
    createOrganization(technician, "Technician", Instant.ofEpochSecond(1));
    createOrganization(admin, "Admin", Instant.ofEpochSecond(1));
    createOrganization(UUID.randomUUID(), "Hidden", Instant.ofEpochSecond(2));
    addMembership(owner, account.accountId(), "OWNER");
    addMembership(technician, account.accountId(), "TECHNICIAN");
    addMembership(admin, account.accountId(), "ADMIN");

    HttpResponse<String> response = list(null, account.sessionToken());

    assertThat(response.statusCode()).isEqualTo(200);
    JsonNode body = json.readTree(response.body());
    assertThat(body).hasSize(3);
    assertThat(body.get(0).path("id").asText()).isEqualTo(admin.toString());
    assertThat(body.get(1).path("id").asText()).isEqualTo(technician.toString());
    assertThat(body.get(2).path("id").asText()).isEqualTo(owner.toString());
    assertThat(body.get(0).path("role").asText()).isEqualTo("ADMIN");
    assertThat(body.get(1).path("role").asText()).isEqualTo("TECHNICIAN");
    assertThat(body.get(2).path("role").asText()).isEqualTo("OWNER");
    assertThat(body.get(0).propertyNames())
        .containsExactlyInAnyOrder("id", "name", "createdAt", "updatedAt", "role");
    assertThat(response.body()).doesNotContain("Hidden");
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void returnsEmptyForAccountWithoutMembershipAndUnauthorizedWithoutSession() throws Exception {
    AccountSession account = accountSession("empty-organizations@example.com");

    HttpResponse<String> empty = list(null, account.sessionToken());
    HttpResponse<String> absent = list(null, null);

    assertThat(empty.statusCode()).isEqualTo(200);
    assertThat(empty.body()).isEqualTo("[]");
    assertThat(absent.statusCode()).isEqualTo(401);
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void rejectsQueryOrBodyWithoutMutation() throws Exception {
    AccountSession account = accountSession("invalid-organizations@example.com");
    int organizationsBefore =
        jdbc.queryForObject("select count(*) from organizations", Integer.class);
    int membershipsBefore =
        jdbc.queryForObject("select count(*) from organization_memberships", Integer.class);

    HttpResponse<String> query = list("?accountId=" + UUID.randomUUID(), account.sessionToken());
    HttpResponse<String> body = listWithBody("{}", account.sessionToken());

    assertThat(query.statusCode()).isEqualTo(400);
    assertThat(body.statusCode()).isEqualTo(400);
    assertThat(jdbc.queryForObject("select count(*) from organizations", Integer.class))
        .isEqualTo(organizationsBefore);
    assertThat(jdbc.queryForObject("select count(*) from organization_memberships", Integer.class))
        .isEqualTo(membershipsBefore);
  }

  private AccountSession accountSession(String email) throws Exception {
    UUID accountId = UUID.randomUUID();
    jdbc.update(
        "insert into identity_accounts (id, normalized_email, password_hash, created_at) values (?, ?, ?, current_timestamp)",
        accountId,
        email,
        passwordHasher.hash("long-password-organizations"));
    HttpResponse<String> csrf = request("GET", "/api/v1/csrf", null, null);
    HttpResponse<String> authenticatedLogin =
        requestWithCsrf(
            "POST",
            "/api/v1/sessions",
            "{\"email\":\"" + email + "\",\"password\":\"long-password-organizations\"}",
            "JSESSIONID=" + cookie(csrf, "JSESSIONID"),
            json.readTree(csrf.body()).path("token").asText());
    assertThat(authenticatedLogin.statusCode()).isEqualTo(204);
    return new AccountSession(accountId, cookie(authenticatedLogin, "__Host-oriontask-session"));
  }

  private void createOrganization(UUID id, String name, Instant updatedAt) {
    jdbc.update(
        "insert into organizations (id, name, created_at, updated_at) values (?, ?, ?, ?)",
        id,
        name,
        Timestamp.from(Instant.EPOCH),
        Timestamp.from(updatedAt));
  }

  private void addMembership(UUID organizationId, UUID accountId, String role) {
    jdbc.update(
        "insert into organization_memberships (id, organization_id, account_id, role, created_at, updated_at) values (?, ?, ?, ?, current_timestamp, current_timestamp)",
        UUID.randomUUID(),
        organizationId,
        accountId,
        role);
  }

  private HttpResponse<String> list(String query, String sessionToken) throws Exception {
    return request(
        "GET",
        "/api/v1/organizations" + (query == null ? "" : query),
        null,
        sessionToken == null ? null : "__Host-oriontask-session=" + sessionToken);
  }

  private HttpResponse<String> listWithBody(String body, String sessionToken) throws Exception {
    return request(
        "GET", "/api/v1/organizations", body, "__Host-oriontask-session=" + sessionToken);
  }

  private HttpResponse<String> request(String method, String path, String body, String cookie)
      throws Exception {
    return requestWithCsrf(method, path, body, cookie, null);
  }

  private HttpResponse<String> requestWithCsrf(
      String method, String path, String body, String cookie, String csrfToken) throws Exception {
    HttpRequest.Builder request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
    if (cookie != null) {
      request.header("Cookie", cookie);
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

  private static String cookie(HttpResponse<String> response, String name) {
    return response.headers().allValues("Set-Cookie").stream()
        .filter(value -> value.startsWith(name + "="))
        .map(value -> value.substring(name.length() + 1).split(";", 2)[0])
        .findFirst()
        .orElseThrow();
  }

  private record AccountSession(UUID accountId, String sessionToken) {}
}
