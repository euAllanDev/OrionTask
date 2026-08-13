package com.oriontask.identity.adapter.out.persistence;

import com.oriontask.identity.domain.model.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "identity_accounts")
class AccountJpaEntity {
  @Id private UUID id;

  @Column(name = "normalized_email", nullable = false, unique = true)
  private String normalizedEmail;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected AccountJpaEntity() {}

  AccountJpaEntity(UUID id, String normalizedEmail, String passwordHash, Instant createdAt) {
    this.id = id;
    this.normalizedEmail = normalizedEmail;
    this.passwordHash = passwordHash;
    this.createdAt = createdAt;
  }

  Account toDomain() {
    return new Account(id, normalizedEmail, passwordHash, createdAt);
  }

  UUID id() {
    return id;
  }
}
