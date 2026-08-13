package com.oriontask;

import static org.assertj.core.api.Assertions.assertThat;

import com.oriontask.identity.application.port.out.PasswordHasher;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
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
      "oriontask.security.session-hmac-key=VGhpcy1pcy1hLXRlc3QtaG1hYy1rZXktd2l0aC1zMi1ieXRlcy0xMjM0NTY=",
      "server.servlet.session.cookie.secure=true"
    })
@Testcontainers
class TicketOpeningHttpIntegrationTest {
  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

  @LocalServerPort private int port;
  @Autowired private JdbcTemplate jdbc;
  @Autowired private PasswordHasher passwordHasher;
  private final ObjectMapper json = new ObjectMapper();

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void authorizedRolesOpenTicketsWithServerCreatorDefaultPriorityAndOpenStatus() throws Exception {
    Tenant tenant = tenant("roles");
    UUID customer = client(tenant.organizationId(), "ACTIVE");
    for (String role : new String[] {"OWNER", "ADMIN", "TECHNICIAN"}) {
      Actor actor =
          role.equals("OWNER")
              ? new Actor(tenant.accountId(), tenant.session())
              : actor(tenant.organizationId(), role);
      HttpResponse<String> response =
          open(tenant.organizationId(), customer, null, actor.session());
      assertThat(response.statusCode()).isEqualTo(201);
      JsonNode body = json.readTree(response.body());
      assertThat(body.path("creatorAccountId").asText()).isEqualTo(actor.accountId().toString());
      assertThat(body.path("priority").asText()).isEqualTo("MEDIUM");
      assertThat(body.path("status").asText()).isEqualTo("OPEN");
      assertThat(response.headers().firstValue("location").orElse(""))
          .endsWith("/tickets/" + body.path("id").asText());
    }
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void inaccessibleRelationsAndOrganizationsAreIndistinguishableAndDoNotPersist() throws Exception {
    Tenant a = tenant("tenant-a");
    Tenant b = tenant("tenant-b");
    UUID activeA = client(a.organizationId(), "ACTIVE");
    UUID inactiveA = client(a.organizationId(), "INACTIVE");
    UUID activeB = client(b.organizationId(), "ACTIVE");
    Actor outsider = actor(b.organizationId(), "TECHNICIAN");
    UUID outsiderAssignee = outsider.accountId();
    HttpResponse<String> missing = open(UUID.randomUUID(), activeA, null, a.session());
    HttpResponse<String> inactive = open(a.organizationId(), inactiveA, null, a.session());
    HttpResponse<String> foreignCustomer = open(a.organizationId(), activeB, null, a.session());
    HttpResponse<String> foreignAssignee =
        open(a.organizationId(), activeA, outsiderAssignee, a.session());
    HttpResponse<String> noAccess = open(a.organizationId(), activeA, null, b.session());

    for (HttpResponse<String> response :
        new HttpResponse[] {inactive, foreignCustomer, foreignAssignee, noAccess}) {
      assertThat(response.statusCode()).isEqualTo(404);
      assertThat(response.body()).isEqualTo(missing.body());
    }
    assertThat(jdbc.queryForObject("select count(*) from organization_tickets", Integer.class))
        .isZero();
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void csrfAndUnexpectedFieldsAreRejectedWithoutPersistence() throws Exception {
    Tenant tenant = tenant("strict");
    UUID customer = client(tenant.organizationId(), "ACTIVE");
    HttpResponse<String> csrfRejected =
        request(
            "POST", path(tenant.organizationId()), ticket(customer, null), null, tenant.session());
    Map<String, String> invalid = ticket(customer, null);
    invalid.put("creatorAccountId", UUID.randomUUID().toString());
    HttpResponse<String> bodyRejected = open(tenant.organizationId(), invalid, tenant.session());

    assertThat(csrfRejected.statusCode()).isEqualTo(403);
    assertThat(bodyRejected.statusCode()).isEqualTo(400);
    assertThat(jdbc.queryForObject("select count(*) from organization_tickets", Integer.class))
        .isZero();
  }

  private Tenant tenant(String prefix) throws Exception {
    Actor owner = account(prefix + "-owner@example.com");
    UUID organization = UUID.randomUUID();
    jdbc.update(
        "insert into organizations (id, name, created_at, updated_at) values (?, ?, current_timestamp, current_timestamp)",
        organization,
        prefix);
    membership(organization, owner.accountId(), "OWNER");
    return new Tenant(owner.accountId(), organization, owner.session());
  }

  private Actor actor(UUID organizationId, String role) throws Exception {
    Actor actor = account(UUID.randomUUID() + "@example.com");
    membership(organizationId, actor.accountId(), role);
    return actor;
  }

  private Actor account(String email) throws Exception {
    UUID id = UUID.randomUUID();
    jdbc.update(
        "insert into identity_accounts (id, normalized_email, password_hash, created_at) values (?, ?, ?, current_timestamp)",
        id,
        email,
        passwordHasher.hash("long-password-ticket"));
    HttpResponse<String> csrf = csrf(null);
    HttpResponse<String> login =
        request(
            "POST",
            "/api/v1/sessions",
            Map.of("email", email, "password", "long-password-ticket"),
            token(csrf),
            new Session(cookie(csrf, "JSESSIONID"), null));
    return new Actor(
        id, new Session(cookie(login, "JSESSIONID"), cookie(login, "__Host-oriontask-session")));
  }

  private UUID client(UUID organizationId, String status) {
    UUID id = UUID.randomUUID();
    jdbc.update(
        "insert into organization_clients (id, organization_id, name, status, created_at, updated_at) values (?, ?, 'client', ?, current_timestamp, current_timestamp)",
        id,
        organizationId,
        status);
    return id;
  }

  private void membership(UUID organizationId, UUID accountId, String role) {
    jdbc.update(
        "insert into organization_memberships (id, organization_id, account_id, role, created_at, updated_at) values (?, ?, ?, ?, current_timestamp, current_timestamp)",
        UUID.randomUUID(),
        organizationId,
        accountId,
        role);
  }

  private HttpResponse<String> open(
      UUID organizationId, UUID customer, UUID assignee, Session session) throws Exception {
    return open(organizationId, ticket(customer, assignee), session);
  }

  private HttpResponse<String> open(UUID organizationId, Map<String, String> body, Session session)
      throws Exception {
    HttpResponse<String> csrf = csrf(session.technicalSession());
    return request("POST", path(organizationId), body, token(csrf), session);
  }

  private static Map<String, String> ticket(UUID customer, UUID assignee) {
    Map<String, String> body = new LinkedHashMap<>();
    body.put("title", "Ticket title");
    body.put("customerId", customer.toString());
    if (assignee != null) {
      body.put("assigneeAccountId", assignee.toString());
    }
    return body;
  }

  private String path(UUID organizationId) {
    return "/api/v1/organizations/" + organizationId + "/tickets";
  }

  private HttpResponse<String> csrf(String session) throws Exception {
    HttpRequest.Builder request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/csrf")).GET();
    if (session != null) {
      request.header("Cookie", "JSESSIONID=" + session);
    }
    return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> request(
      String method, String path, Map<String, String> body, String csrf, Session session)
      throws Exception {
    HttpRequest.Builder request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
    String cookies = "JSESSIONID=" + session.technicalSession();
    if (session.sessionToken() != null) {
      cookies += "; __Host-oriontask-session=" + session.sessionToken();
    }
    request.header("Cookie", cookies);
    if (csrf != null) {
      request.header("X-CSRF-TOKEN", csrf);
    }
    request
        .header("Content-Type", "application/json")
        .method(method, HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
    return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
  }

  private String token(HttpResponse<String> response) throws Exception {
    return json.readTree(response.body()).path("token").asText();
  }

  private static String cookie(HttpResponse<String> response, String name) {
    return response.headers().allValues("Set-Cookie").stream()
        .filter(v -> v.startsWith(name + "="))
        .map(v -> v.substring(name.length() + 1).split(";", 2)[0])
        .findFirst()
        .orElseThrow();
  }

  private record Session(String technicalSession, String sessionToken) {}

  private record Actor(UUID accountId, Session session) {}

  private record Tenant(UUID accountId, UUID organizationId, Session session) {}
}
