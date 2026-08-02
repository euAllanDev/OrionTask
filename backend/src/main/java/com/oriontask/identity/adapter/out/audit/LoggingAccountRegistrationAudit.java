package com.oriontask.identity.adapter.out.audit;

import com.oriontask.identity.application.port.out.AccountRegistrationAudit;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class LoggingAccountRegistrationAudit implements AccountRegistrationAudit {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(LoggingAccountRegistrationAudit.class);

  @Override
  public void accountCreated(UUID accountId) {
    LOGGER.info("account_registration_succeeded accountId={}", accountId);
  }
}
