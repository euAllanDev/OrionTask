package com.oriontask.client.adapter.out.audit;

import com.oriontask.client.application.port.out.ClientAudit;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class LoggingClientAudit implements ClientAudit {
  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingClientAudit.class);

  @Override
  public void created(UUID organizationId, UUID clientId, UUID accountId) {
    log("organization.client_created", organizationId, clientId, accountId);
  }

  @Override
  public void updated(UUID organizationId, UUID clientId, UUID accountId) {
    log("organization.client_updated", organizationId, clientId, accountId);
  }

  @Override
  public void deactivated(UUID organizationId, UUID clientId, UUID accountId) {
    log("organization.client_deactivated", organizationId, clientId, accountId);
  }

  private static void log(String event, UUID organizationId, UUID clientId, UUID accountId) {
    LOGGER.info(
        "{} organizationId={} clientId={} accountId={}",
        event,
        organizationId,
        clientId,
        accountId);
  }
}
