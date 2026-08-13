package com.oriontask.ticket.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Ticket(
    UUID id,
    UUID organizationId,
    UUID customerId,
    UUID creatorAccountId,
    UUID assigneeAccountId,
    String title,
    String description,
    Priority priority,
    Status status,
    Instant createdAt,
    Instant updatedAt) {
  public enum Priority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
  }

  public enum Status {
    OPEN
  }
}
