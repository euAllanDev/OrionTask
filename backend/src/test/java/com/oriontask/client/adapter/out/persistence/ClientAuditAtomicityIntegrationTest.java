package com.oriontask.client.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.oriontask.audit.application.port.out.AuditEventStore;
import com.oriontask.client.application.port.out.ClientStore;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
    properties =
        "oriontask.security.session-hmac-key=VGhpcy1pcy1hLXRlc3QtaG1hYy1rZXktd2l0aC0zMi1ieXRlcy0xMjM0NTY=")
@Testcontainers
class ClientAuditAtomicityIntegrationTest {
  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

  @Autowired private JdbcTemplate jdbc;
  @Autowired private TransactionTemplate transactionTemplate;

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void auditWriteFailureRollsBackClientMutation() {
    UUID accountId = UUID.randomUUID();
    UUID organizationId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();
    seedOwner(accountId, organizationId);
    AuditEventStore failingStore =
        new AuditEventStore() {
          @Override
          public void save(com.oriontask.audit.domain.model.AuditEvent event) {
            throw new IllegalStateException("audit write failed");
          }

          @Override
          public AuthorizationResult findAuthorized(
              UUID organization, UUID account, int offset, int size) {
            return new AuthorizationResult(AuthorizationResult.Access.NOT_FOUND, List.of());
          }

          @Override
          public void deleteOccurredAtOrBefore(java.time.Instant cutoff) {}
        };
    ClientStore store = new JdbcClientStore(jdbc, failingStore);

    assertThatThrownBy(
            () ->
                transactionTemplate.executeWithoutResult(
                    ignored -> store.create(organizationId, accountId, clientId, "Client")))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("audit write failed");
    assertThat(
            jdbc.queryForObject(
                "select count(*) from organization_clients where id = ?", Integer.class, clientId))
        .isZero();
  }

  private void seedOwner(UUID accountId, UUID organizationId) {
    jdbc.update(
        "insert into identity_accounts (id, normalized_email, password_hash, created_at) values (?, ?, ?, current_timestamp)",
        accountId,
        accountId + "@example.com",
        "hash");
    jdbc.update(
        "insert into organizations (id, name, created_at, updated_at) values (?, ?, current_timestamp, current_timestamp)",
        organizationId,
        "organization");
    jdbc.update(
        "insert into organization_memberships (id, organization_id, account_id, role, created_at, updated_at) values (?, ?, ?, 'OWNER', current_timestamp, current_timestamp)",
        UUID.randomUUID(),
        organizationId,
        accountId);
  }
}
