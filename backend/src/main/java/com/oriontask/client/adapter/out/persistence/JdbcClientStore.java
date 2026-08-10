package com.oriontask.client.adapter.out.persistence;

import com.oriontask.client.application.port.out.ClientStore;
import com.oriontask.client.domain.model.Client;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class JdbcClientStore implements ClientStore {
  private final JdbcTemplate jdbc;

  JdbcClientStore(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  @Transactional
  public Result create(UUID organizationId, UUID accountId, UUID clientId, String name) {
    Result access = writeAccess(organizationId, accountId);
    if (access != null) {
      return access;
    }
    jdbc.update(
        "insert into organization_clients (id, organization_id, name, status, created_at, updated_at) values (?, ?, ?, 'ACTIVE', current_timestamp, current_timestamp)",
        clientId,
        organizationId,
        name);
    return Result.CREATED;
  }

  @Override
  public boolean hasAccess(UUID organizationId, UUID accountId) {
    return hasMembership(organizationId, accountId);
  }

  @Override
  public Optional<Client> find(UUID organizationId, UUID accountId, UUID clientId) {
    if (!hasMembership(organizationId, accountId)) {
      return Optional.empty();
    }
    return jdbc
        .query(
            "select * from organization_clients where organization_id = ? and id = ?",
            (rs, row) ->
                new Client(
                    UUID.fromString(rs.getString("id")),
                    UUID.fromString(rs.getString("organization_id")),
                    rs.getString("name"),
                    Client.Status.valueOf(rs.getString("status")),
                    rs.getTimestamp("created_at").toInstant(),
                    rs.getTimestamp("updated_at").toInstant(),
                    rs.getTimestamp("deactivated_at") == null
                        ? null
                        : rs.getTimestamp("deactivated_at").toInstant()),
            organizationId,
            clientId)
        .stream()
        .findFirst();
  }

  @Override
  public List<Client> list(UUID organizationId, UUID accountId, Client.Status status) {
    String sql =
        status == null
            ? "select * from organization_clients where organization_id = ? order by created_at desc"
            : "select * from organization_clients where organization_id = ? and status = ? order by created_at desc";
    return jdbc.query(
        sql,
        (rs, row) ->
            new Client(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("organization_id")),
                rs.getString("name"),
                Client.Status.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant(),
                rs.getTimestamp("deactivated_at") == null
                    ? null
                    : rs.getTimestamp("deactivated_at").toInstant()),
        status == null
            ? new Object[] {organizationId}
            : new Object[] {organizationId, status.name()});
  }

  @Override
  @Transactional
  public Result update(UUID organizationId, UUID accountId, UUID clientId, String name) {
    Result access = writeAccess(organizationId, accountId);
    if (access != null) {
      return access;
    }
    return jdbc.update(
                "update organization_clients set name = ?, updated_at = current_timestamp where organization_id = ? and id = ?",
                name,
                organizationId,
                clientId)
            == 1
        ? Result.UPDATED
        : Result.NOT_FOUND;
  }

  @Override
  @Transactional
  public Result deactivate(UUID organizationId, UUID accountId, UUID clientId) {
    Result access = writeAccess(organizationId, accountId);
    if (access != null) {
      return access;
    }
    return jdbc.update(
                "update organization_clients set status = 'INACTIVE', deactivated_at = current_timestamp, updated_at = current_timestamp where organization_id = ? and id = ?",
                organizationId,
                clientId)
            == 1
        ? Result.DEACTIVATED
        : Result.NOT_FOUND;
  }

  private boolean hasMembership(UUID organizationId, UUID accountId) {
    return jdbc.queryForList(
                "select 1 from organization_memberships where organization_id = ? and account_id = ?",
                organizationId,
                accountId)
            .size()
        == 1;
  }

  private Result writeAccess(UUID organizationId, UUID accountId) {
    List<String> roles =
        jdbc.queryForList(
            "select role from organization_memberships where organization_id = ? and account_id = ?",
            String.class,
            organizationId,
            accountId);
    if (roles.isEmpty()) {
      return Result.NOT_FOUND;
    }
    return roles.getFirst().equals("TECHNICIAN") ? Result.FORBIDDEN : null;
  }
}
