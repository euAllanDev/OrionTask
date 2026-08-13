package com.oriontask.ticket.application.command;

import com.oriontask.ticket.domain.model.Ticket.Priority;
import java.util.UUID;

public record OpenTicketCommand(
    UUID organizationId,
    UUID creatorAccountId,
    UUID customerId,
    UUID assigneeAccountId,
    String title,
    String description,
    Priority priority) {}
