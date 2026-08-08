package com.oriontask.organization.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Organization(UUID id, String name, Instant createdAt, Instant updatedAt) {
  public Organization {
    if (id == null || name == null || createdAt == null || updatedAt == null) {
      throw new IllegalArgumentException("Organization fields must be present");
    }
  }
}
