package com.oriontask.identity.adapter.out.audit;

import com.oriontask.identity.application.port.out.AuthenticationAudit;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class LoggingAuthenticationAudit implements AuthenticationAudit {
  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAuthenticationAudit.class);

  @Override
  public void loginSucceeded(UUID accountId) {
    LOGGER.info("authentication_login_succeeded accountId={}", accountId);
  }

  @Override
  public void loginRejected() {
    LOGGER.info("authentication_login_rejected");
  }

  @Override
  public void rateLimitApplied() {
    LOGGER.info("authentication_rate_limit_applied");
  }

  @Override
  public void sessionLoggedOut() {
    LOGGER.info("authentication_session_logged_out");
  }
}
