package com.oriontask.organization.adapter.out.audit;

import com.oriontask.organization.application.port.out.OrganizationCreationAudit;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class LoggingOrganizationCreationAudit implements OrganizationCreationAudit {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(LoggingOrganizationCreationAudit.class);

  @Override
  public void organizationCreated(UUID accountId, UUID organizationId) {
    LOGGER.info("organization.created accountId={} organizationId={}", accountId, organizationId);
  }
}
