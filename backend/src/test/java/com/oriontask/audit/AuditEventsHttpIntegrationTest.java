package com.oriontask.audit;

import static org.assertj.core.api.Assertions.assertThat;

import com.oriontask.identity.application.port.out.PasswordHasher;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
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
      "oriontask.security.session-hmac-key=VGhpcy1pcy1hLXRlc3QtaG1hYy1rZXktd2l0aC0zMi1ieXRlcy0xMjM0NTY=",
      "server.servlet.session.cookie.secure=true"
    })
@Testcontainers
class AuditEventsHttpIntegrationTest {
  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

  @LocalServerPort private int port;
  @Autowired private JdbcTemplate jdbc;
  @Autowired private PasswordHasher passwordHasher;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void recordsAllCriticalMutationsAndEnforcesAuditHttpContract() throws Exception {
    Account owner = account("owner");
    UUID organizationId = createOrganization(owner.session(), "Organization Name Is Not Audited");
    Account admin = account("admin");
    addMembership(organizationId, admin.accountId(), "ADMIN");
    Account technician = account("technician");
    addMembership(organizationId, technician.accountId(), "TECHNICIAN");
    HttpResponse<String> technicianResponse =
        auditEvents(organizationId, "page=0&size=100", technician.session());
    Account recipient = account("recipient");
    Account outsider = account("outsider");

    JsonNode invitation =
        json(
            csrfRequest(
                "POST",
                "/api/v1/organizations/" + organizationId + "/invitations",
                Map.of("recipientEmail", recipient.email(), "role", "ADMIN"),
                owner.session()));
    assertThat(invitation.path("token").asText()).isNotBlank();
    assertThat(
            csrfRequest(
                    "POST",
                    "/api/v1/membership-invitations/"
                        + invitation.path("token").asText()
                        + "/accept",
                    null,
                    recipient.session())
                .statusCode())
        .isEqualTo(201);
    assertThat(
            csrfRequest(
                    "DELETE",
                    "/api/v1/organizations/"
                        + organizationId
                        + "/members/"
                        + technician.accountId(),
                    null,
                    owner.session())
                .statusCode())
        .isEqualTo(204);

    assertThat(
            csrfRequest(
                    "POST",
                    "/api/v1/organizations/" + organizationId + "/clients",
                    Map.of("name", "Sensitive Client Name"),
                    owner.session())
                .statusCode())
        .isEqualTo(201);
    UUID clientId =
        jdbc.queryForObject(
            "select id from organization_clients where organization_id = ? and name = ?",
            UUID.class,
            organizationId,
            "Sensitive Client Name");
    assertThat(
            csrfRequest(
                    "PATCH",
                    "/api/v1/organizations/" + organizationId + "/clients/" + clientId,
                    Map.of("name", "Changed Client Name"),
                    owner.session())
                .statusCode())
        .isEqualTo(200);
    assertThat(
            csrfRequest(
                    "DELETE",
                    "/api/v1/organizations/" + organizationId + "/clients/" + clientId,
                    null,
                    owner.session())
                .statusCode())
        .isEqualTo(204);

    HttpResponse<String> ownerResponse =
        auditEvents(organizationId, "page=0&size=100", owner.session());
    JsonNode events = json(ownerResponse);
    assertThat(ownerResponse.statusCode()).isEqualTo(200);
    assertThat(events.size()).isEqualTo(7);
    assertThat(events.toString())
        .doesNotContain("Sensitive Client Name", recipient.email(), "token", "role", "name");
    assertThat(events.get(0).propertyNames())
        .containsExactlyInAnyOrder(
            "id", "action", "resourceType", "resourceId", "actorAccountId", "occurredAt");
    assertThat(events.toString()).contains(owner.accountId().toString());
    assertThat(events.toString())
        .contains(
            "organization.created",
            "membership_invitation.created",
            "membership_invitation.accepted",
            "organization.membership_revoked",
            "organization.client_created",
            "organization.client_updated",
            "organization.client_deactivated");

    assertThat(auditEvents(organizationId, "page=0&size=100", admin.session()).statusCode())
        .isEqualTo(200);
    assertThat(technicianResponse.statusCode()).isEqualTo(403);
    HttpResponse<String> outsiderResponse =
        auditEvents(organizationId, "page=0&size=100", outsider.session());
    HttpResponse<String> missingResponse =
        auditEvents(UUID.randomUUID(), "page=0&size=100", outsider.session());
    assertThat(outsiderResponse.statusCode()).isEqualTo(404);
    assertThat(outsiderResponse.body()).isEqualTo(missingResponse.body());

    JsonNode firstPage = json(auditEvents(organizationId, "page=0&size=2", owner.session()));
    JsonNode secondPage = json(auditEvents(organizationId, "page=1&size=2", owner.session()));
    assertThat(firstPage.size()).isEqualTo(2);
    assertThat(secondPage.size()).isEqualTo(2);
    assertThat(firstPage.get(1).path("id").asText())
        .isNotEqualTo(secondPage.get(0).path("id").asText());
    assertThat(auditEvents(organizationId, "page=-1", owner.session()).statusCode()).isEqualTo(400);
    assertThat(auditEvents(organizationId, "size=0", owner.session()).statusCode()).isEqualTo(400);
    assertThat(auditEvents(organizationId, "size=101", owner.session()).statusCode())
        .isEqualTo(400);
  }

