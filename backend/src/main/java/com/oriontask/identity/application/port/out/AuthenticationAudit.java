package com.oriontask.identity.application.port.out;

import java.util.UUID;

public interface AuthenticationAudit {
  void loginSucceeded(UUID accountId);

  void loginRejected();

  void rateLimitApplied();

  void sessionLoggedOut();
}
