package com.oriontask.audit.adapter.out.persistence;

import com.oriontask.audit.application.port.out.AuditEventStore;
import com.oriontask.audit.domain.model.AuditAction;
import com.oriontask.audit.domain.model.AuditEvent;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcAuditEventStore implements AuditEventStore {
  private final JdbcTemplate jdbc;

  public JdbcAuditEventStore(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public void save(AuditEvent event) {
    jdbc.update(
        "insert into audit_events (id, organization_id, actor_account_id, action, resource_type, resource_id, occurred_at) values (?, ?, ?, ?, ?, ?, ?)",
        event.id(),
        event.organizationId(),
        event.actorAccountId(),
        event.action().value(),
        event.resourceType(),
        event.resourceId(),
        Timestamp.from(event.occurredAt()));
  }

  @Override
  public AuthorizationResult findAuthorized(
      UUID organizationId, UUID accountId, int offset, int size) {
    List<String> roles =
        jdbc.queryForList(
            "select role from organization_memberships where organization_id = ? and account_id = ?",
            String.class,
            organizationId,
            accountId);
    if (roles.isEmpty()) {
      return new AuthorizationResult(AuthorizationResult.Access.NOT_FOUND, List.of());
    }
    if (roles.getFirst().equals("TECHNICIAN")) {
      return new AuthorizationResult(AuthorizationResult.Access.FORBIDDEN, List.of());
    }
    return new AuthorizationResult(
        AuthorizationResult.Access.ALLOWED,
        jdbc.query(
            "select id, organization_id, actor_account_id, action, resource_type, resource_id, occurred_at from audit_events where organization_id = ? order by occurred_at desc, id desc limit ? offset ?",
            (rs, row) ->
                new AuditEvent(
                    UUID.fromString(rs.getString("id")),
                    UUID.fromString(rs.getString("organization_id")),
                    UUID.fromString(rs.getString("actor_account_id")),
                    AuditAction.valueOf(rs.getString("action").toUpperCase().replace('.', '_')),
                    rs.getString("resource_type"),
                    UUID.fromString(rs.getString("resource_id")),
                    rs.getTimestamp("occurred_at").toInstant()),
            organizationId,
            size,
            offset));
  }

  @Override
  public void deleteOccurredAtOrBefore(Instant cutoff) {
    jdbc.update("delete from audit_events where occurred_at <= ?", Timestamp.from(cutoff));
  }
}
