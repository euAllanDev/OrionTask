package com.oriontask.identity.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Account(UUID id, String normalizedEmail, String passwordHash, Instant createdAt) {

  public Account {
    if (id == null || normalizedEmail == null || passwordHash == null || createdAt == null) {
      throw new IllegalArgumentException("Account fields must be present");
    }
  }
}
