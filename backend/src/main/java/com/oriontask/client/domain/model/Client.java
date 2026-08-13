package com.oriontask.client.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Client(
    UUID id,
    UUID organizationId,
    String name,
    Status status,
    Instant createdAt,
    Instant updatedAt,
    Instant deactivatedAt) {
  public enum Status {
    ACTIVE,
    INACTIVE
  }
}
