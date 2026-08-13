package com.oriontask;

import static org.assertj.core.api.Assertions.assertThat;

import com.oriontask.identity.application.port.out.PasswordHasher;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
class TicketReadingHttpIntegrationTest {
  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

  @LocalServerPort private int port;
  @Autowired private JdbcTemplate jdbc;
  @Autowired private PasswordHasher passwordHasher;
  private final ObjectMapper json = new ObjectMapper();

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void allRolesReadOrderedTicketsWithoutCsrfAndResponsesHaveExactFields() throws Exception {
    Tenant tenant = tenant("roles");
    UUID customer = client(tenant.organizationId(), "INACTIVE");
    UUID older =
        ticket(
            tenant.organizationId(),
            customer,
            tenant.accountId(),
            "older",
            Instant.parse("2026-01-01T00:00:00Z"));
    UUID newer =
        ticket(
            tenant.organizationId(),
            customer,
            tenant.accountId(),
            "newer",
            Instant.parse("2026-01-02T00:00:00Z"));
    for (String role : new String[] {"OWNER", "ADMIN", "TECHNICIAN"}) {
      Actor actor =
          role.equals("OWNER")
              ? new Actor(tenant.accountId(), tenant.session())
              : actor(tenant.organizationId(), role);
      HttpResponse<String> list = get(path(tenant.organizationId()), actor.session());
      assertThat(list.statusCode()).isEqualTo(200);
      JsonNode body = json.readTree(list.body());
      assertThat(body.propertyNames())
          .containsExactlyInAnyOrder("items", "page", "size", "totalElements", "totalPages");
      assertThat(body.path("page").asInt()).isZero();
      assertThat(body.path("size").asInt()).isEqualTo(50);
      assertThat(body.path("items").get(0).path("id").asText()).isEqualTo(newer.toString());
      assertThat(body.path("items").get(1).path("id").asText()).isEqualTo(older.toString());
      assertThat(body.path("items").get(0).propertyNames())
          .containsExactlyInAnyOrder(
              "id",
              "title",
              "status",
              "priority",
              "customerId",
              "assigneeAccountId",
              "createdAt",
              "updatedAt");
      HttpResponse<String> detail =
          get(path(tenant.organizationId()) + "/" + newer, actor.session());
      assertThat(detail.statusCode()).isEqualTo(200);
      assertThat(json.readTree(detail.body()).propertyNames())
          .containsExactlyInAnyOrder(
              "id",
              "title",
              "description",
              "status",
              "priority",
              "customerId",
              "creatorAccountId",
              "assigneeAccountId",
              "createdAt",
              "updatedAt");
    }
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void filtersPaginateStrictlyAndHideForeignResources() throws Exception {
    Tenant a = tenant("a");
    Tenant b = tenant("b");
    UUID inactiveA = client(a.organizationId(), "INACTIVE");
    UUID customerB = client(b.organizationId(), "ACTIVE");
    UUID first =
        ticket(
            a.organizationId(),
            inactiveA,
            a.accountId(),
            "first",
            Instant.parse("2026-01-01T00:00:00Z"));
    ticket(
        a.organizationId(),
        inactiveA,
        a.accountId(),
        "second",
        Instant.parse("2026-01-02T00:00:00Z"));
    UUID foreign =
        ticket(
            b.organizationId(),
            customerB,
            b.accountId(),
            "foreign",
            Instant.parse("2026-01-03T00:00:00Z"));

    HttpResponse<String> filtered =
        get(path(a.organizationId()) + "?customerId=" + inactiveA + "&size=1&page=1", a.session());
    JsonNode page = json.readTree(filtered.body());
    assertThat(filtered.statusCode()).isEqualTo(200);
    assertThat(page.path("totalElements").asInt()).isEqualTo(2);
    assertThat(page.path("totalPages").asInt()).isEqualTo(2);
    assertThat(page.path("items").get(0).path("id").asText()).isEqualTo(first.toString());
    for (String query :
        new String[] {
          "unknown=x", "page=0&page=1", "size=101", "status=CLOSED", "customerId=nope"
        }) {
      assertThat(get(path(a.organizationId()) + "?" + query, a.session()).statusCode())
          .isEqualTo(400);
    }
    HttpResponse<String> foreignFilter =
        get(path(a.organizationId()) + "?customerId=" + customerB, a.session());
    assertThat(foreignFilter.statusCode()).isEqualTo(200);
    assertThat(json.readTree(foreignFilter.body()).path("totalElements").asInt()).isZero();
    HttpResponse<String> missing =
        get(path(a.organizationId()) + "/" + UUID.randomUUID(), a.session());
    HttpResponse<String> foreignTicket = get(path(a.organizationId()) + "/" + foreign, a.session());
    HttpResponse<String> noMembership = get(path(a.organizationId()), b.session());
    assertThat(foreignTicket.statusCode()).isEqualTo(404);
    assertThat(foreignTicket.body()).isEqualTo(missing.body());
    assertThat(noMembership.statusCode()).isEqualTo(404);
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
            "{\"email\":\"" + email + "\",\"password\":\"long-password-ticket\"}",
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

  private UUID ticket(
      UUID organizationId, UUID customerId, UUID creator, String title, Instant createdAt) {
    UUID id = UUID.randomUUID();
    jdbc.update(
        "insert into organization_tickets (id, organization_id, customer_id, creator_account_id, title, priority, status, created_at, updated_at) values (?, ?, ?, ?, ?, 'MEDIUM', 'OPEN', ?, ?)",
        id,
        organizationId,
        customerId,
        creator,
        title,
        java.sql.Timestamp.from(createdAt),
        java.sql.Timestamp.from(createdAt));
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

  private HttpResponse<String> get(String path, Session session) throws Exception {
    return request("GET", path, null, null, session);
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
      String method, String path, String body, String csrf, Session session) throws Exception {
    HttpRequest.Builder request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
    String cookies = "JSESSIONID=" + session.technicalSession();
    if (session.sessionToken() != null) {
      cookies += "; __Host-oriontask-session=" + session.sessionToken();
    }
    request.header("Cookie", cookies);
    request.header("Content-Type", "application/json");
    if (csrf != null) {
      request.header("X-CSRF-TOKEN", csrf);
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
        .filter(v -> v.startsWith(name + "="))
        .map(v -> v.substring(name.length() + 1).split(";", 2)[0])
        .findFirst()
        .orElseThrow();
  }

  private String path(UUID organizationId) {
    return "/api/v1/organizations/" + organizationId + "/tickets";
  }

  private record Session(String technicalSession, String sessionToken) {}

  private record Actor(UUID accountId, Session session) {}

  private record Tenant(UUID accountId, UUID organizationId, Session session) {}
}
