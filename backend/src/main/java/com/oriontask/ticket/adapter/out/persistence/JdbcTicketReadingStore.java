package com.oriontask.ticket.adapter.out.persistence;

import com.oriontask.ticket.application.port.out.TicketReadingStore;
import com.oriontask.ticket.application.query.TicketListQuery;
import com.oriontask.ticket.application.query.TicketPage;
import com.oriontask.ticket.domain.model.Ticket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
class JdbcTicketReadingStore implements TicketReadingStore {
  private static final String AUTHORIZED =
      """
      exists (select 1 from organization_memberships m
              where m.organization_id = t.organization_id and m.account_id = ?
                and m.role in ('OWNER', 'ADMIN', 'TECHNICIAN'))
      """;
  private final JdbcTemplate jdbc;

  JdbcTicketReadingStore(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public Optional<Ticket> find(UUID organizationId, UUID ticketId, UUID accountId) {
    return jdbc
        .query(
            "select t.* from organization_tickets t where t.organization_id = ? and t.id = ? and "
                + AUTHORIZED,
            this::ticket,
            organizationId,
            ticketId,
            accountId)
        .stream()
        .findFirst();
  }

  @Override
  public Optional<TicketPage> list(TicketListQuery query) {
    if (!authorized(query.organizationId(), query.accountId())) {
      return Optional.empty();
    }
    Filter filter = filter(query);
    List<Object> itemArguments = new ArrayList<>(filter.arguments());
    itemArguments.add(query.size());
    itemArguments.add((long) query.page() * query.size());
    List<Ticket> items =
        jdbc.query(
            "select t.* from organization_tickets t where "
                + filter.clause()
                + " order by t.created_at desc, t.id desc limit ? offset ?",
            this::ticket,
            itemArguments.toArray());
    Long total =
        jdbc.queryForObject(
            "select count(*) from organization_tickets t where " + filter.clause(),
            Long.class,
            filter.arguments().toArray());
    return Optional.of(new TicketPage(items, query.page(), query.size(), total));
  }

  private Filter filter(TicketListQuery query) {
    StringBuilder clause = new StringBuilder("t.organization_id = ? and ").append(AUTHORIZED);
    List<Object> arguments = new ArrayList<>(List.of(query.organizationId(), query.accountId()));
    append(clause, arguments, "t.status", query.status() == null ? null : query.status().name());
    append(
        clause, arguments, "t.priority", query.priority() == null ? null : query.priority().name());
    append(clause, arguments, "t.customer_id", query.customerId());
    append(clause, arguments, "t.assignee_account_id", query.assigneeAccountId());
    return new Filter(clause.toString(), arguments);
  }

  private static void append(
      StringBuilder clause, List<Object> arguments, String column, Object value) {
    if (value != null) {
      clause.append(" and ").append(column).append(" = ?");
      arguments.add(value);
    }
  }

  private boolean authorized(UUID organizationId, UUID accountId) {
    Integer count =
        jdbc.queryForObject(
            """
            select count(*) from organization_memberships
            where organization_id = ? and account_id = ?
              and role in ('OWNER', 'ADMIN', 'TECHNICIAN')
            """,
            Integer.class,
            organizationId,
            accountId);
    return count != null && count == 1;
  }

  private Ticket ticket(ResultSet rs, int row) throws SQLException {
    return new Ticket(
        rs.getObject("id", UUID.class),
        rs.getObject("organization_id", UUID.class),
        rs.getObject("customer_id", UUID.class),
        rs.getObject("creator_account_id", UUID.class),
        rs.getObject("assignee_account_id", UUID.class),
        rs.getString("title"),
        rs.getString("description"),
        Ticket.Priority.valueOf(rs.getString("priority")),
        Ticket.Status.valueOf(rs.getString("status")),
        rs.getTimestamp("created_at").toInstant(),
        rs.getTimestamp("updated_at").toInstant());
  }

  private record Filter(String clause, List<Object> arguments) {}
}
