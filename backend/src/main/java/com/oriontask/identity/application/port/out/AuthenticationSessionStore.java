package com.oriontask.identity.application.port.out;

import com.oriontask.identity.domain.model.AuthenticationSession;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AuthenticationSessionStore {
  void create(AuthenticationSession session);

  Optional<AuthenticationSession> findByTokenDerivation(String tokenDerivation);

  void updateLastActivityIfNeeded(
      UUID sessionId, Instant now, Instant threshold, Instant inactivityCutoff);

  void revokeByTokenDerivation(String tokenDerivation, Instant now);
}
