package com.oriontask.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuthenticationSessionTest {
  @Test
  void expiresAfterThirtyMinutesWithoutAcceptedActivity() {
    Instant createdAt = Instant.parse("2026-08-04T12:00:00Z");
    AuthenticationSession session =
        session(createdAt, createdAt, createdAt.plusSeconds(8 * 60 * 60));

    assertThat(session.isActiveAt(createdAt.plusSeconds(29 * 60 + 59))).isTrue();
    assertThat(session.isActiveAt(createdAt.plusSeconds(30 * 60))).isFalse();
  }

  @Test
  void expiresAtAbsoluteLimitDespiteRecentActivity() {
    Instant createdAt = Instant.parse("2026-08-04T12:00:00Z");
    Instant absoluteExpiry = createdAt.plusSeconds(8 * 60 * 60);
    AuthenticationSession session =
        session(createdAt, absoluteExpiry.minusSeconds(1), absoluteExpiry);

    assertThat(session.isActiveAt(absoluteExpiry.minusSeconds(1))).isTrue();
    assertThat(session.isActiveAt(absoluteExpiry)).isFalse();
  }

  @Test
  void revokedSessionNeverAuthenticates() {
    Instant now = Instant.parse("2026-08-04T12:00:00Z");
    AuthenticationSession session =
        new AuthenticationSession(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "derivation",
            now,
            now,
            now.plusSeconds(8 * 60 * 60),
            now.plusSeconds(1));

    assertThat(session.isActiveAt(now.plusSeconds(2))).isFalse();
  }

  private static AuthenticationSession session(
      Instant createdAt, Instant lastActivityAt, Instant absoluteExpiresAt) {
    return new AuthenticationSession(
        UUID.randomUUID(),
        UUID.randomUUID(),
        "derivation",
        createdAt,
        lastActivityAt,
        absoluteExpiresAt,
        null);
  }
}
