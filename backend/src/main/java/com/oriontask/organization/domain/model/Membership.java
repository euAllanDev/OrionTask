package com.oriontask.organization.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Membership(
    UUID id, UUID organizationId, UUID accountId, Role role, Instant createdAt, Instant updatedAt) {
  public Membership {
    if (id == null
        || organizationId == null
        || accountId == null
        || role == null
        || createdAt == null
        || updatedAt == null) {
      throw new IllegalArgumentException("Membership fields must be present");
    }
  }

  public enum Role {
    OWNER,
    ADMIN,
    TECHNICIAN
  }
}