  private Account account(String prefix) throws Exception {
    String email = prefix + "-" + UUID.randomUUID() + "@example.com";
    UUID accountId = UUID.randomUUID();
    jdbc.update(
        "insert into identity_accounts (id, normalized_email, password_hash, created_at) values (?, ?, ?, current_timestamp)",
        accountId,
        email,
        passwordHasher.hash("long-password-audit"));
    return new Account(accountId, email, login(email));
  }

  private UUID createOrganization(Session session, String name) throws Exception {
    HttpResponse<String> response =
        csrfRequest("POST", "/api/v1/organizations", Map.of("name", name), session);
    assertThat(response.statusCode()).isEqualTo(201);
    return UUID.fromString(json(response).path("id").asText());
  }

  private HttpResponse<String> auditEvents(UUID organizationId, String query, Session session)
      throws Exception {
    return request(
        "GET",
        "/api/v1/organizations/" + organizationId + "/audit-events?" + query,
        null,
        null,
        session);
  }

  private HttpResponse<String> csrfRequest(
      String method, String path, Map<String, String> body, Session session) throws Exception {
    return request(method, path, body, csrfToken(csrf(session.technicalSession())), session);
  }

  private Session login(String email) throws Exception {
    HttpResponse<String> initialCsrf = csrf(null);
    HttpResponse<String> response =
        request(
            "POST",
            "/api/v1/sessions",
            Map.of("email", email, "password", "long-password-audit"),
            csrfToken(initialCsrf),
            new Session(cookieValue(initialCsrf, "JSESSIONID"), null));
    assertThat(response.statusCode()).isEqualTo(204);
    return new Session(
        cookieValue(response, "JSESSIONID"), cookieValue(response, "__Host-oriontask-session"));
  }

  private HttpResponse<String> csrf(String technicalSession) throws Exception {
    HttpRequest.Builder request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/csrf")).GET();
    if (technicalSession != null) {
      request.header("Cookie", "JSESSIONID=" + technicalSession);
    }
    return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> request(
      String method, String path, Map<String, String> body, String csrfToken, Session session)
      throws Exception {
    HttpRequest.Builder request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
    String cookie = "JSESSIONID=" + session.technicalSession();
    if (session.sessionToken() != null) {
      cookie += "; __Host-oriontask-session=" + session.sessionToken();
    }
    request.header("Cookie", cookie);
    if (csrfToken != null) {
      request.header("X-CSRF-TOKEN", csrfToken);
    }
    if (body == null) {
      if (method.equals("GET")) {
        request.GET();
      } else {
        request.method(method, HttpRequest.BodyPublishers.noBody());
      }
    } else {
      request
          .header("Content-Type", "application/json")
          .method(
              method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
    }
    return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
  }

  private void addMembership(UUID organizationId, UUID accountId, String role) {
    jdbc.update(
        "insert into organization_memberships (id, organization_id, account_id, role, created_at, updated_at) values (?, ?, ?, ?, current_timestamp, current_timestamp)",
        UUID.randomUUID(),
        organizationId,
        accountId,
        role);
  }

  private JsonNode json(HttpResponse<String> response) throws Exception {
    return objectMapper.readTree(response.body());
  }

  private String csrfToken(HttpResponse<String> response) throws Exception {
    return json(response).path("token").asText();
  }

  private static String cookieValue(HttpResponse<String> response, String name) {
    return response.headers().allValues("Set-Cookie").stream()
        .filter(value -> value.startsWith(name + "="))
        .map(value -> value.substring(name.length() + 1).split(";", 2)[0])
        .findFirst()
        .orElseThrow();
  }

  private record Account(UUID accountId, String email, Session session) {}

  private record Session(String technicalSession, String sessionToken) {}
}
