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
    properties = {
      "oriontask.security.session-hmac-key=VGhpcy1pcy1hLXRlc3QtaG1hYy1rZXktd2l0aC0zMi1ieXRlcy0xMjM0NTY=",
      "server.servlet.session.cookie.secure=true"
    })
@Testcontainers
class OrganizationIsolationIntegrationTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

  @LocalServerPort private int port;

  @org.springframework.beans.factory.annotation.Autowired private JdbcTemplate jdbcTemplate;

  @org.springframework.beans.factory.annotation.Autowired private PasswordHasher passwordHasher;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void crossOrganizationReadsAreHiddenLikeMissingResources() throws Exception {
    Tenant tenantA = createTenant("isolation-read-a");
    Tenant tenantB = createTenant("isolation-read-b");
    UUID clientA = createClient(tenantA, "Tenant A Client");

    HttpResponse<String> organizationDenied =
        getOrganization(tenantA.organizationId(), tenantB.session());
    HttpResponse<String> organizationMissing =
        getOrganization(UUID.randomUUID(), tenantB.session());
    HttpResponse<String> listDenied = listClients(tenantA.organizationId(), tenantB.session());
    HttpResponse<String> listMissing = listClients(UUID.randomUUID(), tenantB.session());
    HttpResponse<String> clientDenied =
        getClient(tenantA.organizationId(), clientA, tenantB.session());
    HttpResponse<String> clientMissing =
        getClient(tenantB.organizationId(), UUID.randomUUID(), tenantB.session());
    HttpResponse<String> clientUnderWrongOrganization =
        getClient(tenantB.organizationId(), clientA, tenantB.session());

    assertThat(organizationDenied.statusCode()).isEqualTo(404);
    assertThat(organizationDenied.body()).isEqualTo(organizationMissing.body());
    assertThat(listDenied.statusCode()).isEqualTo(404);
    assertThat(listDenied.body()).isEqualTo(listMissing.body());
    assertThat(clientDenied.statusCode()).isEqualTo(404);
    assertThat(clientDenied.body()).isEqualTo(clientMissing.body());
    assertThat(clientUnderWrongOrganization.statusCode()).isEqualTo(404);
    assertThat(listDenied.body()).doesNotContain("Tenant A Client");
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void crossOrganizationClientMutationsWithValidCsrfDoNotChangeEitherTenant() throws Exception {
    Tenant tenantA = createTenant("isolation-mutate-a");
    Tenant tenantB = createTenant("isolation-mutate-b");
    UUID clientA = createClient(tenantA, "Original Tenant A Client");

    HttpResponse<String> updateDenied =
        clientMutation(
            "PATCH",
            tenantA.organizationId(),
            clientA,
            Map.of("name", "Changed By Tenant B"),
            tenantB.session());
    HttpResponse<String> deactivateDenied =
        clientMutation("DELETE", tenantA.organizationId(), clientA, null, tenantB.session());
    HttpResponse<String> wrongOrganizationUpdate =
        clientMutation(
            "PATCH",
            tenantB.organizationId(),
            clientA,
            Map.of("name", "Changed Under Wrong Organization"),
            tenantB.session());
    HttpResponse<String> wrongOrganizationDeactivate =
        clientMutation("DELETE", tenantB.organizationId(), clientA, null, tenantB.session());

    assertThat(updateDenied.statusCode()).isEqualTo(404);
    assertThat(deactivateDenied.statusCode()).isEqualTo(404);
    assertThat(wrongOrganizationUpdate.statusCode()).isEqualTo(404);
    assertThat(wrongOrganizationDeactivate.statusCode()).isEqualTo(404);
    assertThat(
            jdbcTemplate.queryForObject(
                "select name from organization_clients where id = ? and organization_id = ?",
                String.class,
                clientA,
                tenantA.organizationId()))
        .isEqualTo("Original Tenant A Client");
    assertThat(
            jdbcTemplate.queryForObject(
                "select status from organization_clients where id = ? and organization_id = ?",
                String.class,
                clientA,
                tenantA.organizationId()))
        .isEqualTo("ACTIVE");
    assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from organization_clients where organization_id = ?",
                Integer.class,
                tenantB.organizationId()))
        .isZero();
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void crossOrganizationMembershipRevocationWithValidCsrfDoesNotChangeMemberships()
      throws Exception {
    Tenant tenantA = createTenant("isolation-revoke-a");
    Tenant tenantB = createTenant("isolation-revoke-b");
    UUID memberA = createAccount("isolation-member-a@example.com");
    addMembership(tenantA.organizationId(), memberA, "TECHNICIAN");

    HttpResponse<String> denied =
        revokeMembership(tenantA.organizationId(), memberA, tenantB.session());
    HttpResponse<String> missing =
        revokeMembership(UUID.randomUUID(), UUID.randomUUID(), tenantB.session());

    assertThat(denied.statusCode()).isEqualTo(404);
    assertThat(denied.body()).isEqualTo(missing.body());
    assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from organization_memberships where organization_id = ? and account_id = ?",
                Integer.class,
                tenantA.organizationId(),
                memberA))
        .isEqualTo(1);
    assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from organization_memberships where organization_id = ? and account_id = ?",
                Integer.class,
                tenantB.organizationId(),
                tenantB.accountId()))
        .isEqualTo(1);
  }

  private Tenant createTenant(String prefix) throws Exception {
    String email = prefix + "@example.com";
    UUID accountId = createAccount(email);
    Session session = authenticatedLogin(email);
    return new Tenant(accountId, createOrganization(session, prefix + " organization"), session);
  }

  private UUID createAccount(String email) {
    UUID accountId = UUID.randomUUID();
    jdbcTemplate.update(
        "insert into identity_accounts (id, normalized_email, password_hash, created_at) values (?, ?, ?, current_timestamp)",
        accountId,
        email,
        passwordHasher.hash("long-password-isolation"));
    return accountId;
  }

  private UUID createOrganization(Session session, String name) throws Exception {
    HttpResponse<String> csrf = csrf(session.technicalSession());
    HttpResponse<String> response =
        request("POST", "/api/v1/organizations", Map.of("name", name), csrfToken(csrf), session);
    assertThat(response.statusCode()).isEqualTo(201);
    return UUID.fromString(objectMapper.readTree(response.body()).path("id").asText());
  }

  private UUID createClient(Tenant tenant, String name) throws Exception {
    HttpResponse<String> response =
        clientMutation(
            "POST", tenant.organizationId(), null, Map.of("name", name), tenant.session());
    assertThat(response.statusCode()).isEqualTo(201);
    return jdbcTemplate.queryForObject(
        "select id from organization_clients where organization_id = ? and name = ?",
        UUID.class,
        tenant.organizationId(),
        name);
  }

  private HttpResponse<String> getOrganization(UUID organizationId, Session session)
      throws Exception {
    return request("GET", "/api/v1/organizations/" + organizationId, null, null, session);
  }

  private HttpResponse<String> listClients(UUID organizationId, Session session) throws Exception {
    return request(
        "GET", "/api/v1/organizations/" + organizationId + "/clients", null, null, session);
  }

  private HttpResponse<String> getClient(UUID organizationId, UUID clientId, Session session)
      throws Exception {
    return request(
        "GET",
        "/api/v1/organizations/" + organizationId + "/clients/" + clientId,
        null,
        null,
        session);
  }

  private HttpResponse<String> clientMutation(
      String method, UUID organizationId, UUID clientId, Map<String, String> body, Session session)
      throws Exception {
    String path = "/api/v1/organizations/" + organizationId + "/clients";
    if (clientId != null) {
      path += "/" + clientId;
    }
    HttpResponse<String> csrf = csrf(session.technicalSession());
    return request(method, path, body, csrfToken(csrf), session);
  }

  private HttpResponse<String> revokeMembership(
      UUID organizationId, UUID accountId, Session session) throws Exception {
    HttpResponse<String> csrf = csrf(session.technicalSession());
    return request(
        "DELETE",
        "/api/v1/organizations/" + organizationId + "/members/" + accountId,
        null,
        csrfToken(csrf),
        session);
  }

  private Session authenticatedLogin(String email) throws Exception {
    HttpResponse<String> csrf = csrf(null);
    HttpResponse<String> login =
        request(
            "POST",
            "/api/v1/sessions",
            Map.of("email", email, "password", "long-password-isolation"),
            csrfToken(csrf),
            new Session(cookieValue(csrf, "JSESSIONID"), null));
    assertThat(login.statusCode()).isEqualTo(204);
    return new Session(
        cookieValue(login, "JSESSIONID"), cookieValue(login, "__Host-oriontask-session"));
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
    jdbcTemplate.update(
        "insert into organization_memberships (id, organization_id, account_id, role, created_at, updated_at) values (?, ?, ?, ?, current_timestamp, current_timestamp)",
        UUID.randomUUID(),
        organizationId,
        accountId,
        role);
  }

  private String csrfToken(HttpResponse<String> response) throws Exception {
    JsonNode body = objectMapper.readTree(response.body());
    return body.path("token").asText();
  }

  private static String cookieValue(HttpResponse<String> response, String name) {
    return response.headers().allValues("Set-Cookie").stream()
        .filter(value -> value.startsWith(name + "="))
        .map(value -> value.substring(name.length() + 1).split(";", 2)[0])
        .findFirst()
        .orElseThrow();
  }

  private record Session(String technicalSession, String sessionToken) {}

  private record Tenant(UUID accountId, UUID organizationId, Session session) {}
}
