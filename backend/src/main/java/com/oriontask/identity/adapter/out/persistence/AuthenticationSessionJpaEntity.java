package com.oriontask.identity.adapter.out.persistence;

import com.oriontask.identity.domain.model.AuthenticationSession;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "identity_authentication_sessions")
class AuthenticationSessionJpaEntity {
  @Id private UUID id;

  @Column(name = "account_id", nullable = false)
  private UUID accountId;

  @Column(name = "token_derivation", nullable = false, unique = true)
  private String tokenDerivation;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "last_activity_at", nullable = false)
  private Instant lastActivityAt;

  @Column(name = "absolute_expires_at", nullable = false)
  private Instant absoluteExpiresAt;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  protected AuthenticationSessionJpaEntity() {}

  AuthenticationSessionJpaEntity(AuthenticationSession session) {
    id = session.id();
    accountId = session.accountId();
    tokenDerivation = session.tokenDerivation();
    createdAt = session.createdAt();
    lastActivityAt = session.lastActivityAt();
    absoluteExpiresAt = session.absoluteExpiresAt();
    revokedAt = session.revokedAt();
  }

  AuthenticationSession toDomain() {
    return new AuthenticationSession(
        id, accountId, tokenDerivation, createdAt, lastActivityAt, absoluteExpiresAt, revokedAt);
  }
}
