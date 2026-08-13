package com.oriontask.ticket.adapter.out.persistence;

import com.oriontask.ticket.application.command.OpenTicketCommand;
import com.oriontask.ticket.application.port.out.TicketOpeningStore;
import com.oriontask.ticket.domain.model.Ticket;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class JdbcTicketOpeningStore implements TicketOpeningStore {
  private final JdbcTemplate jdbc;

  JdbcTicketOpeningStore(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  @Transactional
  public Optional<Ticket> open(OpenTicketCommand command, UUID ticketId) {
    return jdbc
        .query(
            """
            insert into organization_tickets (
              id, organization_id, customer_id, creator_account_id, assignee_account_id,
              title, description, priority, status, created_at, updated_at)
            select ?, ?, ?, ?, ?, ?, ?, ?, 'OPEN', current_timestamp, current_timestamp
            where exists (
              select 1 from organization_memberships
              where organization_id = ? and account_id = ?
                and role in ('OWNER', 'ADMIN', 'TECHNICIAN'))
              and exists (
                select 1 from organization_clients
                where organization_id = ? and id = ? and status = 'ACTIVE')
              and (cast(? as uuid) is null or exists (
                select 1 from organization_memberships
                where organization_id = ? and account_id = ?))
            returning *
            """,
            (rs, row) ->
                new Ticket(
                    UUID.fromString(rs.getString("id")),
                    UUID.fromString(rs.getString("organization_id")),
                    UUID.fromString(rs.getString("customer_id")),
                    UUID.fromString(rs.getString("creator_account_id")),
                    rs.getObject("assignee_account_id", UUID.class),
                    rs.getString("title"),
                    rs.getString("description"),
                    Ticket.Priority.valueOf(rs.getString("priority")),
                    Ticket.Status.valueOf(rs.getString("status")),
                    rs.getTimestamp("created_at").toInstant(),
                    rs.getTimestamp("updated_at").toInstant()),
            ticketId,
            command.organizationId(),
            command.customerId(),
            command.creatorAccountId(),
            command.assigneeAccountId(),
            command.title(),
            command.description(),
            command.priority().name(),
            command.organizationId(),
            command.creatorAccountId(),
            command.organizationId(),
            command.customerId(),
            command.assigneeAccountId(),
            command.organizationId(),
            command.assigneeAccountId())
        .stream()
        .findFirst();
  }
}
