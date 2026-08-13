package com.oriontask.identity.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public record AuthenticationSession(
    UUID id,
    UUID accountId,
    String tokenDerivation,
    Instant createdAt,
    Instant lastActivityAt,
    Instant absoluteExpiresAt,
    Instant revokedAt) {

  private static final Duration INACTIVITY_TIMEOUT = Duration.ofMinutes(30);

  public AuthenticationSession {
    if (id == null
        || accountId == null
        || tokenDerivation == null
        || createdAt == null
        || lastActivityAt == null
        || absoluteExpiresAt == null) {
      throw new IllegalArgumentException("Authentication session fields must be present");
    }
  }

  public boolean isActiveAt(Instant now) {
    return revokedAt == null
        && now.isBefore(lastActivityAt.plus(INACTIVITY_TIMEOUT))
        && now.isBefore(absoluteExpiresAt);
  }
}
