package com.oriontask.ticket.application.query;

import com.oriontask.ticket.domain.model.Ticket;
import java.util.UUID;

public record TicketListQuery(
    UUID organizationId,
    UUID accountId,
    int page,
    int size,
    Ticket.Status status,
    Ticket.Priority priority,
    UUID customerId,
    UUID assigneeAccountId) {}
