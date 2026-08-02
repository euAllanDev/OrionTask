package com.oriontask;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrionTaskApplicationTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

  @LocalServerPort private int port;

  @org.springframework.beans.factory.annotation.Autowired private JdbcTemplate jdbcTemplate;

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

  private HttpResponse<String> register(Map<String, String> payload) throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/accounts"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
            .build();
    return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
  }
}
