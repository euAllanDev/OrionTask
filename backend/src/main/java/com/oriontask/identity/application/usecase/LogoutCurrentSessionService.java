package com.oriontask.identity.application.usecase;

import com.oriontask.identity.application.port.in.LogoutCurrentSessionUseCase;
import com.oriontask.identity.application.port.out.AuthenticationAudit;
import com.oriontask.identity.application.port.out.AuthenticationSessionStore;
import com.oriontask.identity.application.port.out.TokenDeriver;
import java.time.Clock;
import java.time.Instant;

public class LogoutCurrentSessionService implements LogoutCurrentSessionUseCase {
  private final AuthenticationSessionStore sessionStore;
  private final TokenDeriver tokenDeriver;
  private final AuthenticationAudit audit;
  private final Clock clock;

  public LogoutCurrentSessionService(
      AuthenticationSessionStore sessionStore,
      TokenDeriver tokenDeriver,
      AuthenticationAudit audit,
      Clock clock) {
    this.sessionStore = sessionStore;
    this.tokenDeriver = tokenDeriver;
    this.audit = audit;
    this.clock = clock;
  }

  @Override
  public void logout(String sessionToken) {
    if (sessionToken != null && !sessionToken.isBlank()) {
      sessionStore.revokeByTokenDerivation(tokenDeriver.derive(sessionToken), Instant.now(clock));
    }
    audit.sessionLoggedOut();
  }
}
