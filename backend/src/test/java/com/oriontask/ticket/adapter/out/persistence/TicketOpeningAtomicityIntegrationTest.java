package com.oriontask.ticket.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.oriontask.ticket.application.command.OpenTicketCommand;
import com.oriontask.ticket.application.port.out.TicketOpeningStore;
import com.oriontask.ticket.domain.model.Ticket;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
    properties =
        "oriontask.security.session-hmac-key=VGhpcy1pcy1hLXRlc3QtaG1hYy1rZXktd2l0aC1zMi1ieXRlcy0xMjM0NTY=")
@Testcontainers
class TicketOpeningAtomicityIntegrationTest {
  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

  @Autowired private JdbcTemplate jdbc;
  @Autowired private TicketOpeningStore store;

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void injectedFailureAfterInsertRollsBackTicket() {
    UUID organizationId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();
    seed(organizationId, accountId, customerId);
    jdbc.execute(
        "create function fail_ticket_insert() returns trigger language plpgsql as $$ begin raise exception 'injected failure'; end; $$");
    jdbc.execute(
        "create trigger fail_ticket_insert after insert on organization_tickets for each row execute function fail_ticket_insert()");
    UUID ticketId = UUID.randomUUID();

    assertThatThrownBy(
            () ->
                store.open(
                    new OpenTicketCommand(
                        organizationId,
                        accountId,
                        customerId,
                        null,
                        "title",
                        null,
                        Ticket.Priority.MEDIUM),
                    ticketId))
        .hasMessageContaining("injected failure");
    org.assertj.core.api.Assertions.assertThat(
            jdbc.queryForObject(
                "select count(*) from organization_tickets where id = ?", Integer.class, ticketId))
        .isZero();
  }

  private void seed(UUID organizationId, UUID accountId, UUID customerId) {
    jdbc.update(
        "insert into identity_accounts (id, normalized_email, password_hash, created_at) values (?, ?, 'hash', current_timestamp)",
        accountId,
        accountId + "@example.com");
    jdbc.update(
        "insert into organizations (id, name, created_at, updated_at) values (?, 'organization', current_timestamp, current_timestamp)",
        organizationId);
    jdbc.update(
        "insert into organization_memberships (id, organization_id, account_id, role, created_at, updated_at) values (?, ?, ?, 'OWNER', current_timestamp, current_timestamp)",
        UUID.randomUUID(),
        organizationId,
        accountId);
    jdbc.update(
        "insert into organization_clients (id, organization_id, name, status, created_at, updated_at) values (?, ?, 'client', 'ACTIVE', current_timestamp, current_timestamp)",
        customerId,
        organizationId);
  }
}
