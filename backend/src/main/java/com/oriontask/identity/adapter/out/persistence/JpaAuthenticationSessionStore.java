package com.oriontask.identity.adapter.out.persistence;

import com.oriontask.identity.application.port.out.AuthenticationSessionStore;
import com.oriontask.identity.domain.model.AuthenticationSession;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class JpaAuthenticationSessionStore implements AuthenticationSessionStore {
  private final AuthenticationSessionJpaRepository repository;

  JpaAuthenticationSessionStore(AuthenticationSessionJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public void create(AuthenticationSession session) {
    repository.saveAndFlush(new AuthenticationSessionJpaEntity(session));
  }

  @Override
  public Optional<AuthenticationSession> findByTokenDerivation(String tokenDerivation) {
    return repository
        .findByTokenDerivation(tokenDerivation)
        .map(AuthenticationSessionJpaEntity::toDomain);
  }

  @Override
  public void updateLastActivityIfNeeded(
      UUID sessionId, Instant now, Instant threshold, Instant inactivityCutoff) {
    repository.updateLastActivityIfNeeded(sessionId, now, threshold, inactivityCutoff);
  }

  @Override
  public void revokeByTokenDerivation(String tokenDerivation, Instant now) {
    repository.revokeByTokenDerivation(tokenDerivation, now);
  }
}
