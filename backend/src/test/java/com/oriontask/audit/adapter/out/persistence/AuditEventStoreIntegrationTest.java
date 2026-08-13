package com.oriontask.audit.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.oriontask.audit.application.port.out.AuditEventStore;
import com.oriontask.audit.application.usecase.PurgeAuditEventsService;
import com.oriontask.audit.domain.model.AuditAction;
import com.oriontask.audit.domain.model.AuditEvent;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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
        "oriontask.security.session-hmac-key=VGhpcy1pcy1hLXRlc3QtaG1hYy1rZXktd2l0aC0zMi1ieXRlcy0xMjM0NTY=")
@Testcontainers
class AuditEventStoreIntegrationTest {
  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

  @Autowired private AuditEventStore store;
  @Autowired private JdbcTemplate jdbc;

  @BeforeEach
  void clearAuditEvents() {
    jdbc.update("delete from audit_events");
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void isolatesOrganizationsOrdersPagesAndRestrictsTechnicians() {
    Tenant owner = tenant("OWNER");
    Tenant technician = tenant("TECHNICIAN");
    Instant occurredAt = Instant.parse("2026-01-01T00:00:00Z");
    UUID first = UUID.fromString("00000000-0000-0000-0000-000000000001");
    UUID second = UUID.fromString("00000000-0000-0000-0000-000000000002");
    store.save(event(owner.organizationId(), owner.accountId(), first, occurredAt));
    store.save(event(owner.organizationId(), owner.accountId(), second, occurredAt));
    store.save(
        event(technician.organizationId(), technician.accountId(), UUID.randomUUID(), occurredAt));

    AuditEventStore.AuthorizationResult firstPage =
        store.findAuthorized(owner.organizationId(), owner.accountId(), 0, 1);
    AuditEventStore.AuthorizationResult secondPage =
        store.findAuthorized(owner.organizationId(), owner.accountId(), 1, 1);

    assertThat(firstPage.access()).isEqualTo(AuditEventStore.AuthorizationResult.Access.ALLOWED);
    assertThat(firstPage.events()).extracting(AuditEvent::id).containsExactly(second);
    assertThat(secondPage.events()).extracting(AuditEvent::id).containsExactly(first);
    assertThat(
            store
                .findAuthorized(technician.organizationId(), technician.accountId(), 0, 50)
                .access())
        .isEqualTo(AuditEventStore.AuthorizationResult.Access.FORBIDDEN);
    assertThat(store.findAuthorized(owner.organizationId(), UUID.randomUUID(), 0, 50).access())
        .isEqualTo(AuditEventStore.AuthorizationResult.Access.NOT_FOUND);
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void storesOnlyApprovedFieldsAndPhysicallyPurgesExpiredEvents() {
    Tenant owner = tenant("OWNER");
    Instant now = Instant.parse("2026-08-10T00:00:00Z");
    store.save(
        event(
            owner.organizationId(),
            owner.accountId(),
            UUID.randomUUID(),
            now.minusSeconds(31_622_400)));
    store.save(
        event(owner.organizationId(), owner.accountId(), UUID.randomUUID(), now.minusSeconds(60)));

    new PurgeAuditEventsService(store, Clock.fixed(now, ZoneOffset.UTC)).purge();

    assertThat(
            jdbc
                .queryForList(
                    "select column_name from information_schema.columns where table_name = 'audit_events'",
                    String.class)
                .stream()
                .sorted())
        .containsExactly(
            "action",
            "actor_account_id",
            "id",
            "occurred_at",
            "organization_id",
            "resource_id",
            "resource_type");
    assertThat(jdbc.queryForObject("select count(*) from audit_events", Integer.class))
        .isEqualTo(1);
  }

  private Tenant tenant(String role) {
    UUID accountId = UUID.randomUUID();
    UUID organizationId = UUID.randomUUID();
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
        "insert into organization_memberships (id, organization_id, account_id, role, created_at, updated_at) values (?, ?, ?, ?, current_timestamp, current_timestamp)",
        UUID.randomUUID(),
        organizationId,
        accountId,
        role);
    return new Tenant(organizationId, accountId);
  }

  private static AuditEvent event(
      UUID organizationId, UUID accountId, UUID id, Instant occurredAt) {
    return new AuditEvent(
        id,
        organizationId,
        accountId,
        AuditAction.ORGANIZATION_CLIENT_CREATED,
        "client",
        UUID.randomUUID(),
        occurredAt);
  }

  private record Tenant(UUID organizationId, UUID accountId) {}
}
