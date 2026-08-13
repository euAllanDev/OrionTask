package com.oriontask.organization.application.port.in;

import com.oriontask.organization.domain.model.Membership;
import java.time.Instant;
import java.util.UUID;

public record ListedOrganization(
    UUID id, String name, Instant createdAt, Instant updatedAt, Membership.Role role) {
  public ListedOrganization {
    if (id == null || name == null || createdAt == null || updatedAt == null || role == null) {
      throw new IllegalArgumentException("Listed organization fields must be present");
    }
  }
}
